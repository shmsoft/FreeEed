"""Structured JSON logging for collectors."""

from __future__ import annotations

import json
import logging
import sys
from datetime import datetime
from typing import Any, Dict, Optional


class _JsonFormatter(logging.Formatter):
    def format(self, record: logging.LogRecord) -> str:
        payload: Dict[str, Any] = {
            "timestamp": datetime.utcnow().isoformat() + "Z",
            "level": record.levelname,
            "logger": record.name,
            "message": record.getMessage(),
        }
        if hasattr(record, "extra_fields") and isinstance(record.extra_fields, dict):
            payload.update(record.extra_fields)
        if record.exc_info:
            payload["exception"] = self.formatException(record.exc_info)
        return json.dumps(payload, default=str)


class LoggingManager:
    """Centralized structured logging."""

    _configured = False

    @classmethod
    def configure(cls, level: int = logging.INFO, name: str = "collectors") -> logging.Logger:
        logger = logging.getLogger(name)
        if not cls._configured:
            handler = logging.StreamHandler(sys.stdout)
            handler.setFormatter(_JsonFormatter())
            logger.addHandler(handler)
            logger.setLevel(level)
            logger.propagate = False
            cls._configured = True
        return logger

    @classmethod
    def log_event(
        cls,
        logger: logging.Logger,
        event: str,
        level: int = logging.INFO,
        **fields: Any,
    ) -> None:
        record = logger.makeRecord(
            logger.name,
            level,
            "(unknown)",
            0,
            event,
            (),
            None,
        )
        record.extra_fields = {"event": event, **fields}  # type: ignore[attr-defined]
        logger.handle(record)
