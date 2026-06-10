"""Secrets management and log sanitization."""

from __future__ import annotations

import json
import os
import re
from pathlib import Path
from typing import TYPE_CHECKING, Any, Callable, Dict, Optional

from cryptography.fernet import Fernet, InvalidToken

from collectors.base.auth_context import AuthContext

if TYPE_CHECKING:
    from collectors.core.token_store import TokenStore


class SecurityManager:
    """Load secrets from env/files, encrypt tokens, sanitize logs."""

    SENSITIVE_KEYS = frozenset(
        {
            "access_token",
            "refresh_token",
            "client_secret",
            "password",
            "api_key",
            "authorization",
        }
    )

    def __init__(
        self,
        encryption_key: Optional[str] = None,
        secrets_dir: Optional[str] = None,
    ):
        key = encryption_key or os.getenv("COLLECTOR_ENCRYPTION_KEY")
        self._fernet: Optional[Fernet] = None
        if key:
            self._fernet = Fernet(key.encode() if isinstance(key, str) else key)
        self.secrets_dir = Path(secrets_dir or os.getenv("COLLECTOR_SECRETS_DIR", "/app/secrets"))
        self._refresh_hooks: Dict[str, Callable[[AuthContext], AuthContext]] = {}

    def encrypt_token(self, plaintext: str) -> str:
        if not self._fernet:
            return plaintext
        return self._fernet.encrypt(plaintext.encode()).decode()

    def decrypt_token(self, ciphertext: str) -> str:
        if not self._fernet:
            return ciphertext
        try:
            return self._fernet.decrypt(ciphertext.encode()).decode()
        except InvalidToken as exc:
            raise ValueError("Failed to decrypt token") from exc

    def get_env_secret(self, key: str, default: Optional[str] = None) -> Optional[str]:
        value = os.getenv(key, default)
        if value and value.startswith("enc:"):
            return self.decrypt_token(value[4:])
        return value

    def load_connector_secrets(self, connector_id: str) -> Dict[str, Any]:
        """Load secrets from env prefix or JSON file."""
        prefix = f"COLLECTOR_{connector_id.upper().replace('-', '_')}_"
        secrets: Dict[str, Any] = {}
        for env_key, env_val in os.environ.items():
            if env_key.startswith(prefix):
                field = env_key[len(prefix) :].lower()
                secrets[field] = self._maybe_decrypt(env_val)

        file_path = self.secrets_dir / f"{connector_id}.json"
        if file_path.is_file():
            with open(file_path, encoding="utf-8") as fh:
                file_secrets = json.load(fh)
            for k, v in file_secrets.items():
                if isinstance(v, str):
                    secrets[k] = self._maybe_decrypt(v)
                else:
                    secrets[k] = v
        return secrets

    def _maybe_decrypt(self, value: str) -> str:
        if value.startswith("enc:"):
            return self.decrypt_token(value[4:])
        return value

    def get_connector_credentials(
        self,
        connector_id: str,
        project_id: Optional[str] = None,
        token_store: Optional["TokenStore"] = None,
    ) -> Dict[str, Any]:
        """Resolve credentials: project OAuth tokens first, then global secrets."""
        secrets = self.load_connector_secrets(connector_id)
        if project_id and token_store:
            stored = token_store.get_auth_context(project_id, connector_id, self)
            if stored and (stored.access_token or stored.refresh_token):
                merged = dict(secrets)
                if stored.access_token:
                    merged["access_token"] = stored.access_token
                if stored.refresh_token:
                    merged["refresh_token"] = stored.refresh_token
                merged["_project_id"] = project_id
                return merged
        return secrets

    def build_auth_context(
        self,
        connector_id: str,
        project_id: Optional[str] = None,
        token_store: Optional["TokenStore"] = None,
    ) -> AuthContext:
        secrets = self.get_connector_credentials(connector_id, project_id, token_store)
        project = secrets.pop("_project_id", None) or project_id
        extra = {k: v for k, v in secrets.items() if k not in ("access_token", "refresh_token")}
        if project:
            extra["project_id"] = project
        return AuthContext(
            connector_id=connector_id,
            access_token=secrets.get("access_token"),
            refresh_token=secrets.get("refresh_token"),
            extra=extra,
        )

    def register_refresh_hook(
        self,
        connector_id: str,
        hook: Callable[[AuthContext], AuthContext],
    ) -> None:
        self._refresh_hooks[connector_id] = hook

    def refresh_auth_if_needed(self, auth: AuthContext) -> AuthContext:
        if not auth.is_expired():
            return auth
        hook = self._refresh_hooks.get(auth.connector_id)
        if hook:
            return hook(auth)
        return auth

    def sanitize_for_log(self, data: Any) -> Any:
        if isinstance(data, dict):
            return {
                k: ("***REDACTED***" if k.lower() in self.SENSITIVE_KEYS else self.sanitize_for_log(v))
                for k, v in data.items()
            }
        if isinstance(data, list):
            return [self.sanitize_for_log(item) for item in data]
        if isinstance(data, str):
            return self._sanitize_string(data)
        return data

    def _sanitize_string(self, text: str) -> str:
        patterns = [
            (r"(Bearer\s+)[A-Za-z0-9\-._~+/]+=*", r"\1***REDACTED***"),
            (r'("access_token"\s*:\s*")[^"]+(")', r"\1***REDACTED***\2"),
            (r'("refresh_token"\s*:\s*")[^"]+(")', r"\1***REDACTED***\2"),
        ]
        result = text
        for pattern, repl in patterns:
            result = re.sub(pattern, repl, result)
        return result
