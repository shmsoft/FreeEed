"""SQLite-backed connector state persistence."""

from __future__ import annotations

import json
import os
import sqlite3
import threading
from contextlib import contextmanager
from datetime import datetime
from typing import Any, Dict, List, Optional

from collectors.base.types import CircuitBreakerState, CollectionJob, CollectionJobStatus


class StateManager:
    """Persists sync tokens, job state, and circuit breaker status."""

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
                CREATE TABLE IF NOT EXISTS connector_state (
                    connector_id TEXT PRIMARY KEY,
                    last_sync_token TEXT,
                    last_successful_run_at TEXT,
                    failed_item_ids TEXT DEFAULT '[]',
                    circuit_breaker_state TEXT DEFAULT 'closed',
                    circuit_failure_count INTEGER DEFAULT 0,
                    circuit_opened_at TEXT
                );
                CREATE TABLE IF NOT EXISTS collection_jobs (
                    job_id TEXT PRIMARY KEY,
                    connector_id TEXT NOT NULL,
                    status TEXT NOT NULL,
                    created_at TEXT,
                    started_at TEXT,
                    completed_at TEXT,
                    output_dir TEXT,
                    items_collected INTEGER DEFAULT 0,
                    items_failed INTEGER DEFAULT 0,
                    error_message TEXT,
                    auto_chain INTEGER DEFAULT 0,
                    config TEXT DEFAULT '{}'
                );
                CREATE TABLE IF NOT EXISTS schedules (
                    schedule_id TEXT PRIMARY KEY,
                    connector_id TEXT NOT NULL,
                    cron_expression TEXT NOT NULL,
                    auto_chain INTEGER DEFAULT 0,
                    config TEXT DEFAULT '{}',
                    enabled INTEGER DEFAULT 1
                );
                """
            )

    def get_connector_state(self, connector_id: str) -> Dict[str, Any]:
        with self._lock, self._connection() as conn:
            row = conn.execute(
                "SELECT * FROM connector_state WHERE connector_id = ?",
                (connector_id,),
            ).fetchone()
        if not row:
            return {
                "connector_id": connector_id,
                "last_sync_token": None,
                "last_successful_run_at": None,
                "failed_item_ids": [],
                "circuit_breaker_state": CircuitBreakerState.CLOSED.value,
                "circuit_failure_count": 0,
            }
        return {
            "connector_id": row["connector_id"],
            "last_sync_token": row["last_sync_token"],
            "last_successful_run_at": row["last_successful_run_at"],
            "failed_item_ids": json.loads(row["failed_item_ids"] or "[]"),
            "circuit_breaker_state": row["circuit_breaker_state"],
            "circuit_failure_count": row["circuit_failure_count"],
            "circuit_opened_at": row["circuit_opened_at"],
        }

    def update_connector_state(self, connector_id: str, **fields: Any) -> None:
        state = self.get_connector_state(connector_id)
        state.update(fields)
        if "failed_item_ids" in fields and isinstance(fields["failed_item_ids"], list):
            state["failed_item_ids"] = fields["failed_item_ids"]
        with self._lock, self._connection() as conn:
            conn.execute(
                """
                INSERT INTO connector_state (
                    connector_id, last_sync_token, last_successful_run_at,
                    failed_item_ids, circuit_breaker_state, circuit_failure_count,
                    circuit_opened_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(connector_id) DO UPDATE SET
                    last_sync_token = excluded.last_sync_token,
                    last_successful_run_at = excluded.last_successful_run_at,
                    failed_item_ids = excluded.failed_item_ids,
                    circuit_breaker_state = excluded.circuit_breaker_state,
                    circuit_failure_count = excluded.circuit_failure_count,
                    circuit_opened_at = excluded.circuit_opened_at
                """,
                (
                    connector_id,
                    state.get("last_sync_token"),
                    state.get("last_successful_run_at"),
                    json.dumps(state.get("failed_item_ids", [])),
                    state.get("circuit_breaker_state", CircuitBreakerState.CLOSED.value),
                    state.get("circuit_failure_count", 0),
                    state.get("circuit_opened_at"),
                ),
            )

    def record_success(self, connector_id: str, sync_token: Optional[str] = None) -> None:
        self.update_connector_state(
            connector_id,
            last_sync_token=sync_token,
            last_successful_run_at=datetime.utcnow().isoformat(),
            circuit_breaker_state=CircuitBreakerState.CLOSED.value,
            circuit_failure_count=0,
            circuit_opened_at=None,
        )

    def record_failure(self, connector_id: str, threshold: int = 5) -> CircuitBreakerState:
        state = self.get_connector_state(connector_id)
        count = int(state.get("circuit_failure_count", 0)) + 1
        cb_state = CircuitBreakerState.CLOSED
        opened_at = state.get("circuit_opened_at")
        if count >= threshold:
            cb_state = CircuitBreakerState.OPEN
            opened_at = datetime.utcnow().isoformat()
        self.update_connector_state(
            connector_id,
            circuit_failure_count=count,
            circuit_breaker_state=cb_state.value,
            circuit_opened_at=opened_at,
        )
        return cb_state

    def is_circuit_open(self, connector_id: str, cooldown_seconds: int = 300) -> bool:
        state = self.get_connector_state(connector_id)
        if state["circuit_breaker_state"] != CircuitBreakerState.OPEN.value:
            return False
        opened_at = state.get("circuit_opened_at")
        if not opened_at:
            return True
        elapsed = (datetime.utcnow() - datetime.fromisoformat(opened_at)).total_seconds()
        if elapsed >= cooldown_seconds:
            self.update_connector_state(
                connector_id,
                circuit_breaker_state=CircuitBreakerState.HALF_OPEN.value,
            )
            return False
        return True

    def add_failed_item(self, connector_id: str, item_id: str) -> None:
        state = self.get_connector_state(connector_id)
        failed: List[str] = list(state.get("failed_item_ids", []))
        if item_id not in failed:
            failed.append(item_id)
        self.update_connector_state(connector_id, failed_item_ids=failed)

    def save_job(self, job: CollectionJob) -> None:
        with self._lock, self._connection() as conn:
            conn.execute(
                """
                INSERT INTO collection_jobs (
                    job_id, connector_id, status, created_at, started_at,
                    completed_at, output_dir, items_collected, items_failed,
                    error_message, auto_chain, config
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(job_id) DO UPDATE SET
                    status = excluded.status,
                    started_at = excluded.started_at,
                    completed_at = excluded.completed_at,
                    output_dir = excluded.output_dir,
                    items_collected = excluded.items_collected,
                    items_failed = excluded.items_failed,
                    error_message = excluded.error_message,
                    auto_chain = excluded.auto_chain,
                    config = excluded.config
                """,
                (
                    job.job_id,
                    job.connector_id,
                    job.status.value,
                    job.created_at.isoformat() if job.created_at else None,
                    job.started_at.isoformat() if job.started_at else None,
                    job.completed_at.isoformat() if job.completed_at else None,
                    job.output_dir,
                    job.items_collected,
                    job.items_failed,
                    job.error_message,
                    1 if job.auto_chain else 0,
                    json.dumps(job.config),
                ),
            )

    def get_job(self, job_id: str) -> Optional[CollectionJob]:
        with self._lock, self._connection() as conn:
            row = conn.execute(
                "SELECT * FROM collection_jobs WHERE job_id = ?",
                (job_id,),
            ).fetchone()
        if not row:
            return None
        return CollectionJob(
            job_id=row["job_id"],
            connector_id=row["connector_id"],
            status=CollectionJobStatus(row["status"]),
            created_at=_parse_dt(row["created_at"]),
            started_at=_parse_dt(row["started_at"]),
            completed_at=_parse_dt(row["completed_at"]),
            output_dir=row["output_dir"],
            items_collected=row["items_collected"],
            items_failed=row["items_failed"],
            error_message=row["error_message"],
            auto_chain=bool(row["auto_chain"]),
            config=json.loads(row["config"] or "{}"),
        )

    def save_schedule(
        self,
        schedule_id: str,
        connector_id: str,
        cron_expression: str,
        auto_chain: bool = False,
        config: Optional[Dict[str, Any]] = None,
    ) -> None:
        with self._lock, self._connection() as conn:
            conn.execute(
                """
                INSERT INTO schedules (schedule_id, connector_id, cron_expression, auto_chain, config)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(schedule_id) DO UPDATE SET
                    connector_id = excluded.connector_id,
                    cron_expression = excluded.cron_expression,
                    auto_chain = excluded.auto_chain,
                    config = excluded.config
                """,
                (
                    schedule_id,
                    connector_id,
                    cron_expression,
                    1 if auto_chain else 0,
                    json.dumps(config or {}),
                ),
            )

    def list_schedules(self) -> List[Dict[str, Any]]:
        with self._lock, self._connection() as conn:
            rows = conn.execute("SELECT * FROM schedules WHERE enabled = 1").fetchall()
        return [dict(row) for row in rows]


def _parse_dt(value: Optional[str]) -> Optional[datetime]:
    if not value:
        return None
    return datetime.fromisoformat(value)
