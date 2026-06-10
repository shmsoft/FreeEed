"""Selection persistence tests."""

import os

from collectors.base.types import BrowseItemType, SelectedItem
from collectors.core.selection_store import SelectionStore


def test_save_and_get_selections(tmp_path):
    db_path = str(tmp_path / "state.db")
    store = SelectionStore(db_path=db_path)

    items = [
        SelectedItem(id="f1", type=BrowseItemType.FOLDER, name="Docs", path="/Docs"),
        SelectedItem(id="file-1", type=BrowseItemType.FILE, name="a.txt", path="/a.txt"),
    ]
    saved = store.save_selections("project-1", "box", items)
    assert saved["project_id"] == "project-1"
    assert len(saved["items"]) == 2

    rows = store.get_selections("project-1", "box")
    assert len(rows) == 1
    assert rows[0]["items"][0]["type"] == "folder"

    all_rows = store.get_selections("project-1")
    assert len(all_rows) == 1

    loaded = store.get_selected_items("project-1", "box")
    assert loaded[1].name == "a.txt"


def test_selection_store_shares_db_with_state_manager(tmp_path):
    db_path = str(tmp_path / "shared.db")
    os.environ["COLLECTOR_STATE_DB"] = db_path
    store = SelectionStore()
    store.save_selections(
        "1",
        "dropbox",
        [SelectedItem(id="/memo.pdf", type=BrowseItemType.FILE, path="/memo.pdf")],
    )
    assert store.get_selections("1", "dropbox")[0]["connector_id"] == "dropbox"
