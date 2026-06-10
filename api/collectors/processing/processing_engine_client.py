"""Client for chaining collection to FreeEed Java processing."""

from __future__ import annotations

import os
import threading
from pathlib import Path
from typing import Any, Dict, Optional


class ProcessingEngineClient:
    """Generate .project files and invoke run_freeeed_process."""

    _lock = threading.Lock()
    _busy = False

    DEFAULT_SOLR = os.getenv("SOLR_ENDPOINT", "http://solr:8983")
    DEFAULT_OUTPUT = os.getenv("COLLECTOR_PROCESSING_OUTPUT", "/data/output")

    def is_busy(self) -> bool:
        return self._busy

    def trigger_processing(
        self,
        job_id: str,
        input_dir: str,
        project_config: Optional[Dict[str, Any]] = None,
    ) -> str:
        with self._lock:
            if self._busy:
                raise RuntimeError("Processing engine is already running")
            self._busy = True

        try:
            config = project_config or {}
            project_path = self.write_project_file(job_id, input_dir, config)
            self._run_java(project_path)
            return project_path
        finally:
            with self._lock:
                self._busy = False

    def write_project_file(
        self,
        job_id: str,
        input_dir: str,
        config: Dict[str, Any],
    ) -> str:
        """Write a Java-canonical .project file."""
        projects_dir = Path(config.get("projects_dir", "/data/input/collections/projects"))
        projects_dir.mkdir(parents=True, exist_ok=True)
        project_path = projects_dir / f"{job_id}.project"

        project_name = config.get("project_name", f"collection-{job_id[:8]}")
        output_dir = config.get("output_dir", self.DEFAULT_OUTPUT)
        custodian = config.get("custodian", "collector")
        if isinstance(custodian, list):
            custodian = ",".join(custodian)
        solr_endpoint = config.get("solr_endpoint", self.DEFAULT_SOLR)
        processing_engine = config.get("processing_engine", "Standard")
        stage = str(config.get("stage", True)).lower()
        process_where = config.get("process_where", "local")

        lines = [
            f"project-name={project_name}",
            f"input={input_dir}",
            f"output-dir={output_dir}",
            f"custodian={custodian}",
            f"stage={stage}",
            f"solr_endpoint={solr_endpoint}",
            f"processing_engine={processing_engine}",
            f"process-where={process_where}",
            "metadata=standard",
            "file-system=local",
            "send_index_solr_enabled=true",
        ]

        optional_keys = {
            "project-code": "project_code",
            "staging-dir": "staging_dir",
            "preview": "preview",
            "ocr_enabled": "ocr_enabled",
        }
        for java_key, cfg_key in optional_keys.items():
            if cfg_key in config:
                val = config[cfg_key]
                if isinstance(val, bool):
                    val = str(val).lower()
                lines.append(f"{java_key}={val}")

        content = "\n".join(lines) + "\n"
        temp_path = project_path.with_suffix(".project.tmp")
        temp_path.write_text(content, encoding="utf-8")
        temp_path.replace(project_path)
        return str(project_path)

    def _run_java(self, project_path: str) -> None:
        import main as api_main

        api_main.run_freeeed_process(project_path)
