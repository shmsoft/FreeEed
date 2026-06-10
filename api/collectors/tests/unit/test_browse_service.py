"""Browse normalization and credential handling tests."""

from unittest.mock import MagicMock, patch

import pytest

from collectors.base.auth_context import AuthContext
from collectors.base.types import ConnectorHealth
from collectors.box.box_collector import BoxCollector
from collectors.core.browse_service import (
    BrowseService,
    ConnectorAuthError,
    CredentialsNotConfiguredError,
)


MOCK_BROWSE = {
    "0": [
        {"id": "folder-1", "name": "Docs", "path": "/Docs", "type": "folder"},
        {"id": "file-1", "name": "readme.txt", "path": "/readme.txt", "type": "file", "size": 42},
    ],
    "folder-1": [
        {"id": "file-2", "name": "nested.pdf", "path": "/Docs/nested.pdf", "type": "file", "size": 100},
    ],
}


def test_box_list_children_mock():
    auth = AuthContext(connector_id="box", access_token="dev")
    collector = BoxCollector(
        auth=auth,
        config={"mock_browse": MOCK_BROWSE, "mock_refresh": True},
    )
    root = collector.list_children()
    assert len(root) == 2
    assert root[0].type.value == "folder"
    assert root[0].connector_id == "box"

    children = collector.list_children("folder-1")
    assert len(children) == 1
    assert children[0].name == "nested.pdf"


def test_browse_service_serialize():
    auth = AuthContext(connector_id="box", access_token="dev")
    collector = BoxCollector(auth=auth, config={"mock_browse": MOCK_BROWSE})
    items = collector.list_children()
    serialized = BrowseService.serialize_browse_item(items[0])
    assert serialized["type"] == "folder"
    assert serialized["connector_id"] == "box"
    assert "modified_at" in serialized


def test_browse_service_missing_credentials(tmp_path, monkeypatch):
    monkeypatch.setenv("COLLECTOR_SECRETS_DIR", str(tmp_path))
    service = BrowseService()
    with pytest.raises(CredentialsNotConfiguredError):
        service.browse("box")


def test_browse_service_connect_with_override():
    service = BrowseService()
    result = service.connect(
        connector_id="box",
        token_override={"access_token": "dev"},
        config={"mock_refresh": True, "mock_browse": MOCK_BROWSE},
    )
    assert result["connected"] is True


@patch.object(BrowseService, "_build_collector")
def test_browse_service_auth_failure(mock_build):
    mock_collector = MagicMock()
    mock_collector.health_check.return_value = ConnectorHealth(
        connector_id="box",
        healthy=False,
        message="Authentication failed",
    )
    mock_build.return_value = mock_collector
    service = BrowseService()
    with pytest.raises(ConnectorAuthError):
        service.connect(
            connector_id="box",
            token_override={"access_token": "invalid"},
        )
