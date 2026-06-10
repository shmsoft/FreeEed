"""Persist per-project browse selections (not downloaded files)."""

from __future__ import annotations

import json
import os
import sqlite3
import threading
from contextlib import contextmanager
from datetime import datetime
from typing import Any, Dict, List, Optional

from collectors.base.types import BrowseItemType, SelectedItem


class SelectionStore:
    """SQLite store keyed by (project_id, connector_id)."""

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
            conn.execute(
                """
                CREATE TABLE IF NOT EXISTS project_selections (
                    project_id TEXT NOT NULL,
                    connector_id TEXT NOT NULL,
                    items TEXT NOT NULL DEFAULT '[]',
                    updated_at TEXT,
                    PRIMARY KEY (project_id, connector_id)
                )
                """
            )

    def save_selections(
        self,
        project_id: str,
        connector_id: str,
        items: List[SelectedItem],
    ) -> Dict[str, Any]:
        payload = [self._item_to_dict(item) for item in items]
        updated_at = datetime.utcnow().isoformat()
        with self._lock, self._connection() as conn:
            conn.execute(
                """
                INSERT INTO project_selections (project_id, connector_id, items, updated_at)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(project_id, connector_id) DO UPDATE SET
                    items = excluded.items,
                    updated_at = excluded.updated_at
                """,
                (project_id, connector_id, json.dumps(payload), updated_at),
            )
        return {
            "project_id": project_id,
            "connector_id": connector_id,
            "items": payload,
            "updated_at": updated_at,
        }

    def get_selections(
        self,
        project_id: str,
        connector_id: Optional[str] = None,
    ) -> List[Dict[str, Any]]:
        with self._lock, self._connection() as conn:
            if connector_id:
                rows = conn.execute(
                    "SELECT * FROM project_selections WHERE project_id = ? AND connector_id = ?",
                    (project_id, connector_id),
                ).fetchall()
            else:
                rows = conn.execute(
                    "SELECT * FROM project_selections WHERE project_id = ?",
                    (project_id,),
                ).fetchall()
        return [self._row_to_dict(row) for row in rows]

    def get_selected_items(
        self,
        project_id: str,
        connector_id: str,
    ) -> List[SelectedItem]:
        rows = self.get_selections(project_id, connector_id)
        if not rows:
            return []
        return [self._dict_to_item(entry) for entry in rows[0]["items"]]

    @staticmethod
    def _item_to_dict(item: SelectedItem) -> Dict[str, Any]:
        return {
            "id": item.id,
            "type": item.type.value if isinstance(item.type, BrowseItemType) else item.type,
            "name": item.name,
            "path": item.path,
        }

    @staticmethod
    def _dict_to_item(data: Dict[str, Any]) -> SelectedItem:
        item_type = data.get("type", "file")
        if isinstance(item_type, BrowseItemType):
            parsed_type = item_type
        else:
            parsed_type = BrowseItemType(item_type)
        return SelectedItem(
            id=str(data["id"]),
            type=parsed_type,
            name=str(data.get("name", "")),
            path=str(data.get("path", "")),
        )

    @staticmethod
    def _row_to_dict(row: sqlite3.Row) -> Dict[str, Any]:
        return {
            "project_id": row["project_id"],
            "connector_id": row["connector_id"],
            "items": json.loads(row["items"] or "[]"),
            "updated_at": row["updated_at"],
        }
