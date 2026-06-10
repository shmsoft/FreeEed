"""Collection run orchestration."""

from __future__ import annotations

import logging
import os
from datetime import datetime
from typing import Any, Dict, Optional

from collectors.base.auth_context import AuthContext
from collectors.base.types import CollectionJob, CollectionJobStatus
from collectors.core.logging_manager import LoggingManager
from collectors.core.registry import get_connector_class
from collectors.core.security_manager import SecurityManager
from collectors.core.state_manager import StateManager
from collectors.ingestion.ingestion_pipeline import IngestionPipeline
from collectors.processing.processing_engine_client import ProcessingEngineClient
from collectors.storage.filesystem_store import FilesystemStore
from collectors.storage.sqlite_store import SQLiteMetadataStore


class Orchestrator:
    """Coordinates connector auth, ingestion, and optional processing."""

    def __init__(
        self,
        state_manager: Optional[StateManager] = None,
        security_manager: Optional[SecurityManager] = None,
        base_output_dir: Optional[str] = None,
    ):
        self.state = state_manager or StateManager()
        self.security = security_manager or SecurityManager()
        self.logger = LoggingManager.configure()
        self.base_output_dir = base_output_dir or os.getenv(
            "COLLECTOR_OUTPUT_BASE", "/data/input/collections"
        )
        self.processing_client = ProcessingEngineClient()

    def run_collection(self, job: CollectionJob) -> CollectionJob:
        connector_id = job.connector_id
        if self.state.is_circuit_open(connector_id):
            job.status = CollectionJobStatus.FAILED
            job.error_message = "Circuit breaker open for connector"
            job.completed_at = datetime.utcnow()
            self.state.save_job(job)
            return job

        job.status = CollectionJobStatus.RUNNING
        job.started_at = datetime.utcnow()
        output_dir = os.path.join(self.base_output_dir, job.job_id)
        job.output_dir = output_dir
        self.state.save_job(job)

        try:
            auth = self.security.build_auth_context(connector_id)
            auth = self.security.refresh_auth_if_needed(auth)
            connector_cls = get_connector_class(connector_id)
            collector = connector_cls(auth=auth, config=job.config)

            health = collector.health_check()
            if not health.healthy:
                raise RuntimeError(f"Connector unhealthy: {health.message}")

            filesystem = FilesystemStore(output_dir)
            metadata_store = SQLiteMetadataStore(
                db_path=os.path.join(output_dir, "metadata.db")
            )
            pipeline = IngestionPipeline(
                collector=collector,
                filesystem=filesystem,
                metadata_store=metadata_store,
                state_manager=self.state,
                logger=self.logger,
            )

            since = self._parse_since(job.config)
            result = pipeline.run(since=since)

            job.items_collected = result["items_collected"]
            job.items_failed = result["items_failed"]
            job.status = CollectionJobStatus.COMPLETED
            job.completed_at = datetime.utcnow()
            self.state.record_success(connector_id, sync_token=result.get("sync_token"))
            self.state.save_job(job)

            if job.auto_chain:
                self._chain_processing(job, output_dir)

            LoggingManager.log_event(
                self.logger,
                "collection_completed",
                job_id=job.job_id,
                connector_id=connector_id,
                items_collected=job.items_collected,
            )
        except Exception as exc:
            job.status = CollectionJobStatus.FAILED
            job.error_message = str(exc)
            job.completed_at = datetime.utcnow()
            self.state.record_failure(connector_id)
            self.state.save_job(job)
            LoggingManager.log_event(
                self.logger,
                "collection_failed",
                level=logging.ERROR,
                job_id=job.job_id,
                connector_id=connector_id,
                error=str(exc),
            )

        return job

    def _chain_processing(self, job: CollectionJob, input_dir: str) -> None:
        project_config = job.config.get("processing", {})
        self.processing_client.trigger_processing(
            job_id=job.job_id,
            input_dir=input_dir,
            project_config=project_config,
        )

    def _parse_since(self, config: Dict[str, Any]) -> Optional[datetime]:
        since_str = config.get("since")
        if not since_str:
            return None
        return datetime.fromisoformat(since_str)

    def get_connector_health(self, connector_id: str) -> Dict[str, Any]:
        try:
            auth = self.security.build_auth_context(connector_id)
            connector_cls = get_connector_class(connector_id)
            collector = connector_cls(auth=auth)
            health = collector.health_check()
            state = self.state.get_connector_state(connector_id)
            return {
                "connector_id": connector_id,
                "healthy": health.healthy,
                "message": health.message,
                "circuit_breaker": state.get("circuit_breaker_state"),
                "last_successful_run_at": state.get("last_successful_run_at"),
            }
        except Exception as exc:
            return {
                "connector_id": connector_id,
                "healthy": False,
                "message": str(exc),
            }

    def subsystem_status(self) -> Dict[str, Any]:
        from collectors.core.registry import list_connectors

        connectors = list_connectors()
        open_circuits = [
            cid
            for cid in connectors
            if self.state.is_circuit_open(cid, cooldown_seconds=0)
            and self.state.get_connector_state(cid)["circuit_breaker_state"] == "open"
        ]
        return {
            "status": "healthy" if not open_circuits else "degraded",
            "registered_connectors": connectors,
            "open_circuits": open_circuits,
            "processing_busy": self.processing_client.is_busy(),
        }
