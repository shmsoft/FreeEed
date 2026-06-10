"""Collect-only-selected ingestion tests."""

from datetime import datetime
from unittest.mock import MagicMock

from collectors.base.auth_context import AuthContext
from collectors.base.types import BrowseItemType, SelectedItem
from collectors.box.box_collector import BoxCollector
from collectors.ingestion.ingestion_pipeline import IngestionPipeline


MOCK_BROWSE = {
    "0": [
        {"id": "folder-1", "name": "Docs", "path": "/Docs", "type": "folder"},
        {"id": "file-1", "name": "root.txt", "path": "/root.txt", "type": "file", "content": "root"},
    ],
    "folder-1": [
        {"id": "file-2", "name": "nested.pdf", "path": "/Docs/nested.pdf", "type": "file", "content": "nested"},
    ],
}

MOCK_ITEMS = [
    {"id": "file-1", "name": "root.txt", "content": "root"},
    {"id": "file-2", "name": "nested.pdf", "content": "nested"},
]


def test_expand_selection_folder_recursive():
    auth = AuthContext(connector_id="box", access_token="dev")
    collector = BoxCollector(
        auth=auth,
        config={"mock_browse": MOCK_BROWSE, "mock_items": MOCK_ITEMS, "mock_refresh": True},
    )
    selected = [SelectedItem(id="folder-1", type=BrowseItemType.FOLDER, name="Docs", path="/Docs")]
    expanded = collector.expand_selection(selected)
    assert {item.item_id for item in expanded} == {"file-2"}


def test_expand_selection_mixed():
    auth = AuthContext(connector_id="box", access_token="dev")
    collector = BoxCollector(
        auth=auth,
        config={"mock_browse": MOCK_BROWSE, "mock_items": MOCK_ITEMS, "mock_refresh": True},
    )
    selected = [
        SelectedItem(id="file-1", type=BrowseItemType.FILE, name="root.txt", path="/root.txt"),
        SelectedItem(id="folder-1", type=BrowseItemType.FOLDER, name="Docs", path="/Docs"),
    ]
    expanded = collector.expand_selection(selected)
    assert {item.item_id for item in expanded} == {"file-1", "file-2"}


def test_ingestion_pipeline_selected_only(tmp_path):
    auth = AuthContext(connector_id="box", access_token="dev")
    collector = BoxCollector(
        auth=auth,
        config={
            "mock_browse": MOCK_BROWSE,
            "mock_items": MOCK_ITEMS,
            "mock_refresh": True,
            "selected_items": [
                {"id": "file-1", "type": "file", "name": "root.txt", "path": "/root.txt"},
            ],
        },
    )

    filesystem = MagicMock()
    filesystem.write_item.return_value = str(tmp_path / "root.txt")
    metadata_store = MagicMock()
    metadata_store.exists.return_value = False
    state_manager = MagicMock()

    pipeline = IngestionPipeline(
        collector=collector,
        filesystem=filesystem,
        metadata_store=metadata_store,
        state_manager=state_manager,
    )
    result = pipeline.run()
    assert result["items_collected"] == 1
    assert result["items_failed"] == 0
    filesystem.write_item.assert_called_once()
