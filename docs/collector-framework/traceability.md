# Collector Framework — Traceability Matrix

Track milestone progress: **Milestone → Issues → PRs → Deliverables**.

Update this file at each milestone boundary (or when major deliverables land). Issue and PR columns start as placeholders until work begins in the private `FreeEed-collectors` repo.

**Legend:** `-` = not started | `#NNN` = GitHub issue/PR number | `✓` = complete

---

## Summary matrix

| Milestone | Issues | PRs | Status | Deliverables |
|-----------|--------|-----|--------|--------------|
| M0 — GitHub & scaffolding | — | — | Planned | See [M0](#m0--github--scaffolding) |
| M1 — Core framework | — | — | Planned | See [M1](#m1--core-framework) |
| M2 — Ingestion & storage | — | — | Planned | See [M2](#m2--ingestion--storage) |
| M3 — Trigger & processing | — | — | Planned | See [M3](#m3--trigger--processing) |
| M4 — Box connector | — | — | Planned | See [M4](#m4--box-connector) |
| M5 — Drive & Dropbox | — | — | Planned | See [M5](#m5--drive--dropbox) |
| M6 — API & deployment | — | — | Planned | See [M6](#m6--api--deployment) |
| M7 — Tests & docs | — | — | Planned | See [M7](#m7--tests--docs) |
| M8 — Public promotion | — | — | Planned | See [M8](#m8--public-promotion) |
| M9 — Collection Selection UI | — | — | Planned | See [M9](#m9--collection-selection-ui) |

---

## M0 — GitHub & scaffolding

| Field | Value |
|-------|-------|
| **Issues** | — |
| **PRs** | — |
| **Target** | Private repo bootstrap, tracking artifacts, CI skeleton |

### Planned deliverables

- [ ] Private repo `shmsoft/FreeEed-collectors` created
- [ ] GitHub Project v2: **External Data Collection Framework** (Backlog → Done columns)
- [ ] Milestones M0–M8 created
- [ ] Label taxonomy applied (`area:*`, `connector:*`, `type:*`, `priority:*`, `epic:collector-framework`)
- [ ] Issue templates: `feature.yml`, `bug.yml`, `task.yml`
- [ ] PR template with security + public-promotion checklist
- [ ] Workflow: `.github/workflows/collectors-ci.yml` (unit tests; integration gated)
- [ ] `docs/collector-framework/WORKFLOW.md` and this traceability matrix
- [ ] `api/collectors/` package scaffold (`__init__.py`, directory layout per architecture plan)
- [ ] Initial M0 seed issues opened and linked to project board

---

## M1 — Core framework

| Field | Value |
|-------|-------|
| **Issues** | — |
| **PRs** | — |
| **Target** | Base types, registry, orchestrator, cross-cutting managers |

### Planned deliverables

- [ ] `api/collectors/base/types.py` — `ItemMetadata`, `ItemContent`, `ChangeEvent`, `HealthStatus`
- [ ] `api/collectors/base/auth_context.py` — auth context model
- [ ] `api/collectors/base/base_collector.py` — `BaseCollector` ABC (mandatory interface)
- [ ] `api/collectors/core/registry.py` — `@register_connector` plugin discovery
- [ ] `api/collectors/core/orchestrator.py` — collector orchestration skeleton
- [ ] `api/collectors/core/logging_manager.py` — structured JSON logging
- [ ] `api/collectors/core/state_manager.py` — sync tokens, failed items, circuit breaker state
- [ ] `api/collectors/core/security_manager.py` — secrets loading, token encryption, log redaction skeleton
- [ ] Unit tests: registry, state manager, security encrypt/decrypt + redaction
- [ ] `api/requirements-collectors.txt` — pinned OSS deps (pytest, tenacity, etc.)

---

## M2 — Ingestion & storage

| Field | Value |
|-------|-------|
| **Issues** | — |
| **PRs** | — |
| **Target** | End-to-end collect → normalize → persist (no external connectors yet) |

### Planned deliverables

- [ ] `api/collectors/ingestion/streaming_engine.py` — concurrent fetch with limits
- [ ] `api/collectors/ingestion/metadata_normalizer.py` — unified metadata schema
- [ ] `api/collectors/ingestion/ingestion_pipeline.py` — orchestrated ingest flow
- [ ] `api/collectors/storage/storage_interface.py` — pluggable storage seam
- [ ] `api/collectors/storage/filesystem_store.py` — job-scoped dirs under `/data/input/collections/<job_id>/`
- [ ] `api/collectors/storage/sqlite_store.py` — metadata + job manifest in SQLite
- [ ] Unit tests: normalizer, filesystem store, SQLite store, pipeline (mock collector)
- [ ] Idempotent writes (content hash / item_id dedup)

---

## M3 — Trigger & processing

| Field | Value |
|-------|-------|
| **Issues** | — |
| **PRs** | — |
| **Target** | Manual/scheduled triggers + FreeEed `.project` generation + auto-chain |

### Planned deliverables

- [ ] `api/collectors/core/trigger_manager.py` — manual + APScheduler cron triggers
- [ ] Trigger config schema (YAML): `connector`, `trigger`, `schedule`, `auto_chain`, `custodian`, `since`
- [ ] `api/collectors/processing/processing_engine_client.py` — wraps `run_freeeed_process()`
- [ ] `.project` file writer using **Java-canonical keys** (`input`, `custodian`, `stage`, `output-dir`, `solr_endpoint`, …)
- [ ] `ProcessProgress` guard — no concurrent Java runs during auto-chain
- [ ] `api/collectors/scripts/run_collectors.py` — CLI entry point
- [ ] `api/collectors/scripts/manual_trigger.py` — manual job trigger
- [ ] Stub interfaces for future event/webhook triggers (no impl required v1)
- [ ] Unit tests: `.project` generation, auto-chain mock subprocess, trigger scheduling

---

## M4 — Box connector

| Field | Value |
|-------|-------|
| **Issues** | — |
| **PRs** | — |
| **Target** | Reference connector implementation + tests |

### Planned deliverables

- [ ] `api/collectors/box/box_auth.py` — OAuth2 via SecurityManager
- [ ] `api/collectors/box/box_collector.py` — list, fetch, incremental sync, rate limits
- [ ] Registration: `@register_connector("box")`
- [ ] `handle_rate_limits` using Box `Retry-After` headers
- [ ] Unit tests with mocked HTTP (list, fetch, sync token, rate limit backoff)
- [ ] Connector health check (auth + lightweight API ping)

---

## M5 — Drive & Dropbox

| Field | Value |
|-------|-------|
| **Issues** | — |
| **PRs** | — |
| **Target** | Remaining v1 connectors + extension template |

### Planned deliverables

- [ ] `api/collectors/google_drive/gdrive_auth.py` + `gdrive_collector.py`
- [ ] Google Drive: `files.list`, `changes.getStartPageToken`, `files.get`
- [ ] `api/collectors/dropbox/dropbox_auth.py` + `dropbox_collector.py`
- [ ] Dropbox: `files/list_folder`, `files/list_folder/continue`, `files/download`
- [ ] `api/collectors/template/new_connector_template.py` — boilerplate for all `BaseCollector` methods
- [ ] Unit tests per connector (mocked HTTP)
- [ ] Rate-limit handling per vendor

---

## M6 — API & deployment

| Field | Value |
|-------|-------|
| **Issues** | — |
| **PRs** | — |
| **Target** | FastAPI surface, Docker, compose, env documentation |

### Planned deliverables

- [ ] Extend `api/main.py` with `/collect/*` routes:
  - `POST /collect/trigger`
  - `GET /collect/jobs/{job_id}`
  - `GET /collect/connectors`
  - `GET /collect/connectors/{id}/health`
  - `POST /collect/schedules` (optional v1)
- [ ] Extend `/health` with collector subsystem status
- [ ] Update `api/Dockerfile` — install `requirements-collectors.txt`
- [ ] Update `docker-compose.yml` — optional `./secrets:/run/secrets:ro` mount
- [ ] Env var documentation (OAuth client IDs, `COLLECTOR_ENCRYPTION_KEY`, etc.)
- [ ] Air-gapped install notes (offline wheelhouse, pre-seeded refresh tokens)

---

## M7 — Tests & docs

| Field | Value |
|-------|-------|
| **Issues** | — |
| **PRs** | — |
| **Target** | Integration test suite + architecture documentation |

### Planned deliverables

- [ ] Integration tests: `api/collectors/tests/integration/` (VCR cassettes or sandbox env; skip if unset)
- [ ] E2E: trigger → collect → filesystem write → `.project` generation
- [ ] Auto-chain integration test (mock Java subprocess)
- [ ] `api/collectors/docs/architecture.md` — diagram + component responsibilities
- [ ] `api/collectors/docs/connectors.md` — step-by-step new connector guide
- [ ] `api/collectors/docs/ingestion_flow.md` — data flow into FreeEed processing
- [ ] `api/collectors/CHANGELOG.md` — versioned change log with issue/PR refs
- [ ] README section: OAuth setup, example `curl` commands
- [ ] CI: integration job gated on repo secrets / `RUN_COLLECTOR_INTEGRATION` variable

---

## M8 — Public promotion

| Field | Value |
|-------|-------|
| **Issues** | — |
| **PRs** | — |
| **Target** | Curated merge to public `shmsoft/FreeEed` |

### Planned deliverables

- [ ] Full security review — no secrets, no telemetry, sanitized logs
- [ ] All M1–M7 unit tests passing in CI
- [ ] Integration tests passing or documented skip rationale
- [ ] Sanitized upstream PR to `shmsoft/FreeEed` (single squash/rebase preferred)
- [ ] Public-facing CHANGELOG / release notes entry
- [ ] Close private-repo issues with link to public PR
- [ ] Update this matrix: mark M0–M8 rows **Complete** with final issue/PR lists
- [ ] Decision recorded: ongoing private-first vs public-only for future connectors

---

## M9 — Collection Selection UI

| Field | Value |
|-------|-------|
| **Issues** | — |
| **PRs** | — |
| **Target** | Interactive case-scoped browser for external sources with OAuth, lazy tree, multi-select, and collection trigger |

### Planned deliverables

- [ ] Interactive case-scoped browser for Box, Google Drive, Dropbox
- [ ] In-app OAuth (WebView or embedded browser) wired to `/collect/oauth/*`
- [ ] Lazy folder tree, breadcrumbs, multi-select folders/files
- [ ] Project/custodian binding, selection validation, collection trigger + job status
- [ ] **UI approach (document both; default: web):**
  - **Recommended:** small web UI at `/collectors/` via FastAPI static mount — faster tree/OAuth UX, shared with API deployment
  - **Alternative:** Swing panel in `freeeed-processing` Java UI calling existing browse/select API
- [ ] Design spec: `api/collectors/docs/m9-collection-ui.md`
- [ ] Static scaffold: `api/static/collectors/index.html` (placeholder → full implementation)

---

## Issue ↔ PR cross-reference (living section)

Fill in as work progresses:

| Issue | Title | PR(s) | Milestone | Merged |
|-------|-------|-------|-----------|--------|
| — | — | — | — | — |

---

## Changelog pointers

| Version | Date | Milestone | Notes |
|---------|------|-----------|-------|
| — | — | — | First entry after M1 code lands in `api/collectors/CHANGELOG.md` |

---

See also: [WORKFLOW.md](WORKFLOW.md) for branch naming, label taxonomy, and private-repo setup.
