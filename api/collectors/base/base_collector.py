"""Abstract base class for external data connectors."""

from __future__ import annotations

import abc
import time
from datetime import datetime
from typing import Any, Dict, Iterator, List, Optional

from .auth_context import AuthContext
from .types import BrowseItem, CollectionItem, ConnectorHealth, ItemMetadata, RateLimitInfo, SelectedItem


class BaseCollector(abc.ABC):
    """All connectors must implement this interface."""

    def __init__(self, auth: AuthContext, config: Optional[Dict[str, Any]] = None):
        self.auth = auth
        self.config = config or {}

    @property
    @abc.abstractmethod
    def connector_id(self) -> str:
        """Unique connector identifier."""

    @abc.abstractmethod
    def authenticate(self) -> AuthContext:
        """Validate or refresh credentials."""

    @abc.abstractmethod
    def list_children(self, parent_id: Optional[str] = None) -> List[BrowseItem]:
        """List immediate child folders and files at parent (root when parent_id is None)."""

    @abc.abstractmethod
    def list_items(self, since: Optional[datetime] = None) -> List[ItemMetadata]:
        """List available items, optionally filtered by modification time."""

    @abc.abstractmethod
    def fetch_item(self, item_id: str) -> CollectionItem:
        """Download a single item by ID."""

    @abc.abstractmethod
    def stream_changes(self) -> Iterator[ItemMetadata]:
        """Stream incremental changes from the remote source."""

    @abc.abstractmethod
    def handle_rate_limits(self, response: Any) -> Optional[RateLimitInfo]:
        """Parse rate-limit response headers/body; return wait info if limited."""

    @abc.abstractmethod
    def serialize_metadata(self, metadata: ItemMetadata) -> Dict[str, Any]:
        """Convert metadata to a JSON-serializable dict."""

    def health_check(self) -> ConnectorHealth:
        """Default health check: attempt authentication."""
        try:
            self.authenticate()
            return ConnectorHealth(
                connector_id=self.connector_id,
                healthy=True,
                message="Authentication successful",
                checked_at=datetime.utcnow(),
            )
        except Exception as exc:
            return ConnectorHealth(
                connector_id=self.connector_id,
                healthy=False,
                message=str(exc),
                checked_at=datetime.utcnow(),
            )

    def wait_for_rate_limit(self, info: RateLimitInfo) -> None:
        """Block until rate limit window expires."""
        time.sleep(max(0.0, info.retry_after_seconds))

    def expand_selection(
        self,
        selected: List[SelectedItem],
        since: Optional[datetime] = None,
    ) -> List[ItemMetadata]:
        """Resolve selected folders/files into file metadata for collection."""
        self.authenticate()
        seen: set[str] = set()
        result: List[ItemMetadata] = []

        def add_file(meta: ItemMetadata) -> None:
            if meta.item_id in seen:
                return
            if since and meta.modified_at and meta.modified_at < since:
                return
            seen.add(meta.item_id)
            result.append(meta)

        def walk_folder(folder_id: str) -> None:
            for child in self.list_children(folder_id):
                if child.type.value == "folder":
                    walk_folder(child.id)
                else:
                    add_file(self._browse_item_to_metadata(child))

        for item in selected:
            if item.type.value == "folder":
                walk_folder(item.id)
            else:
                add_file(self._browse_item_to_metadata(item))

        return result

    def _browse_item_to_metadata(self, item: BrowseItem | SelectedItem) -> ItemMetadata:
        return ItemMetadata(
            item_id=item.id,
            name=item.name or item.id,
            size_bytes=getattr(item, "size", None),
            modified_at=getattr(item, "modified_at", None),
            source_path=f"{self.connector_id}://{item.path or item.id}",
        )
