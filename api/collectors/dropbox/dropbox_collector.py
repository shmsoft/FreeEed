"""Dropbox connector."""

from __future__ import annotations

from datetime import datetime
from typing import Any, Dict, Iterator, List, Optional

from collectors.base.auth_context import AuthContext
from collectors.base.base_collector import BaseCollector
from collectors.base.types import BrowseItem, BrowseItemType, CollectionItem, ItemMetadata, RateLimitInfo
from collectors.core.registry import register_connector

from .dropbox_auth import DropboxAuth


@register_connector("dropbox")
class DropboxCollector(BaseCollector):
    """Collect files from Dropbox using OAuth2."""

    def __init__(self, auth: AuthContext, config: Optional[Dict[str, Any]] = None):
        super().__init__(auth, config)
        self._auth_client = DropboxAuth(auth, config)
        self._client = None
        self._mock_items = config.get("mock_items") if config else None
        self._mock_browse = config.get("mock_browse") if config else None

    @property
    def connector_id(self) -> str:
        return "dropbox"

    def authenticate(self) -> AuthContext:
        self.auth = self._auth_client.ensure_valid_token()
        if self._mock_items is None and self._mock_browse is None:
            self._client = self._auth_client.build_client(self.auth)
        return self.auth

    def list_children(self, parent_id: Optional[str] = None) -> List[BrowseItem]:
        self.authenticate()
        path = parent_id if parent_id is not None else ""
        if self._mock_browse is not None:
            return [self._dict_to_browse_item(entry) for entry in self._mock_browse.get(path, [])]

        result = self._client.files_list_folder(path or "")
        entries = list(result.entries)
        while result.has_more:
            result = self._client.files_list_folder_continue(result.cursor)
            entries.extend(result.entries)

        items: List[BrowseItem] = []
        for entry in entries:
            is_folder = entry.__class__.__name__ == "FolderMetadata"
            item_type = BrowseItemType.FOLDER if is_folder else BrowseItemType.FILE
            display_path = getattr(entry, "path_display", "") or ""
            modified = getattr(entry, "server_modified", None) or getattr(entry, "client_modified", None)
            modified_at = modified if isinstance(modified, datetime) else None
            items.append(
                BrowseItem(
                    id=display_path,
                    name=getattr(entry, "name", display_path),
                    path=display_path,
                    type=item_type,
                    connector_id=self.connector_id,
                    size=getattr(entry, "size", None),
                    modified_at=modified_at,
                )
            )
        return items

    def list_items(self, since: Optional[datetime] = None) -> List[ItemMetadata]:
        self.authenticate()
        if self._mock_items is not None:
            items = [self._to_metadata(item) for item in self._mock_items]
            if since:
                items = [m for m in items if not m.modified_at or m.modified_at >= since]
            return items

        path = self.config.get("path", "")
        result = self._client.files_list_folder(path, recursive=True)
        entries = list(result.entries)
        while result.has_more:
            result = self._client.files_list_folder_continue(result.cursor)
            entries.extend(result.entries)

        items = []
        for entry in entries:
            if entry.__class__.__name__ != "FileMetadata":
                continue
            meta = self._entry_to_metadata(entry)
            if since and meta.modified_at and meta.modified_at < since:
                continue
            items.append(meta)
        return items

    def fetch_item(self, item_id: str) -> CollectionItem:
        self.authenticate()
        if self._mock_items is not None:
            for item in self._mock_items:
                if item["id"] == item_id:
                    content = item.get("content", b"")
                    if isinstance(content, str):
                        content = content.encode()
                    return CollectionItem(metadata=self._to_metadata(item), content=content)
            raise FileNotFoundError(item_id)

        path = item_id if item_id.startswith("/") else f"/{item_id}"
        _, response = self._client.files_download(path)
        meta = self._client.files_get_metadata(path)
        return CollectionItem(metadata=self._entry_to_metadata(meta), content=response.content)

    def stream_changes(self) -> Iterator[ItemMetadata]:
        yield from self.list_items()

    def handle_rate_limits(self, response: Any) -> Optional[RateLimitInfo]:
        headers = getattr(response, "headers", {}) or {}
        retry_after = headers.get("Retry-After") or headers.get("retry-after")
        status = getattr(response, "status_code", None)
        if status == 429 or retry_after:
            seconds = float(retry_after) if retry_after else 60.0
            return RateLimitInfo(retry_after_seconds=seconds, reason="dropbox_rate_limit")
        return None

    def serialize_metadata(self, metadata: ItemMetadata) -> Dict[str, Any]:
        return {
            "id": metadata.item_id,
            "name": metadata.name,
            "mime_type": metadata.mime_type,
            "size": metadata.size_bytes,
            "modified_at": metadata.modified_at.isoformat() if metadata.modified_at else None,
            "source": "dropbox",
        }

    def _entry_to_metadata(self, entry: Any) -> ItemMetadata:
        modified = getattr(entry, "server_modified", None) or getattr(entry, "client_modified", None)
        modified_at = modified if isinstance(modified, datetime) else None
        path = getattr(entry, "path_display", None) or getattr(entry, "id", "")
        return ItemMetadata(
            item_id=str(path),
            name=getattr(entry, "name", str(path)),
            size_bytes=getattr(entry, "size", None),
            modified_at=modified_at,
            source_path=f"dropbox://{path}",
        )

    def _dict_to_browse_item(self, item: Dict[str, Any]) -> BrowseItem:
        item_type = BrowseItemType(item.get("type", "file"))
        modified = item.get("modified_at") or item.get("server_modified")
        modified_at = datetime.fromisoformat(modified) if isinstance(modified, str) else modified
        item_id = str(item.get("id", item.get("path", "")))
        return BrowseItem(
            id=item_id,
            name=str(item.get("name", item_id)),
            path=str(item.get("path", item_id)),
            type=item_type,
            connector_id=self.connector_id,
            size=item.get("size"),
            modified_at=modified_at,
            mime_type=item.get("mime_type"),
        )

    def _to_metadata(self, item: Dict[str, Any]) -> ItemMetadata:
        modified = item.get("modified_at") or item.get("server_modified")
        modified_at = datetime.fromisoformat(modified) if isinstance(modified, str) else modified
        item_id = str(item.get("id", item.get("path", "")))
        return ItemMetadata(
            item_id=item_id,
            name=str(item.get("name", item_id)),
            mime_type=item.get("mime_type"),
            size_bytes=item.get("size"),
            modified_at=modified_at,
            source_path=f"dropbox://{item_id}",
        )
