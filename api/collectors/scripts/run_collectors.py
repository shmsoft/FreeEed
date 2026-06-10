#!/usr/bin/env python3
"""CLI entry point for running scheduled collectors."""

from __future__ import annotations

import argparse
import signal
import sys
from pathlib import Path

# Ensure api/ is on path when run from repo root
API_ROOT = Path(__file__).resolve().parents[2]
if str(API_ROOT) not in sys.path:
    sys.path.insert(0, str(API_ROOT))

from collectors.core.logging_manager import LoggingManager
from collectors.core.orchestrator import Orchestrator
from collectors.core.state_manager import StateManager
from collectors.core.trigger_manager import TriggerManager

# Import connectors to register them
import collectors.box.box_collector  # noqa: F401
import collectors.google_drive.gdrive_collector  # noqa: F401
import collectors.dropbox.dropbox_collector  # noqa: F401


def main() -> int:
    parser = argparse.ArgumentParser(description="Run FreeEed collector scheduler")
    parser.add_argument("--load-schedules", action="store_true", help="Load cron schedules from DB")
    args = parser.parse_args()

    LoggingManager.configure()
    state = StateManager()
    orchestrator = Orchestrator(state_manager=state)
    trigger = TriggerManager(state, run_callback=orchestrator.run_collection)

    if args.load_schedules:
        trigger.load_schedules_from_db()

    def _shutdown(signum, frame):
        trigger.shutdown()
        sys.exit(0)

    signal.signal(signal.SIGINT, _shutdown)
    signal.signal(signal.SIGTERM, _shutdown)

    print("Collector scheduler running. Press Ctrl+C to stop.")
    signal.pause()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
