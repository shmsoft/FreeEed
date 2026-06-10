"""Tests for connector registry."""

import pytest

from collectors.base.auth_context import AuthContext
from collectors.base.base_collector import BaseCollector
from collectors.base.types import CollectionItem, ItemMetadata, RateLimitInfo
from collectors.core.registry import clear_registry, get_connector_class, list_connectors, register_connector


@pytest.fixture(autouse=True)
def reset_registry():
    clear_registry()
    yield
    clear_registry()


class DummyCollector(BaseCollector):
    @property
    def connector_id(self) -> str:
        return "dummy"

    def authenticate(self):
        return self.auth

    def list_items(self, since=None):
        return []

    def fetch_item(self, item_id: str):
        return CollectionItem(metadata=ItemMetadata(item_id=item_id, name="x"), content=b"")

    def stream_changes(self):
        yield from []

    def handle_rate_limits(self, response):
        return None

    def serialize_metadata(self, metadata):
        return {"id": metadata.item_id}


def test_register_and_lookup():
    registered = register_connector("dummy")(DummyCollector)
    assert get_connector_class("dummy") is registered
    assert "dummy" in list_connectors()


def test_duplicate_registration_raises():
    register_connector("dummy")(DummyCollector)
    with pytest.raises(ValueError):
        register_connector("dummy")(DummyCollector)


def test_unknown_connector_raises():
    with pytest.raises(KeyError):
        get_connector_class("missing")
