"""End-to-end integration tests with mock connectors."""

import os
import tempfile
from unittest.mock import patch

import pytest

from collectors.base.auth_context import AuthContext
from collectors.core.orchestrator import Orchestrator
from collectors.core.state_manager import StateManager
from collectors.base.types import CollectionJob, CollectionJobStatus
from collectors.processing.processing_engine_client import ProcessingEngineClient

import collectors.box.box_collector  # noqa: F401
import collectors.google_drive.gdrive_collector  # noqa: F401
import collectors.dropbox.dropbox_collector  # noqa: F401


MOCK_ITEMS = [
    {"id": "f1", "name": "doc1.txt", "content": "hello", "modified_at": "2024-06-01T12:00:00"},
    {"id": "f2", "name": "doc2.txt", "content": "world", "modified_at": "2024-06-01T12:00:00"},
]


@pytest.fixture
def orchestrator(tmp_path, monkeypatch):
    monkeypatch.setenv("COLLECTOR_BOX_ACCESS_TOKEN", "mock-token")
    state = StateManager(db_path=str(tmp_path / "state.db"))
    orch = Orchestrator(
        state_manager=state,
        base_output_dir=str(tmp_path / "collections"),
    )
    return orch, state, tmp_path


def test_end_to_end_mock_collection(orchestrator):
    orch, state, tmp_path = orchestrator
    job = CollectionJob(
        job_id="integration-job-1",
        connector_id="box",
        status=CollectionJobStatus.PENDING,
        auto_chain=False,
        config={"mock_items": MOCK_ITEMS, "use_stream": False},
    )
    result = orch.run_collection(job)
    assert result.status == CollectionJobStatus.COMPLETED
    assert result.items_collected == 2
    output_dir = tmp_path / "collections" / "integration-job-1"
    assert (output_dir / "doc1.txt").exists()
    assert (output_dir / "doc2.txt").exists()


def test_project_file_java_canonical_keys(tmp_path):
    client = ProcessingEngineClient()
    project_path = client.write_project_file(
        job_id="test-job",
        input_dir="/data/input/collections/test-job",
        config={
            "project_name": "Test Collection",
            "custodian": "c1,c2",
            "output_dir": "/data/output",
            "solr_endpoint": "http://solr:8983",
        },
    )
    content = open(project_path, encoding="utf-8").read()
    assert "input=/data/input/collections/test-job" in content
    assert "input-dir=" not in content
    assert "custodian=c1,c2" in content
    assert "stage=true" in content
    assert "processing_engine=Standard" in content
    assert "solr_endpoint=http://solr:8983" in content


@pytest.mark.skipif(
    not os.getenv("BOX_ACCESS_TOKEN") and not os.getenv("COLLECTOR_BOX_ACCESS_TOKEN"),
    reason="No live Box credentials",
)
def test_live_box_skipped_without_credentials():
    pytest.skip("Live connector test requires credentials")
