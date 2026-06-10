"""Project-scoped OAuth token persistence."""

from __future__ import annotations

import json
import os
import sqlite3
import threading
from contextlib import contextmanager
from datetime import datetime
from typing import Any, Dict, Optional, TYPE_CHECKING

from collectors.base.auth_context import AuthContext

if TYPE_CHECKING:
    from collectors.core.security_manager import SecurityManager


class TokenStore:
    """SQLite store for OAuth tokens keyed by (project_id, connector_id)."""

    def __init__(self, db_path: Optional[str] = None):
        self.db_path = db_path or os.getenv("COLLECTOR_STATE_DB", "/app/db/collector_state.db")
        self._lock = threading.Lock()
        self._init_db()

    @contextmanager
    def _connection(self):
        conn = sqlite3.connect(self.db_path, check_same_thread=False)
        conn.row_factory = sqlite3.Row
        try:
            yield conn
            conn.commit()
        finally:
            conn.close()

    def _init_db(self) -> None:
        os.makedirs(os.path.dirname(self.db_path), exist_ok=True)
        with self._lock, self._connection() as conn:
            conn.executescript(
                """
                CREATE TABLE IF NOT EXISTS oauth_tokens (
                    project_id TEXT NOT NULL,
                    connector_id TEXT NOT NULL,
                    access_token TEXT,
                    refresh_token TEXT,
                    token_type TEXT DEFAULT 'Bearer',
                    expires_at TEXT,
                    scopes TEXT DEFAULT '[]',
                    updated_at TEXT,
                    PRIMARY KEY (project_id, connector_id)
                );
                CREATE TABLE IF NOT EXISTS oauth_states (
                    state TEXT PRIMARY KEY,
                    project_id TEXT NOT NULL,
                    connector_id TEXT NOT NULL,
                    code_verifier TEXT NOT NULL,
                    redirect_after TEXT,
                    expires_at TEXT NOT NULL,
                    created_at TEXT NOT NULL
                );
                """
            )

    def save_auth_context(
        self,
        project_id: str,
        connector_id: str,
        auth: AuthContext,
        security: SecurityManager,
    ) -> None:
        access = self._encrypt_field(auth.access_token, security)
        refresh = self._encrypt_field(auth.refresh_token, security)
        updated_at = datetime.utcnow().isoformat()
        with self._lock, self._connection() as conn:
            conn.execute(
                """
                INSERT INTO oauth_tokens (
                    project_id, connector_id, access_token, refresh_token,
                    token_type, expires_at, scopes, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(project_id, connector_id) DO UPDATE SET
                    access_token = excluded.access_token,
                    refresh_token = excluded.refresh_token,
                    token_type = excluded.token_type,
                    expires_at = excluded.expires_at,
                    scopes = excluded.scopes,
                    updated_at = excluded.updated_at
                """,
                (
                    project_id,
                    connector_id,
                    access,
                    refresh,
                    auth.token_type,
                    auth.expires_at.isoformat() if auth.expires_at else None,
                    json.dumps(auth.scopes),
                    updated_at,
                ),
            )

    def get_auth_context(
        self,
        project_id: str,
        connector_id: str,
        security: SecurityManager,
    ) -> Optional[AuthContext]:
        with self._lock, self._connection() as conn:
            row = conn.execute(
                """
                SELECT * FROM oauth_tokens
                WHERE project_id = ? AND connector_id = ?
                """,
                (project_id, connector_id),
            ).fetchone()
        if not row:
            return None
        access = self._decrypt_field(row["access_token"], security)
        refresh = self._decrypt_field(row["refresh_token"], security)
        if not access and not refresh:
            return None
        expires_at = None
        if row["expires_at"]:
            expires_at = datetime.fromisoformat(row["expires_at"])
        scopes = json.loads(row["scopes"] or "[]")
        return AuthContext(
            connector_id=connector_id,
            access_token=access,
            refresh_token=refresh,
            token_type=row["token_type"] or "Bearer",
            expires_at=expires_at,
            scopes=scopes,
            extra={"project_id": project_id},
        )

    def has_tokens(self, project_id: str, connector_id: str) -> bool:
        with self._lock, self._connection() as conn:
            row = conn.execute(
                """
                SELECT access_token, refresh_token FROM oauth_tokens
                WHERE project_id = ? AND connector_id = ?
                """,
                (project_id, connector_id),
            ).fetchone()
        if not row:
            return False
        return bool(row["access_token"] or row["refresh_token"])

    def save_oauth_state(
        self,
        state: str,
        project_id: str,
        connector_id: str,
        code_verifier: str,
        expires_at: datetime,
        redirect_after: Optional[str] = None,
    ) -> None:
        now = datetime.utcnow().isoformat()
        with self._lock, self._connection() as conn:
            conn.execute(
                """
                INSERT INTO oauth_states (
                    state, project_id, connector_id, code_verifier,
                    redirect_after, expires_at, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                (
                    state,
                    project_id,
                    connector_id,
                    code_verifier,
                    redirect_after,
                    expires_at.isoformat(),
                    now,
                ),
            )

    def consume_oauth_state(self, state: str, connector_id: str) -> Optional[Dict[str, Any]]:
        with self._lock, self._connection() as conn:
            row = conn.execute(
                "SELECT * FROM oauth_states WHERE state = ? AND connector_id = ?",
                (state, connector_id),
            ).fetchone()
            if not row:
                return None
            conn.execute("DELETE FROM oauth_states WHERE state = ?", (state,))
        expires_at = datetime.fromisoformat(row["expires_at"])
        if datetime.utcnow() > expires_at:
            return None
        return {
            "project_id": row["project_id"],
            "connector_id": row["connector_id"],
            "code_verifier": row["code_verifier"],
            "redirect_after": row["redirect_after"],
        }

    @staticmethod
    def _encrypt_field(value: Optional[str], security: SecurityManager) -> Optional[str]:
        if not value:
            return None
        encrypted = security.encrypt_token(value)
        if encrypted != value:
            return f"enc:{encrypted}"
        return value

    @staticmethod
    def _decrypt_field(value: Optional[str], security: SecurityManager) -> Optional[str]:
        if not value:
            return None
        if value.startswith("enc:"):
            return security.decrypt_token(value[4:])
        return value
