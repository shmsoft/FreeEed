"""Manual and scheduled collection triggers."""

from __future__ import annotations

import logging
import threading
import uuid
from datetime import datetime
from typing import Any, Callable, Dict, Optional

from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.triggers.cron import CronTrigger

from collectors.base.types import CollectionJob, CollectionJobStatus
from collectors.core.logging_manager import LoggingManager
from collectors.core.state_manager import StateManager


class TriggerManager:
    """Handles manual triggers and APScheduler cron jobs."""

    def __init__(
        self,
        state_manager: StateManager,
        run_callback: Callable[[CollectionJob], None],
    ):
        self.state = state_manager
        self.run_callback = run_callback
        self.logger = LoggingManager.configure()
        self._scheduler = BackgroundScheduler()
        self._scheduler.start()
        self._shutdown = threading.Event()

    def trigger_manual(
        self,
        connector_id: str,
        auto_chain: bool = False,
        config: Optional[Dict[str, Any]] = None,
    ) -> CollectionJob:
        job = CollectionJob(
            job_id=str(uuid.uuid4()),
            connector_id=connector_id,
            status=CollectionJobStatus.PENDING,
            created_at=datetime.utcnow(),
            auto_chain=auto_chain,
            config=config or {},
        )
        self.state.save_job(job)
        if not self._shutdown.is_set():
            thread = threading.Thread(target=self._execute_job, args=(job,), daemon=True)
            thread.start()
        return job

    def _execute_job(self, job: CollectionJob) -> None:
        try:
            self.run_callback(job)
        except Exception as exc:
            LoggingManager.log_event(
                self.logger,
                "trigger_job_failed",
                level=logging.ERROR,
                job_id=job.job_id,
                error=str(exc),
            )

    def add_schedule(
        self,
        schedule_id: str,
        connector_id: str,
        cron_expression: str,
        auto_chain: bool = False,
        config: Optional[Dict[str, Any]] = None,
    ) -> None:
        parts = cron_expression.split()
        if len(parts) != 5:
            raise ValueError("Cron expression must have 5 fields: min hour dom month dow")
        minute, hour, day, month, day_of_week = parts
        trigger = CronTrigger(
            minute=minute,
            hour=hour,
            day=day,
            month=month,
            day_of_week=day_of_week,
        )
        self.state.save_schedule(schedule_id, connector_id, cron_expression, auto_chain, config)

        def _scheduled_run() -> None:
            if self._shutdown.is_set():
                return
            self.trigger_manual(connector_id, auto_chain=auto_chain, config=config)

        self._scheduler.add_job(
            _scheduled_run,
            trigger=trigger,
            id=schedule_id,
            replace_existing=True,
        )
        LoggingManager.log_event(
            self.logger,
            "schedule_registered",
            schedule_id=schedule_id,
            connector_id=connector_id,
            cron=cron_expression,
        )

    def load_schedules_from_db(self) -> None:
        for row in self.state.list_schedules():
            self.add_schedule(
                schedule_id=row["schedule_id"],
                connector_id=row["connector_id"],
                cron_expression=row["cron_expression"],
                auto_chain=bool(row["auto_chain"]),
                config=__import__("json").loads(row["config"] or "{}"),
            )

    def shutdown(self) -> None:
        self._shutdown.set()
        if self._scheduler.running:
            self._scheduler.shutdown(wait=False)
