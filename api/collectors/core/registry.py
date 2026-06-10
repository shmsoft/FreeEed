"""Connector registry with decorator-based registration."""

from __future__ import annotations

from typing import Callable, Dict, List, Type

from collectors.base.base_collector import BaseCollector

_REGISTRY: Dict[str, Type[BaseCollector]] = {}


def register_connector(connector_id: str) -> Callable[[Type[BaseCollector]], Type[BaseCollector]]:
    """Decorator to register a connector class by ID."""

    def decorator(cls: Type[BaseCollector]) -> Type[BaseCollector]:
        if connector_id in _REGISTRY:
            raise ValueError(f"Connector already registered: {connector_id}")
        _REGISTRY[connector_id] = cls
        cls._registered_connector_id = connector_id  # type: ignore[attr-defined]
        return cls

    return decorator


def get_connector_class(connector_id: str) -> Type[BaseCollector]:
    if connector_id not in _REGISTRY:
        raise KeyError(f"Unknown connector: {connector_id}")
    return _REGISTRY[connector_id]


def list_connectors() -> List[str]:
    return sorted(_REGISTRY.keys())


def clear_registry() -> None:
    """Test helper to reset registry."""
    _REGISTRY.clear()
