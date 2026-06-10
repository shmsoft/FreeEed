"""End-to-end ingestion: list/stream -> fetch -> normalize -> store."""

from __future__ import annotations

import logging
from datetime import datetime
from typing import Any, Dict, Optional

from collectors.base.base_collector import BaseCollector
from collectors.base.types import BrowseItemType, SelectedItem
from collectors.core.logging_manager import LoggingManager
from collectors.core.state_manager import StateManager
from collectors.ingestion.metadata_normalizer import MetadataNormalizer
from collectors.ingestion.streaming_engine import StreamingEngine
from collectors.storage.storage_interface import MetadataStore, StorageBackend


class IngestionPipeline:
    """Orchestrates item discovery, download, and persistence."""

    def __init__(
        self,
        collector: BaseCollector,
        filesystem: StorageBackend,
        metadata_store: MetadataStore,
        state_manager: StateManager,
        logger: Optional[logging.Logger] = None,
        max_workers: int = 4,
    ):
        self.collector = collector
        self.filesystem = filesystem
        self.metadata_store = metadata_store
        self.state = state_manager
        self.logger = logger or LoggingManager.configure()
        self.normalizer = MetadataNormalizer()
        self.engine = StreamingEngine(collector, max_workers=max_workers, logger=self.logger)

    def run(self, since: Optional[datetime] = None) -> Dict[str, Any]:
        connector_id = self.collector.connector_id
        selected = self._resolve_selected_items()
        if selected:
            items = self.collector.expand_selection(selected, since=since)
        else:
            items = list(self.engine.iter_items(since=since))
        LoggingManager.log_event(
            self.logger,
            "items_discovered",
            connector_id=connector_id,
            count=len(items),
        )

        def on_item(collection_item) -> None:
            meta = collection_item.metadata
            if collection_item.content is None:
                raise ValueError(f"No content for item {meta.item_id}")

            if not meta.checksum:
                meta.checksum = self.normalizer.content_checksum(collection_item.content)

            if self.metadata_store.exists(meta.item_id, meta.checksum):
                LoggingManager.log_event(
                    self.logger,
                    "item_skipped_idempotent",
                    item_id=meta.item_id,
                )
                return

            local_path = self.filesystem.write_item(meta, collection_item.content)
            record = self.normalizer.to_storage_record(meta, local_path)
            self.metadata_store.save(record)
            serialized = self.collector.serialize_metadata(meta)
            self.metadata_store.save_raw(meta.item_id, serialized)

        def on_error(item_id: str, exc: Exception) -> None:
            self.state.add_failed_item(connector_id, item_id)

        result = self.engine.fetch_all(items, on_item=on_item, on_error=on_error)
        result["sync_token"] = datetime.utcnow().isoformat()
        return result

    def _resolve_selected_items(self) -> List[SelectedItem]:
        config = self.collector.config
        raw_items = config.get("selected_items")
        if raw_items:
            resolved: List[SelectedItem] = []
            for entry in raw_items:
                item_type = entry.get("type", "file")
                resolved.append(
                    SelectedItem(
                        id=str(entry["id"]),
                        type=BrowseItemType(item_type),
                        name=str(entry.get("name", "")),
                        path=str(entry.get("path", "")),
                    )
                )
            return resolved

        selected_ids = config.get("selected_item_ids") or []
        selected_paths = config.get("selected_paths") or []
        resolved = [
            SelectedItem(id=str(item_id), type=BrowseItemType.FILE, path=str(item_id))
            for item_id in selected_ids
        ]
        resolved.extend(
            SelectedItem(id=str(path), type=BrowseItemType.FILE, path=str(path))
            for path in selected_paths
        )
        return resolved
