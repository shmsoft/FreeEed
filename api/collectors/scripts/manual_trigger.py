#!/usr/bin/env python3
"""Manually trigger a collection job from the CLI."""

from __future__ import annotations

import argparse
import json
import sys
import time
from pathlib import Path

API_ROOT = Path(__file__).resolve().parents[2]
if str(API_ROOT) not in sys.path:
    sys.path.insert(0, str(API_ROOT))

from collectors.core.logging_manager import LoggingManager
from collectors.core.orchestrator import Orchestrator
from collectors.core.state_manager import StateManager
from collectors.core.trigger_manager import TriggerManager

import collectors.box.box_collector  # noqa: F401
import collectors.google_drive.gdrive_collector  # noqa: F401
import collectors.dropbox.dropbox_collector  # noqa: F401


def main() -> int:
    parser = argparse.ArgumentParser(description="Manually trigger a collection job")
    parser.add_argument("connector_id", help="Connector ID (box, google_drive, dropbox)")
    parser.add_argument("--auto-chain", action="store_true", help="Chain to Java processing")
    parser.add_argument("--config", default="{}", help="JSON config override")
    parser.add_argument("--wait", action="store_true", help="Wait for job completion")
    args = parser.parse_args()

    LoggingManager.configure()
    state = StateManager()
    orchestrator = Orchestrator(state_manager=state)
    trigger = TriggerManager(state, run_callback=orchestrator.run_collection)

    config = json.loads(args.config)
    job = trigger.trigger_manual(args.connector_id, auto_chain=args.auto_chain, config=config)
    print(json.dumps({"job_id": job.job_id, "status": job.status.value}, indent=2))

    if args.wait:
        while True:
            current = state.get_job(job.job_id)
            if current and current.status.value in ("completed", "failed", "cancelled"):
                print(json.dumps({
                    "job_id": current.job_id,
                    "status": current.status.value,
                    "items_collected": current.items_collected,
                    "items_failed": current.items_failed,
                    "error_message": current.error_message,
                }, indent=2))
                break
            time.sleep(1)

    trigger.shutdown()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
