"""SQLite metadata store."""

from __future__ import annotations

import json
import os
import sqlite3
import threading
from contextlib import contextmanager
from typing import Any, Dict


class SQLiteMetadataStore:
    """Persist item metadata in SQLite for a collection job."""

    def __init__(self, db_path: str):
        self.db_path = db_path
        self._lock = threading.Lock()
        os.makedirs(os.path.dirname(db_path), exist_ok=True)
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
        with self._lock, self._connection() as conn:
            conn.executescript(
                """
                CREATE TABLE IF NOT EXISTS items (
                    item_id TEXT PRIMARY KEY,
                    name TEXT,
                    mime_type TEXT,
                    size_bytes INTEGER,
                    modified_at TEXT,
                    source_path TEXT,
                    checksum TEXT,
                    local_path TEXT,
                    connector_id TEXT
                );
                CREATE TABLE IF NOT EXISTS raw_metadata (
                    item_id TEXT PRIMARY KEY,
                    data TEXT
                );
                CREATE INDEX IF NOT EXISTS idx_items_checksum ON items(checksum);
                """
            )

    def save(self, record: Dict[str, Any]) -> None:
        with self._lock, self._connection() as conn:
            conn.execute(
                """
                INSERT INTO items (
                    item_id, name, mime_type, size_bytes, modified_at,
                    source_path, checksum, local_path, connector_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(item_id) DO UPDATE SET
                    name = excluded.name,
                    checksum = excluded.checksum,
                    local_path = excluded.local_path
                """,
                (
                    record["item_id"],
                    record.get("name"),
                    record.get("mime_type"),
                    record.get("size_bytes"),
                    record.get("modified_at"),
                    record.get("source_path"),
                    record.get("checksum"),
                    record.get("local_path"),
                    record.get("connector_id"),
                ),
            )

    def save_raw(self, item_id: str, raw: Dict[str, Any]) -> None:
        with self._lock, self._connection() as conn:
            conn.execute(
                """
                INSERT INTO raw_metadata (item_id, data) VALUES (?, ?)
                ON CONFLICT(item_id) DO UPDATE SET data = excluded.data
                """,
                (item_id, json.dumps(raw)),
            )

    def exists(self, item_id: str, checksum: str) -> bool:
        with self._lock, self._connection() as conn:
            row = conn.execute(
                "SELECT 1 FROM items WHERE item_id = ? AND checksum = ?",
                (item_id, checksum),
            ).fetchone()
        return row is not None
