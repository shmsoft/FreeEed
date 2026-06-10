"""Shared types for the collector framework."""

from __future__ import annotations

from dataclasses import dataclass, field
from datetime import datetime
from enum import Enum
from typing import Any, Dict, List, Optional


class CollectionJobStatus(str, Enum):
    PENDING = "pending"
    RUNNING = "running"
    COMPLETED = "completed"
    FAILED = "failed"
    CANCELLED = "cancelled"


class CircuitBreakerState(str, Enum):
    CLOSED = "closed"
    OPEN = "open"
    HALF_OPEN = "half_open"


class BrowseItemType(str, Enum):
    FOLDER = "folder"
    FILE = "file"


@dataclass
class BrowseItem:
    """Normalized remote folder/file entry for UI browsing."""

    id: str
    name: str
    path: str
    type: BrowseItemType
    connector_id: str
    size: Optional[int] = None
    modified_at: Optional[datetime] = None
    mime_type: Optional[str] = None
    extra: Dict[str, Any] = field(default_factory=dict)


@dataclass
class SelectedItem:
    """User selection persisted per project and connector."""

    id: str
    type: BrowseItemType
    name: str = ""
    path: str = ""


@dataclass
class ItemMetadata:
    item_id: str
    name: str
    mime_type: Optional[str] = None
    size_bytes: Optional[int] = None
    modified_at: Optional[datetime] = None
    source_path: Optional[str] = None
    checksum: Optional[str] = None
    extra: Dict[str, Any] = field(default_factory=dict)


@dataclass
class CollectionItem:
    metadata: ItemMetadata
    content: Optional[bytes] = None
    local_path: Optional[str] = None


@dataclass
class RateLimitInfo:
    retry_after_seconds: float
    reason: str = "rate_limit"


@dataclass
class ConnectorHealth:
    connector_id: str
    healthy: bool
    message: str = ""
    checked_at: Optional[datetime] = None


@dataclass
class CollectionJob:
    job_id: str
    connector_id: str
    status: CollectionJobStatus = CollectionJobStatus.PENDING
    created_at: Optional[datetime] = None
    started_at: Optional[datetime] = None
    completed_at: Optional[datetime] = None
    output_dir: Optional[str] = None
    items_collected: int = 0
    items_failed: int = 0
    error_message: Optional[str] = None
    auto_chain: bool = False
    config: Dict[str, Any] = field(default_factory=dict)
