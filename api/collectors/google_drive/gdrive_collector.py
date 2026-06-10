"""Google Drive connector."""

from __future__ import annotations

from datetime import datetime
from typing import Any, Dict, Iterator, List, Optional

from collectors.base.auth_context import AuthContext
from collectors.base.base_collector import BaseCollector
from collectors.base.types import BrowseItem, BrowseItemType, CollectionItem, ItemMetadata, RateLimitInfo
from collectors.core.registry import register_connector

from .gdrive_auth import GoogleDriveAuth


@register_connector("google_drive")
class GoogleDriveCollector(BaseCollector):
    """Collect files from Google Drive using OAuth2."""

    def __init__(self, auth: AuthContext, config: Optional[Dict[str, Any]] = None):
        super().__init__(auth, config)
        self._auth_client = GoogleDriveAuth(auth, config)
        self._service = None
        self._mock_items = config.get("mock_items") if config else None
        self._mock_browse = config.get("mock_browse") if config else None

    @property
    def connector_id(self) -> str:
        return "google_drive"

    def authenticate(self) -> AuthContext:
        self.auth = self._auth_client.ensure_valid_token()
        if self._mock_items is None and self._mock_browse is None:
            self._service = self._auth_client.build_service(self.auth)
        return self.auth

    def list_children(self, parent_id: Optional[str] = None) -> List[BrowseItem]:
        self.authenticate()
        folder_id = parent_id or "root"
        if self._mock_browse is not None:
            return [self._dict_to_browse_item(entry) for entry in self._mock_browse.get(folder_id, [])]

        query = f"'{folder_id}' in parents and trashed = false"
        results = (
            self._service.files()
            .list(
                q=query,
                fields="files(id,name,mimeType,size,modifiedTime)",
                orderBy="folder,name",
            )
            .execute()
        )
        items: List[BrowseItem] = []
        for file_obj in results.get("files", []):
            mime = file_obj.get("mimeType", "")
            is_folder = mime == "application/vnd.google-apps.folder"
            item_type = BrowseItemType.FOLDER if is_folder else BrowseItemType.FILE
            modified = file_obj.get("modifiedTime")
            modified_at = (
                datetime.fromisoformat(modified.replace("Z", "+00:00")) if modified else None
            )
            size = file_obj.get("size")
            items.append(
                BrowseItem(
                    id=file_obj["id"],
                    name=file_obj.get("name", file_obj["id"]),
                    path=f"/{file_obj.get('name', file_obj['id'])}",
                    type=item_type,
                    connector_id=self.connector_id,
                    size=int(size) if size else None,
                    modified_at=modified_at,
                    mime_type=mime,
                )
            )
        return items

    def list_items(self, since: Optional[datetime] = None) -> List[ItemMetadata]:
        self.authenticate()
        if self._mock_items is not None:
            return [self._to_metadata(item) for item in self._mock_items]

        query = "trashed = false"
        if since:
            query += f" and modifiedTime > '{since.isoformat()}Z'"
        folder_id = self.config.get("folder_id")
        if folder_id:
            query += f" and '{folder_id}' in parents"

        results = (
            self._service.files()
            .list(q=query, fields="files(id,name,mimeType,size,modifiedTime)")
            .execute()
        )
        return [self._file_to_metadata(f) for f in results.get("files", [])]

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

        meta = self._service.files().get(fileId=item_id, fields="id,name,mimeType,size,modifiedTime").execute()
        from googleapiclient.http import MediaIoBaseDownload
        import io

        request = self._service.files().get_media(fileId=item_id)
        buffer = io.BytesIO()
        downloader = MediaIoBaseDownload(buffer, request)
        done = False
        while not done:
            _, done = downloader.next_chunk()
        return CollectionItem(metadata=self._file_to_metadata(meta), content=buffer.getvalue())

    def stream_changes(self) -> Iterator[ItemMetadata]:
        yield from self.list_items()

    def handle_rate_limits(self, response: Any) -> Optional[RateLimitInfo]:
        headers = getattr(response, "headers", {}) or {}
        retry_after = headers.get("Retry-After") or headers.get("retry-after")
        status = getattr(response, "status_code", None) or getattr(response, "status", None)
        if status in (429, "429") or retry_after:
            seconds = float(retry_after) if retry_after else 60.0
            return RateLimitInfo(retry_after_seconds=seconds, reason="gdrive_rate_limit")
        return None

    def serialize_metadata(self, metadata: ItemMetadata) -> Dict[str, Any]:
        return {
            "id": metadata.item_id,
            "name": metadata.name,
            "mime_type": metadata.mime_type,
            "size": metadata.size_bytes,
            "modified_at": metadata.modified_at.isoformat() if metadata.modified_at else None,
            "source": "google_drive",
        }

    def _file_to_metadata(self, file_obj: Dict[str, Any]) -> ItemMetadata:
        modified = file_obj.get("modifiedTime")
        modified_at = datetime.fromisoformat(modified.replace("Z", "+00:00")) if modified else None
        size = file_obj.get("size")
        return ItemMetadata(
            item_id=file_obj["id"],
            name=file_obj.get("name", file_obj["id"]),
            mime_type=file_obj.get("mimeType"),
            size_bytes=int(size) if size else None,
            modified_at=modified_at,
            source_path=f"gdrive://{file_obj['id']}",
        )

    def _dict_to_browse_item(self, item: Dict[str, Any]) -> BrowseItem:
        item_type = BrowseItemType(item.get("type", "file"))
        modified = item.get("modified_at") or item.get("modifiedTime")
        modified_at = datetime.fromisoformat(modified.replace("Z", "+00:00")) if modified else None
        return BrowseItem(
            id=str(item["id"]),
            name=str(item.get("name", item["id"])),
            path=str(item.get("path", item["id"])),
            type=item_type,
            connector_id=self.connector_id,
            size=int(item["size"]) if item.get("size") else None,
            modified_at=modified_at,
            mime_type=item.get("mime_type") or item.get("mimeType"),
        )

    def _to_metadata(self, item: Dict[str, Any]) -> ItemMetadata:
        modified = item.get("modified_at") or item.get("modifiedTime")
        modified_at = datetime.fromisoformat(modified.replace("Z", "+00:00")) if modified else None
        return ItemMetadata(
            item_id=str(item["id"]),
            name=str(item.get("name", item["id"])),
            mime_type=item.get("mime_type") or item.get("mimeType"),
            size_bytes=int(item["size"]) if item.get("size") else None,
            modified_at=modified_at,
            source_path=f"gdrive://{item['id']}",
        )
