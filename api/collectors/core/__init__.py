"""Core collector subsystems."""

from .registry import register_connector, get_connector_class, list_connectors
from .security_manager import SecurityManager
from .logging_manager import LoggingManager
from .state_manager import StateManager

__all__ = [
    "register_connector",
    "get_connector_class",
    "list_connectors",
    "Orchestrator",
    "TriggerManager",
    "SecurityManager",
    "LoggingManager",
    "StateManager",
]


def __getattr__(name: str):
    if name == "Orchestrator":
        from .orchestrator import Orchestrator
        return Orchestrator
    if name == "TriggerManager":
        from .trigger_manager import TriggerManager
        return TriggerManager
    raise AttributeError(f"module {__name__!r} has no attribute {name!r}")
