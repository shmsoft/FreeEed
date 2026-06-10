"""Storage abstractions."""

from __future__ import annotations

import abc
from typing import Any, Dict


class StorageBackend(abc.ABC):
    @abc.abstractmethod
    def write_item(self, metadata: Any, content: bytes) -> str:
        """Persist item content; return local path."""


class MetadataStore(abc.ABC):
    @abc.abstractmethod
    def save(self, record: Dict[str, Any]) -> None:
        """Save normalized metadata record."""

    @abc.abstractmethod
    def save_raw(self, item_id: str, raw: Dict[str, Any]) -> None:
        """Save connector-specific serialized metadata."""

    @abc.abstractmethod
    def exists(self, item_id: str, checksum: str) -> bool:
        """Return True if item with same checksum already stored (idempotent)."""
