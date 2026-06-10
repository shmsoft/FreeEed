"""Dropbox OAuth2 authentication."""

from __future__ import annotations

import os
from datetime import datetime, timedelta
from typing import Any, Dict, Optional

import requests

from collectors.base.auth_context import AuthContext


class DropboxAuth:
    TOKEN_URL = "https://api.dropboxapi.com/oauth2/token"

    def __init__(self, auth: AuthContext, config: Optional[Dict[str, Any]] = None):
        self.auth = auth
        self.config = config or {}
        self.client_id = self.config.get("client_id") or os.getenv("DROPBOX_CLIENT_ID")
        self.client_secret = self.config.get("client_secret") or os.getenv("DROPBOX_CLIENT_SECRET")

    def ensure_valid_token(self) -> AuthContext:
        if not self.auth.is_expired():
            return self.auth
        if not self.auth.refresh_token:
            if self.auth.access_token:
                return self.auth
            raise ValueError("Dropbox credentials missing")
        return self.refresh_token()

    def refresh_token(self) -> AuthContext:
        if self.config.get("mock_refresh"):
            self.auth.access_token = "mock-dropbox-token"
            self.auth.expires_at = datetime.utcnow() + timedelta(hours=1)
            return self.auth

        response = requests.post(
            self.TOKEN_URL,
            data={
                "grant_type": "refresh_token",
                "refresh_token": self.auth.refresh_token,
                "client_id": self.client_id,
                "client_secret": self.client_secret,
            },
            timeout=30,
        )
        if response.status_code >= 400:
            raise RuntimeError(f"Dropbox token refresh failed: {response.status_code}")
        data = response.json()
        self.auth.access_token = data["access_token"]
        if "refresh_token" in data:
            self.auth.refresh_token = data["refresh_token"]
        expires_in = int(data.get("expires_in", 14400))
        self.auth.expires_at = datetime.utcnow() + timedelta(seconds=expires_in)
        return self.auth

    def build_client(self, auth: AuthContext):
        import dropbox

        return dropbox.Dropbox(oauth2_access_token=auth.access_token)
