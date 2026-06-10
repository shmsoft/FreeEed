"""Tests for rate limits and circuit breaker behavior."""

from datetime import datetime
from unittest.mock import MagicMock

import pytest

from collectors.base.auth_context import AuthContext
from collectors.base.types import RateLimitInfo
from collectors.core.registry import clear_registry, register_connector
from collectors.core.state_manager import StateManager
from collectors.box.box_collector import BoxCollector


@pytest.fixture(autouse=True)
def reset_registry():
    clear_registry()
    yield
    clear_registry()


def test_box_handle_rate_limits_retry_after():
    auth = AuthContext(connector_id="box", access_token="tok")
    collector = BoxCollector(auth, config={"mock_items": []})
    response = MagicMock()
    response.status_code = 429
    response.headers = {"Retry-After": "30"}
    info = collector.handle_rate_limits(response)
    assert info is not None
    assert info.retry_after_seconds == 30.0


def test_circuit_breaker_opens_after_threshold(tmp_path):
    state = StateManager(db_path=str(tmp_path / "state.db"))
    for _ in range(5):
        state.record_failure("box", threshold=5)
    assert state.is_circuit_open("box") is True


def test_wait_for_rate_limit(monkeypatch):
    auth = AuthContext(connector_id="box", access_token="tok")
    collector = BoxCollector(auth, config={"mock_items": []})
    slept = []

    def fake_sleep(seconds):
        slept.append(seconds)

    monkeypatch.setattr("collectors.base.base_collector.time.sleep", fake_sleep)
    collector.wait_for_rate_limit(RateLimitInfo(retry_after_seconds=15))
    assert slept == [15.0]
