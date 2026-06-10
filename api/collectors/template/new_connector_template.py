"""Template for implementing a new connector."""

from __future__ import annotations

from datetime import datetime
from typing import Any, Dict, Iterator, List, Optional

from collectors.base.auth_context import AuthContext
from collectors.base.base_collector import BaseCollector
from collectors.base.types import BrowseItem, CollectionItem, ItemMetadata, RateLimitInfo
from collectors.core.registry import register_connector


@register_connector("template")
class TemplateCollector(BaseCollector):
    """
    Copy this file to collectors/<name>/ and replace template logic.

    1. Rename connector_id and register_connector value
    2. Implement authenticate() with OAuth2 or API key flow
    3. Implement list_items, fetch_item, stream_changes
    4. Add auth module alongside this file
    """

    @property
    def connector_id(self) -> str:
        return "template"

    def authenticate(self) -> AuthContext:
        if not self.auth.access_token:
            raise ValueError("Template connector requires access_token")
        return self.auth

    def list_children(self, parent_id: Optional[str] = None) -> List[BrowseItem]:
        self.authenticate()
        return []

    def list_items(self, since: Optional[datetime] = None) -> List[ItemMetadata]:
        self.authenticate()
        return []

    def fetch_item(self, item_id: str) -> CollectionItem:
        self.authenticate()
        raise NotImplementedError(f"fetch_item not implemented for {item_id}")

    def stream_changes(self) -> Iterator[ItemMetadata]:
        yield from self.list_items()

    def handle_rate_limits(self, response: Any) -> Optional[RateLimitInfo]:
        headers = getattr(response, "headers", {}) or {}
        retry_after = headers.get("Retry-After")
        if retry_after:
            return RateLimitInfo(retry_after_seconds=float(retry_after))
        return None

    def serialize_metadata(self, metadata: ItemMetadata) -> Dict[str, Any]:
        return {"id": metadata.item_id, "name": metadata.name, "source": "template"}
