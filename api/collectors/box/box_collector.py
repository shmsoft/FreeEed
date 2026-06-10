"""Box.com connector."""

from __future__ import annotations

import os
from datetime import datetime
from typing import Any, Dict, Iterator, List, Optional

from collectors.base.auth_context import AuthContext
from collectors.base.base_collector import BaseCollector
from collectors.base.types import BrowseItem, BrowseItemType, CollectionItem, ItemMetadata, RateLimitInfo
from collectors.core.registry import register_connector

from .box_auth import BoxAuth


@register_connector("box")
class BoxCollector(BaseCollector):
    """Collect files from Box using OAuth2."""

    def __init__(self, auth: AuthContext, config: Optional[Dict[str, Any]] = None):
        super().__init__(auth, config)
        self._auth_client = BoxAuth(auth, config)
        self._client = None
        self._mock_items = config.get("mock_items") if config else None
        self._mock_browse = config.get("mock_browse") if config else None

    @property
    def connector_id(self) -> str:
        return "box"

    def authenticate(self) -> AuthContext:
        self.auth = self._auth_client.ensure_valid_token()
        if self._mock_items is None and self._mock_browse is None:
            self._client = self._auth_client.build_client(self.auth)
        return self.auth

    def list_children(self, parent_id: Optional[str] = None) -> List[BrowseItem]:
        self.authenticate()
        folder_id = parent_id or "0"
        if self._mock_browse is not None:
            return [self._dict_to_browse_item(entry) for entry in self._mock_browse.get(folder_id, [])]

        items = self._client.folder(folder_id).get_items(limit=1000)
        result: List[BrowseItem] = []
        for entry in items:
            item_type = BrowseItemType.FOLDER if entry.type == "folder" else BrowseItemType.FILE
            modified = getattr(entry, "modified_at", None)
            modified_at = (
                datetime.fromisoformat(str(modified).replace("Z", "+00:00")) if modified else None
            )
            result.append(
                BrowseItem(
                    id=str(entry.id),
                    name=str(entry.name),
                    path=f"/{entry.name}" if folder_id == "0" else f"{folder_id}/{entry.name}",
                    type=item_type,
                    connector_id=self.connector_id,
                    size=getattr(entry, "size", None),
                    modified_at=modified_at,
                )
            )
        return result

    def list_items(self, since: Optional[datetime] = None) -> List[ItemMetadata]:
        self.authenticate()
        if self._mock_items is not None:
            return [self._to_metadata(item) for item in self._mock_items]
        folder_id = self.config.get("folder_id", "0")
        items = self._client.folder(folder_id).get_items(limit=1000)
        result = []
        for entry in items:
            if entry.type != "file":
                continue
            meta = self._entry_to_metadata(entry)
            if since and meta.modified_at and meta.modified_at < since:
                continue
            result.append(meta)
        return result

    def fetch_item(self, item_id: str) -> CollectionItem:
        self.authenticate()
        if self._mock_items is not None:
            for item in self._mock_items:
                if item["id"] == item_id:
                    content = item.get("content", b"").encode() if isinstance(item.get("content"), str) else item.get("content", b"")
                    return CollectionItem(metadata=self._to_metadata(item), content=content)
            raise FileNotFoundError(item_id)

        file_obj = self._client.file(item_id).get()
        content = self._client.file(item_id).content()
        return CollectionItem(
            metadata=self._entry_to_metadata(file_obj),
            content=content,
        )

    def stream_changes(self) -> Iterator[ItemMetadata]:
        yield from self.list_items()

    def handle_rate_limits(self, response: Any) -> Optional[RateLimitInfo]:
        headers = getattr(response, "headers", {}) or {}
        retry_after = headers.get("Retry-After") or headers.get("retry-after")
        status = getattr(response, "status_code", None)
        if status == 429 or retry_after:
            seconds = float(retry_after) if retry_after else 60.0
            return RateLimitInfo(retry_after_seconds=seconds, reason="box_rate_limit")
        return None

    def serialize_metadata(self, metadata: ItemMetadata) -> Dict[str, Any]:
        return {
            "id": metadata.item_id,
            "name": metadata.name,
            "mime_type": metadata.mime_type,
            "size": metadata.size_bytes,
            "modified_at": metadata.modified_at.isoformat() if metadata.modified_at else None,
            "source": "box",
        }

    def _entry_to_metadata(self, entry: Any) -> ItemMetadata:
        modified = getattr(entry, "modified_at", None)
        modified_at = datetime.fromisoformat(str(modified).replace("Z", "+00:00")) if modified else None
        return ItemMetadata(
            item_id=str(entry.id),
            name=str(entry.name),
            size_bytes=getattr(entry, "size", None),
            modified_at=modified_at,
            source_path=f"box://{entry.id}",
        )

    def _dict_to_browse_item(self, item: Dict[str, Any]) -> BrowseItem:
        item_type = BrowseItemType(item.get("type", "file"))
        modified = item.get("modified_at")
        modified_at = datetime.fromisoformat(modified) if modified else None
        return BrowseItem(
            id=str(item["id"]),
            name=str(item.get("name", item["id"])),
            path=str(item.get("path", item["id"])),
            type=item_type,
            connector_id=self.connector_id,
            size=item.get("size"),
            modified_at=modified_at,
            mime_type=item.get("mime_type"),
        )

    def _to_metadata(self, item: Dict[str, Any]) -> ItemMetadata:
        modified = item.get("modified_at")
        modified_at = datetime.fromisoformat(modified) if modified else None
        return ItemMetadata(
            item_id=str(item["id"]),
            name=str(item.get("name", item["id"])),
            mime_type=item.get("mime_type"),
            size_bytes=item.get("size"),
            modified_at=modified_at,
            source_path=f"box://{item['id']}",
        )
