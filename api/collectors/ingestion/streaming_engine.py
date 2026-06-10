"""Concurrent streaming fetch engine."""

from __future__ import annotations

import logging
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime
from typing import Callable, Iterator, List, Optional

from collectors.base.base_collector import BaseCollector
from collectors.base.types import CollectionItem, ItemMetadata, RateLimitInfo
from collectors.core.logging_manager import LoggingManager


class StreamingEngine:
    """Fetch items with bounded concurrency and rate-limit handling."""

    def __init__(
        self,
        collector: BaseCollector,
        max_workers: int = 4,
        logger: Optional[logging.Logger] = None,
    ):
        self.collector = collector
        self.max_workers = max_workers
        self.logger = logger or LoggingManager.configure()

    def iter_items(self, since: Optional[datetime] = None) -> Iterator[ItemMetadata]:
        use_stream = self.collector.config.get("use_stream", True)
        if use_stream:
            yield from self.collector.stream_changes()
        else:
            yield from self.collector.list_items(since=since)

    def fetch_all(
        self,
        items: List[ItemMetadata],
        on_item: Callable[[CollectionItem], None],
        on_error: Optional[Callable[[str, Exception], None]] = None,
    ) -> dict:
        collected = 0
        failed = 0

        with ThreadPoolExecutor(max_workers=self.max_workers) as executor:
            futures = {
                executor.submit(self._fetch_with_retry, meta.item_id): meta
                for meta in items
            }
            for future in as_completed(futures):
                meta = futures[future]
                try:
                    item = future.result()
                    on_item(item)
                    collected += 1
                except Exception as exc:
                    failed += 1
                    LoggingManager.log_event(
                        self.logger,
                        "fetch_item_failed",
                        level=logging.WARNING,
                        item_id=meta.item_id,
                        error=str(exc),
                    )
                    if on_error:
                        on_error(meta.item_id, exc)

        return {"items_collected": collected, "items_failed": failed}

    def _fetch_with_retry(self, item_id: str, max_retries: int = 3) -> CollectionItem:
        last_exc: Optional[Exception] = None
        for attempt in range(max_retries):
            try:
                return self.collector.fetch_item(item_id)
            except Exception as exc:
                last_exc = exc
                rate_info = self._extract_rate_limit(exc)
                if rate_info:
                    self.collector.wait_for_rate_limit(rate_info)
                    continue
                if attempt < max_retries - 1:
                    continue
                raise
        raise last_exc or RuntimeError(f"Failed to fetch {item_id}")

    def _extract_rate_limit(self, exc: Exception) -> Optional[RateLimitInfo]:
        response = getattr(exc, "response", None)
        if response is not None:
            return self.collector.handle_rate_limits(response)
        return None
