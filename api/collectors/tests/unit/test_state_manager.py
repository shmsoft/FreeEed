"""Tests for StateManager."""

import os
import tempfile

import pytest

from collectors.base.types import CollectionJob, CollectionJobStatus, CircuitBreakerState
from collectors.core.state_manager import StateManager


@pytest.fixture
def state_db():
    with tempfile.TemporaryDirectory() as tmp:
        yield StateManager(db_path=os.path.join(tmp, "state.db"))


def test_connector_state_defaults(state_db):
    state = state_db.get_connector_state("box")
    assert state["connector_id"] == "box"
    assert state["circuit_breaker_state"] == CircuitBreakerState.CLOSED.value


def test_record_success_resets_circuit(state_db):
    state_db.record_failure("box", threshold=1)
    assert state_db.get_connector_state("box")["circuit_breaker_state"] == CircuitBreakerState.OPEN.value
    state_db.record_success("box", sync_token="token-1")
    updated = state_db.get_connector_state("box")
    assert updated["last_sync_token"] == "token-1"
    assert updated["circuit_breaker_state"] == CircuitBreakerState.CLOSED.value


def test_failed_items_tracking(state_db):
    state_db.add_failed_item("box", "file-1")
    state_db.add_failed_item("box", "file-1")
    failed = state_db.get_connector_state("box")["failed_item_ids"]
    assert failed == ["file-1"]


def test_job_persistence(state_db):
    job = CollectionJob(
        job_id="job-1",
        connector_id="box",
        status=CollectionJobStatus.COMPLETED,
        items_collected=3,
    )
    state_db.save_job(job)
    loaded = state_db.get_job("job-1")
    assert loaded is not None
    assert loaded.items_collected == 3
    assert loaded.status == CollectionJobStatus.COMPLETED
