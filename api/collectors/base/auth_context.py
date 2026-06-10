"""Authentication context for connector OAuth flows."""

from __future__ import annotations

from dataclasses import dataclass, field
from datetime import datetime
from typing import Any, Dict, Optional


@dataclass
class AuthContext:
    """Holds OAuth tokens and connector-specific auth metadata."""

    connector_id: str
    access_token: Optional[str] = None
    refresh_token: Optional[str] = None
    token_type: str = "Bearer"
    expires_at: Optional[datetime] = None
    scopes: list[str] = field(default_factory=list)
    extra: Dict[str, Any] = field(default_factory=dict)

    def is_expired(self, skew_seconds: int = 60) -> bool:
        if self.expires_at is None:
            return False
        return datetime.utcnow().timestamp() >= (self.expires_at.timestamp() - skew_seconds)

    def authorization_header(self) -> Dict[str, str]:
        if not self.access_token:
            return {}
        return {"Authorization": f"{self.token_type} {self.access_token}"}
