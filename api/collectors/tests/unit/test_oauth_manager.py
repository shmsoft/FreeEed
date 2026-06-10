"""Unit tests for OAuth manager (no live provider calls)."""

from datetime import datetime, timedelta
from unittest.mock import MagicMock, patch

import pytest
from cryptography.fernet import Fernet

from collectors.core.oauth_manager import (
    OAuthError,
    OAuthManager,
    _code_challenge_s256,
    _generate_code_verifier,
)
from collectors.core.security_manager import SecurityManager
from collectors.core.token_store import TokenStore


@pytest.fixture
def oauth_stack(tmp_path):
    key = Fernet.generate_key().decode()
    secrets_dir = tmp_path / "secrets"
    secrets_dir.mkdir()
    (secrets_dir / "dropbox.json").write_text(
        '{"client_id": "test-client", "client_secret": "test-secret"}',
        encoding="utf-8",
    )
    db_path = str(tmp_path / "state.db")
    security = SecurityManager(encryption_key=key, secrets_dir=str(secrets_dir))
    token_store = TokenStore(db_path=db_path)
    manager = OAuthManager(
        token_store=token_store,
        security_manager=security,
        redirect_base="http://localhost:8000",
    )
    return manager, token_store, security


def test_pkce_challenge_is_deterministic():
    verifier = "test-verifier-value"
    challenge = _code_challenge_s256(verifier)
    assert challenge == _code_challenge_s256(verifier)
    assert "=" not in challenge
    assert len(_generate_code_verifier()) >= 43


def test_build_authorize_url_contains_pkce_and_state(oauth_stack):
    manager, token_store, _ = oauth_stack
    url = manager.build_authorize_url("dropbox", "project-1")
    assert "dropbox.com/oauth2/authorize" in url
    assert "code_challenge=" in url
    assert "code_challenge_method=S256" in url
    assert "state=" in url
    assert "token_access_type=offline" in url
    assert "redirect_uri=http%3A%2F%2Flocalhost%3A8000%2Fcollect%2Foauth%2Fdropbox%2Fcallback" in url


@patch("collectors.core.oauth_manager.requests.post")
def test_handle_callback_exchanges_and_persists(mock_post, oauth_stack):
    manager, token_store, security = oauth_stack
    url = manager.build_authorize_url("dropbox", "case-42")
    state = url.split("state=")[1].split("&")[0]

    mock_response = MagicMock()
    mock_response.status_code = 200
    mock_response.json.return_value = {
        "access_token": "access-abc",
        "refresh_token": "refresh-xyz",
        "token_type": "bearer",
        "expires_in": 14400,
        "scope": "files.metadata.read files.content.read",
    }
    mock_post.return_value = mock_response

    auth, project_id, redirect_after = manager.handle_callback(
        "dropbox", code="auth-code", state=state
    )
    assert project_id == "case-42"
    assert redirect_after is None
    assert auth.access_token == "access-abc"
    assert auth.refresh_token == "refresh-xyz"

    stored = token_store.get_auth_context("case-42", "dropbox", security)
    assert stored is not None
    assert stored.access_token == "access-abc"
    assert stored.refresh_token == "refresh-xyz"

    mock_post.assert_called_once()
    payload = mock_post.call_args.kwargs["data"]
    assert payload["grant_type"] == "authorization_code"
    assert payload["code"] == "auth-code"
    assert payload["code_verifier"]
    assert payload["client_id"] == "test-client"
    assert payload["client_secret"] == "test-secret"


def test_handle_callback_rejects_reused_state(oauth_stack):
    manager, _, _ = oauth_stack
    url = manager.build_authorize_url("dropbox", "p1")
    state = url.split("state=")[1].split("&")[0]

    with patch("collectors.core.oauth_manager.requests.post") as mock_post:
        mock_post.return_value = MagicMock(
            status_code=200,
            json=lambda: {
                "access_token": "a",
                "refresh_token": "r",
                "expires_in": 3600,
            },
        )
        manager.handle_callback("dropbox", code="c", state=state)

    with pytest.raises(OAuthError, match="Invalid or expired"):
        manager.handle_callback("dropbox", code="c", state=state)


def test_handle_callback_rejects_expired_state(oauth_stack):
    manager, token_store, _ = oauth_stack
    state = "expired-state-token"
    token_store.save_oauth_state(
        state=state,
        project_id="p1",
        connector_id="dropbox",
        code_verifier="verifier",
        expires_at=datetime.utcnow() - timedelta(minutes=1),
    )
    with pytest.raises(OAuthError, match="Invalid or expired"):
        manager.handle_callback("dropbox", code="code", state=state)


def test_unsupported_connector_raises(oauth_stack):
    manager, _, _ = oauth_stack
    with pytest.raises(OAuthError, match="not supported"):
        manager.build_authorize_url("box", "1")
