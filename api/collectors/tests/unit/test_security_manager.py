"""Tests for SecurityManager."""

import os
import tempfile
from datetime import datetime, timedelta

import pytest
from cryptography.fernet import Fernet

from collectors.base.auth_context import AuthContext
from collectors.core.security_manager import SecurityManager


@pytest.fixture
def security():
    key = Fernet.generate_key().decode()
    with tempfile.TemporaryDirectory() as tmp:
        yield SecurityManager(encryption_key=key, secrets_dir=tmp)


def test_encrypt_decrypt_roundtrip(security):
    encrypted = security.encrypt_token("secret-value")
    assert security.decrypt_token(encrypted) == "secret-value"


def test_sanitize_redacts_tokens(security):
    data = {"access_token": "abc123", "name": "file.txt"}
    sanitized = security.sanitize_for_log(data)
    assert sanitized["access_token"] == "***REDACTED***"
    assert sanitized["name"] == "file.txt"


def test_refresh_hook(security):
    auth = AuthContext(
        connector_id="box",
        access_token="old",
        expires_at=datetime.utcnow() - timedelta(minutes=5),
    )

    def refresh(ctx):
        ctx.access_token = "new"
        ctx.expires_at = datetime.utcnow() + timedelta(hours=1)
        return ctx

    security.register_refresh_hook("box", refresh)
    refreshed = security.refresh_auth_if_needed(auth)
    assert refreshed.access_token == "new"


def test_load_secrets_from_file(security):
    secrets_file = security.secrets_dir / "box.json"
    secrets_file.write_text('{"access_token": "file-token", "refresh_token": "refresh"}')
    secrets = security.load_connector_secrets("box")
    assert secrets["access_token"] == "file-token"


def test_get_connector_credentials_prefers_project_tokens(security, tmp_path):
    from collectors.core.token_store import TokenStore

    token_store = TokenStore(db_path=str(tmp_path / "state.db"))
    (security.secrets_dir / "dropbox.json").write_text(
        '{"access_token": "global-token", "refresh_token": "global-refresh"}',
        encoding="utf-8",
    )
    token_store.save_auth_context(
        "proj-1",
        "dropbox",
        AuthContext(connector_id="dropbox", access_token="oauth-access", refresh_token="oauth-refresh"),
        security,
    )
    creds = security.get_connector_credentials("dropbox", "proj-1", token_store)
    assert creds["access_token"] == "oauth-access"
    assert creds["refresh_token"] == "oauth-refresh"

    global_only = security.get_connector_credentials("dropbox", "other", token_store)
    assert global_only["access_token"] == "global-token"
