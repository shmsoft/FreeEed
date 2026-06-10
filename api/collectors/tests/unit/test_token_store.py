"""Tests for project-scoped OAuth token store."""

import tempfile

import pytest
from cryptography.fernet import Fernet

from collectors.base.auth_context import AuthContext
from collectors.core.security_manager import SecurityManager
from collectors.core.token_store import TokenStore


@pytest.fixture
def store_and_security():
    key = Fernet.generate_key().decode()
    with tempfile.TemporaryDirectory() as tmp:
        security = SecurityManager(encryption_key=key, secrets_dir=tmp)
        token_store = TokenStore(db_path=f"{tmp}/state.db")
        yield token_store, security


def test_save_and_load_encrypted_tokens(store_and_security):
    token_store, security = store_and_security
    auth = AuthContext(
        connector_id="dropbox",
        access_token="plain-access",
        refresh_token="plain-refresh",
    )
    token_store.save_auth_context("1", "dropbox", auth, security)
    loaded = token_store.get_auth_context("1", "dropbox", security)
    assert loaded is not None
    assert loaded.access_token == "plain-access"
    assert loaded.refresh_token == "plain-refresh"
    assert loaded.extra.get("project_id") == "1"


def test_has_tokens(store_and_security):
    token_store, security = store_and_security
    assert not token_store.has_tokens("1", "dropbox")
    token_store.save_auth_context(
        "1",
        "dropbox",
        AuthContext(connector_id="dropbox", refresh_token="r"),
        security,
    )
    assert token_store.has_tokens("1", "dropbox")
