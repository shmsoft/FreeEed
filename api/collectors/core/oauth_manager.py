"""OAuth2 authorization code flow with PKCE for collector connectors."""

from __future__ import annotations

import base64
import hashlib
import os
import secrets
from dataclasses import dataclass
from datetime import datetime, timedelta
from typing import Any, Dict, Optional
from urllib.parse import urlencode

import requests

from collectors.base.auth_context import AuthContext
from collectors.core.security_manager import SecurityManager
from collectors.core.token_store import TokenStore


class OAuthError(Exception):
    """OAuth flow failure."""


@dataclass(frozen=True)
class OAuthProviderConfig:
    authorize_url: str
    token_url: str
    scopes: str
    client_id_env: str
    client_secret_env: str


PROVIDER_CONFIGS: Dict[str, OAuthProviderConfig] = {
    "dropbox": OAuthProviderConfig(
        authorize_url="https://www.dropbox.com/oauth2/authorize",
        token_url="https://api.dropboxapi.com/oauth2/token",
        scopes="files.metadata.read files.content.read account_info.read",
        client_id_env="DROPBOX_CLIENT_ID",
        client_secret_env="DROPBOX_CLIENT_SECRET",
    ),
}


class OAuthManager:
    """Provider-agnostic OAuth2 manager (authorization code + PKCE)."""

    STATE_TTL_MINUTES = 10

    def __init__(
        self,
        token_store: Optional[TokenStore] = None,
        security_manager: Optional[SecurityManager] = None,
        redirect_base: Optional[str] = None,
    ):
        self.token_store = token_store or TokenStore()
        self.security = security_manager or SecurityManager()
        self.redirect_base = (
            redirect_base or os.getenv("COLLECTOR_OAUTH_REDIRECT_BASE", "http://localhost:8000")
        ).rstrip("/")

    def supports_connector(self, connector_id: str) -> bool:
        return connector_id in PROVIDER_CONFIGS

    def callback_url(self, connector_id: str) -> str:
        return f"{self.redirect_base}/collect/oauth/{connector_id}/callback"

    def build_authorize_url(
        self,
        connector_id: str,
        project_id: str,
        redirect_after: Optional[str] = None,
    ) -> str:
        provider = self._provider(connector_id)
        client_id, _ = self._client_credentials(connector_id)
        if not client_id:
            raise OAuthError(
                f"OAuth client_id not configured for '{connector_id}'. "
                f"Set {provider.client_id_env} or secrets/{connector_id}.json"
            )

        code_verifier = _generate_code_verifier()
        code_challenge = _code_challenge_s256(code_verifier)
        state = secrets.token_urlsafe(32)
        expires_at = datetime.utcnow() + timedelta(minutes=self.STATE_TTL_MINUTES)
        self.token_store.save_oauth_state(
            state=state,
            project_id=project_id,
            connector_id=connector_id,
            code_verifier=code_verifier,
            expires_at=expires_at,
            redirect_after=redirect_after,
        )

        params = {
            "client_id": client_id,
            "response_type": "code",
            "redirect_uri": self.callback_url(connector_id),
            "state": state,
            "code_challenge": code_challenge,
            "code_challenge_method": "S256",
            "token_access_type": "offline",
            "scope": provider.scopes,
        }
        return f"{provider.authorize_url}?{urlencode(params)}"

    def handle_callback(
        self,
        connector_id: str,
        code: str,
        state: str,
    ) -> tuple[AuthContext, str, Optional[str]]:
        """Exchange code for tokens, persist, return auth + project_id + redirect_after."""
        if not code:
            raise OAuthError("Missing authorization code")
        if not state:
            raise OAuthError("Missing OAuth state")

        oauth_state = self.token_store.consume_oauth_state(state, connector_id)
        if not oauth_state:
            raise OAuthError("Invalid or expired OAuth state")

        project_id = oauth_state["project_id"]
        auth = self._exchange_code(
            connector_id=connector_id,
            code=code,
            code_verifier=oauth_state["code_verifier"],
        )
        self.token_store.save_auth_context(project_id, connector_id, auth, self.security)
        return auth, project_id, oauth_state.get("redirect_after")

    def _exchange_code(
        self,
        connector_id: str,
        code: str,
        code_verifier: str,
    ) -> AuthContext:
        provider = self._provider(connector_id)
        client_id, client_secret = self._client_credentials(connector_id)
        if not client_id or not client_secret:
            raise OAuthError(f"OAuth client credentials not configured for '{connector_id}'")

        response = requests.post(
            provider.token_url,
            data={
                "grant_type": "authorization_code",
                "code": code,
                "code_verifier": code_verifier,
                "client_id": client_id,
                "client_secret": client_secret,
                "redirect_uri": self.callback_url(connector_id),
            },
            timeout=30,
        )
        if response.status_code >= 400:
            detail = response.text[:500]
            raise OAuthError(f"Token exchange failed ({response.status_code}): {detail}")

        data = response.json()
        expires_in = int(data.get("expires_in", 14400))
        scopes_raw = data.get("scope", provider.scopes)
        scopes = scopes_raw.split() if isinstance(scopes_raw, str) else list(scopes_raw or [])
        return AuthContext(
            connector_id=connector_id,
            access_token=data.get("access_token"),
            refresh_token=data.get("refresh_token"),
            token_type=data.get("token_type", "Bearer"),
            expires_at=datetime.utcnow() + timedelta(seconds=expires_in),
            scopes=scopes,
        )

    def _provider(self, connector_id: str) -> OAuthProviderConfig:
        if connector_id not in PROVIDER_CONFIGS:
            raise OAuthError(f"OAuth not supported for connector '{connector_id}'")
        return PROVIDER_CONFIGS[connector_id]

    def _client_credentials(self, connector_id: str) -> tuple[Optional[str], Optional[str]]:
        provider = self._provider(connector_id)
        secrets = self.security.load_connector_secrets(connector_id)
        client_id = secrets.get("client_id") or os.getenv(provider.client_id_env)
        client_secret = secrets.get("client_secret") or os.getenv(provider.client_secret_env)
        return client_id, client_secret


def _generate_code_verifier() -> str:
    return secrets.token_urlsafe(64)[:96]


def _code_challenge_s256(code_verifier: str) -> str:
    digest = hashlib.sha256(code_verifier.encode("ascii")).digest()
    return base64.urlsafe_b64encode(digest).rstrip(b"=").decode("ascii")
