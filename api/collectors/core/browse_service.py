"""Project-scoped connect and browse operations."""

from __future__ import annotations

from typing import Any, Dict, List, Optional

from collectors.base.auth_context import AuthContext
from collectors.base.types import BrowseItem
from collectors.core.registry import get_connector_class
from collectors.core.security_manager import SecurityManager
from collectors.core.token_store import TokenStore


class CredentialsNotConfiguredError(Exception):
    """Raised when connector secrets are missing."""


class ConnectorAuthError(Exception):
    """Raised when authentication fails."""


class BrowseService:
    """Build collectors and expose remote folder browsing."""

    def __init__(
        self,
        security_manager: Optional[SecurityManager] = None,
        token_store: Optional[TokenStore] = None,
    ):
        self.security = security_manager or SecurityManager()
        self.token_store = token_store or TokenStore()

    def has_credentials(self, connector_id: str, project_id: Optional[str] = None) -> bool:
        if project_id and self.token_store.has_tokens(project_id, connector_id):
            return True
        secrets = self.security.load_connector_secrets(connector_id)
        return bool(secrets.get("access_token") or secrets.get("refresh_token"))

    @staticmethod
    def _has_token_override(token_override: Optional[Dict[str, Any]]) -> bool:
        if not token_override:
            return False
        return bool(token_override.get("access_token") or token_override.get("refresh_token"))

    def connect(
        self,
        connector_id: str,
        project_id: Optional[str] = None,
        token_override: Optional[Dict[str, Any]] = None,
        config: Optional[Dict[str, Any]] = None,
    ) -> Dict[str, Any]:
        if not self._has_token_override(token_override) and not self.has_credentials(
            connector_id, project_id
        ):
            raise CredentialsNotConfiguredError(
                f"No credentials configured for connector '{connector_id}'. "
                f"Complete OAuth at /collect/oauth/{connector_id}/authorize?project_id=YOUR_PROJECT "
                f"or set COLLECTOR_{connector_id.upper()}_ACCESS_TOKEN / "
                f"./secrets/{connector_id}.json"
            )

        auth = self._build_auth(connector_id, token_override, project_id)
        collector = self._build_collector(connector_id, auth, config)
        health = collector.health_check()
        if not health.healthy:
            raise ConnectorAuthError(health.message)
        return {
            "connector_id": connector_id,
            "connected": True,
            "message": health.message,
        }

    def browse(
        self,
        connector_id: str,
        parent_id: Optional[str] = None,
        project_id: Optional[str] = None,
        token_override: Optional[Dict[str, Any]] = None,
        config: Optional[Dict[str, Any]] = None,
    ) -> List[BrowseItem]:
        if not self._has_token_override(token_override) and not self.has_credentials(
            connector_id, project_id
        ):
            raise CredentialsNotConfiguredError(
                f"No credentials configured for connector '{connector_id}'. "
                f"Complete OAuth at /collect/oauth/{connector_id}/authorize?project_id=YOUR_PROJECT"
            )

        auth = self._build_auth(connector_id, token_override, project_id)
        collector = self._build_collector(connector_id, auth, config)
        return collector.list_children(parent_id)

    def _build_auth(
        self,
        connector_id: str,
        token_override: Optional[Dict[str, Any]] = None,
        project_id: Optional[str] = None,
    ) -> AuthContext:
        if token_override:
            auth = AuthContext(
                connector_id=connector_id,
                access_token=token_override.get("access_token"),
                refresh_token=token_override.get("refresh_token"),
                extra={
                    k: v
                    for k, v in token_override.items()
                    if k not in ("access_token", "refresh_token")
                },
            )
            if project_id:
                auth.extra["project_id"] = project_id
        else:
            auth = self.security.build_auth_context(
                connector_id,
                project_id=project_id,
                token_store=self.token_store,
            )
        prev_access = auth.access_token
        refreshed = self.security.refresh_auth_if_needed(auth)
        resolved_project = project_id or refreshed.extra.get("project_id")
        if resolved_project and refreshed.access_token and refreshed.access_token != prev_access:
            self.token_store.save_auth_context(
                resolved_project, connector_id, refreshed, self.security
            )
        return refreshed

    def _build_collector(
        self,
        connector_id: str,
        auth: AuthContext,
        config: Optional[Dict[str, Any]] = None,
    ):
        connector_cls = get_connector_class(connector_id)
        merged_config = dict(config or {})
        secrets = self.security.load_connector_secrets(connector_id)
        for key in ("client_id", "client_secret"):
            if key in secrets and key not in merged_config:
                merged_config[key] = secrets[key]
        return connector_cls(auth=auth, config=merged_config)

    @staticmethod
    def serialize_browse_item(item: BrowseItem) -> Dict[str, Any]:
        return {
            "id": item.id,
            "name": item.name,
            "path": item.path,
            "type": item.type.value if hasattr(item.type, "value") else item.type,
            "size": item.size,
            "modified_at": item.modified_at.isoformat() if item.modified_at else None,
            "connector_id": item.connector_id,
            "mime_type": item.mime_type,
        }
