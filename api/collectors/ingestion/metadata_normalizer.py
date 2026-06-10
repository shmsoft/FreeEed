"""Normalize connector metadata to canonical form."""

from __future__ import annotations

import hashlib
import re
from datetime import datetime
from typing import Any, Dict

from collectors.base.types import ItemMetadata


class MetadataNormalizer:
    """Convert raw connector metadata into canonical ItemMetadata records."""

    INVALID_FILENAME_CHARS = re.compile(r'[<>:"/\\|?*\x00-\x1f]')

    def normalize(self, raw: Dict[str, Any], connector_id: str) -> ItemMetadata:
        item_id = str(raw.get("id") or raw.get("item_id") or "")
        name = self._sanitize_filename(str(raw.get("name") or raw.get("title") or item_id))
        modified = raw.get("modified_at") or raw.get("modifiedTime") or raw.get("server_modified")
        modified_at = self._parse_datetime(modified)

        return ItemMetadata(
            item_id=item_id,
            name=name,
            mime_type=raw.get("mime_type") or raw.get("mimeType"),
            size_bytes=self._parse_int(raw.get("size") or raw.get("size_bytes")),
            modified_at=modified_at,
            source_path=raw.get("path") or raw.get("source_path"),
            checksum=raw.get("checksum") or raw.get("content_hash"),
            extra={
                "connector_id": connector_id,
                "raw": {k: v for k, v in raw.items() if k not in ("content",)},
            },
        )

    def to_storage_record(self, metadata: ItemMetadata, local_path: str) -> Dict[str, Any]:
        return {
            "item_id": metadata.item_id,
            "name": metadata.name,
            "mime_type": metadata.mime_type,
            "size_bytes": metadata.size_bytes,
            "modified_at": metadata.modified_at.isoformat() if metadata.modified_at else None,
            "source_path": metadata.source_path,
            "checksum": metadata.checksum,
            "local_path": local_path,
            "connector_id": metadata.extra.get("connector_id"),
        }

    def content_checksum(self, content: bytes) -> str:
        return hashlib.sha256(content).hexdigest()

    def _sanitize_filename(self, name: str) -> str:
        cleaned = self.INVALID_FILENAME_CHARS.sub("_", name.strip())
        return cleaned or "unnamed"

    def _parse_datetime(self, value: Any) -> datetime | None:
        if value is None:
            return None
        if isinstance(value, datetime):
            return value
        if isinstance(value, (int, float)):
            return datetime.utcfromtimestamp(value)
        text = str(value).replace("Z", "+00:00")
        try:
            return datetime.fromisoformat(text)
        except ValueError:
            return None

    def _parse_int(self, value: Any) -> int | None:
        if value is None:
            return None
        try:
            return int(value)
        except (TypeError, ValueError):
            return None
