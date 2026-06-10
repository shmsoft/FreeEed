# External Data Collection Framework

FreeEed collectors ingest documents from cloud storage providers into `/data/input/collections/<job_id>/` for optional Java processing.

## Components

| Layer | Module | Responsibility |
|-------|--------|----------------|
| Base | `base/base_collector.py` | Connector ABC |
| Core | `core/orchestrator.py` | Run coordination |
| Core | `core/trigger_manager.py` | Manual + cron triggers |
| Core | `core/security_manager.py` | Secrets + token encryption |
| Core | `core/state_manager.py` | SQLite sync state |
| Ingestion | `ingestion/ingestion_pipeline.py` | list → fetch → store |
| Storage | `storage/filesystem_store.py` | Item files |
| Processing | `processing/processing_engine_client.py` | `.project` + Java chain |

## Data Flow

1. Trigger (API, CLI, or cron) creates a `CollectionJob`
2. Orchestrator authenticates connector and runs ingestion pipeline
3. Files land under `/data/input/collections/<job_id>/`
4. Metadata stored in job-local SQLite (`metadata.db`)
5. If `auto_chain=true`, a Java-canonical `.project` file is written and `run_freeeed_process()` is invoked

## State Database

Global state: `COLLECTOR_STATE_DB` (default `/app/db/collector_state.db`)

Tracks per-connector sync tokens, failed items, and circuit breaker status.

## Security

- Secrets via `COLLECTOR_<CONNECTOR>_*` env vars or `/app/secrets/<connector>.json`
- Optional Fernet encryption with `COLLECTOR_ENCRYPTION_KEY`
- Structured logs sanitize tokens automatically

## Shutdown

`TriggerManager.shutdown()` stops APScheduler gracefully on SIGTERM/SIGINT (see `scripts/run_collectors.py`).
