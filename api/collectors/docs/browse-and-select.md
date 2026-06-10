# Browse & Select API (Project-Scoped)

This document describes the API layer for remote browse/select workflows. A future Swing or web UI should call these endpoints; no Swing changes are included in this phase.

## Project / Case Mapping

FreeEed cases are defined by `.project` files (Java `Project` / `ParameterProcessing`):

| `.project` key | Role |
|----------------|------|
| `project-code` | Stable case identifier (e.g. `1`, `0002`) — use as `project_id` in API paths |
| `project-name` | Human-readable case name |
| `custodian` | Comma-separated custodian list for collected evidence |

When collection is triggered with `auto_chain=true`, the orchestrator writes a `.project` file via `ProcessingEngineClient`, passing `project_code`, `project_name`, and `custodian` from `config.processing`.

**Selections are keyed by `(project_id, connector_id)`** in SQLite (`project_selections` table). They store remote item references only — files are downloaded at collection run time into `/data/input/collections/{job_id}/`.

## Java UI Integration Hook

Swing screens under `freeeed-processing/src/main/java/org/freeeed/ui/` do not yet call these endpoints. Recommended flow for a future UI:

1. Read `project-code` from the active `.project` file → `project_id`
2. `GET /collect/oauth/{connector_id}/authorize?project_id={project_id}` — browser OAuth (Dropbox first)
3. `POST /collect/projects/{project_id}/connect/{connector_id}` — verify stored tokens
4. `GET /collect/projects/{project_id}/browse/{connector_id}?parent_id=...` — populate tree
5. `POST /collect/projects/{project_id}/selections` — persist user picks
6. `POST /collect/projects/{project_id}/trigger` — collect selected items only

Use `http://localhost:8000` (or deployed API base URL) from Java via HTTP client.

## API Endpoints

### OAuth (Dropbox v1)

Per-project tokens are stored in SQLite (`oauth_tokens` table) after a successful callback. Global `secrets/dropbox.json` tokens remain supported as a fallback when no project token exists.

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/collect/oauth/{connector_id}/authorize` | Query: `project_id`, optional `redirect_after`. Redirects to provider with PKCE. |
| GET | `/collect/oauth/{connector_id}/callback` | Provider callback; exchanges code, stores tokens. HTML success page or `?format=json`. |

**Dropbox OAuth flow (dev):**

1. Ensure `secrets/dropbox.json` has `client_id` and `client_secret` (no short-lived access token required).
2. Register redirect URI in [Dropbox App Console](https://www.dropbox.com/developers/apps):  
   `http://localhost:8000/collect/oauth/dropbox/callback`  
   (or `http://localhost:4001/collect/oauth/dropbox/callback` if you run uvicorn on port 4001 and set `COLLECTOR_OAUTH_REDIRECT_BASE=http://localhost:4001`).
3. Open in a browser:  
   `http://localhost:8000/collect/oauth/dropbox/authorize?project_id=1`
4. Approve access in Dropbox; callback stores refresh + access tokens for project `1`.
5. Connect and browse use stored tokens automatically:

```bash
curl -s -X POST http://localhost:8000/collect/projects/1/connect/dropbox
curl -s "http://localhost:8000/collect/projects/1/browse/dropbox"
```

Environment:

| Variable | Purpose |
|----------|---------|
| `COLLECTOR_OAUTH_REDIRECT_BASE` | Base URL for callback (default `http://localhost:8000`) |
| `DROPBOX_CLIENT_ID` / `DROPBOX_CLIENT_SECRET` | App credentials (or `secrets/dropbox.json`) |
| `COLLECTOR_ENCRYPTION_KEY` | Optional Fernet key to encrypt tokens at rest |

### Connect

```
POST /collect/projects/{project_id}/connect/{connector_id}
```

Optional body:

```json
{
  "access_token": "dev-token",
  "refresh_token": "dev-refresh",
  "config": { "mock_refresh": true }
}
```

Responses:

- `200` — `{ "project_id", "connector_id", "connected": true, "message" }`
- `503` — credentials not configured
- `401` — authentication failed

### Browse

```
GET /collect/projects/{project_id}/browse/{connector_id}?parent_id={optional}
```

Optional query params: `access_token`, `refresh_token` (dev overrides).

Response:

```json
{
  "project_id": "1",
  "connector_id": "box",
  "parent_id": "0",
  "items": [
    {
      "id": "123",
      "name": "Contracts",
      "path": "/Contracts",
      "type": "folder",
      "size": null,
      "modified_at": "2024-01-15T10:00:00",
      "connector_id": "box",
      "mime_type": null
    }
  ]
}
```

Root parent IDs: `box` → `0` or omit; `google_drive` → `root` or omit; `dropbox` → `""` or omit (path-based).

### Save Selections

```
POST /collect/projects/{project_id}/selections
```

```json
{
  "connector_id": "box",
  "items": [
    { "id": "folder-1", "type": "folder", "name": "Contracts", "path": "/Contracts" },
    { "id": "file-9", "type": "file", "name": "memo.pdf", "path": "/memo.pdf" }
  ]
}
```

### Get Selections

```
GET /collect/projects/{project_id}/selections?connector_id=box
```

### Trigger Selected Collection

```
POST /collect/projects/{project_id}/trigger
```

```json
{
  "connector_id": "box",
  "auto_chain": true,
  "config": {
    "processing": {
      "project_code": "1",
      "project_name": "Enron review",
      "custodian": "jsmith"
    }
  }
}
```

Loads saved selections for `(project_id, connector_id)`, sets `config.selected_items`, and starts a background job. Folders are expanded recursively at run time.

## Dev Secrets

Use OAuth for long-lived refresh tokens, or environment variables / mounted JSON for manual dev tokens.

### Environment Variables

| Connector | Token vars | App creds |
|-----------|------------|-----------|
| `box` | `COLLECTOR_BOX_ACCESS_TOKEN`, `COLLECTOR_BOX_REFRESH_TOKEN` | `BOX_CLIENT_ID`, `BOX_CLIENT_SECRET` |
| `google_drive` | `COLLECTOR_GOOGLE_DRIVE_ACCESS_TOKEN`, `COLLECTOR_GOOGLE_DRIVE_REFRESH_TOKEN` | `GOOGLE_DRIVE_CLIENT_ID`, `GOOGLE_DRIVE_CLIENT_SECRET` |
| `dropbox` | `COLLECTOR_DROPBOX_ACCESS_TOKEN`, `COLLECTOR_DROPBOX_REFRESH_TOKEN` | `DROPBOX_CLIENT_ID`, `DROPBOX_CLIENT_SECRET` |

Set `COLLECTOR_SECRETS_DIR=./secrets` to load file-based secrets.

### `./secrets/box.json` Example

```json
{
  "access_token": "PLACEHOLDER_ACCESS_TOKEN",
  "refresh_token": "PLACEHOLDER_REFRESH_TOKEN",
  "client_id": "your-box-app-client-id",
  "client_secret": "your-box-app-client-secret"
}
```

Equivalent files: `./secrets/google_drive.json`, `./secrets/dropbox.json`.

Copy `secrets/dropbox.json.example` → `secrets/dropbox.json` and fill in values. Real JSON files are gitignored; only `*.example` templates are committed.

### Dropbox Dev Setup (FreeEed.Collector)

1. In the [Dropbox App Console](https://www.dropbox.com/developers/apps), open app **FreeEed.Collector**.
2. Enable scopes: **`files.metadata.read`**, **`files.content.read`**, **`account_info.read`**.
3. Add redirect URI matching your API base, e.g.  
   `http://localhost:8000/collect/oauth/dropbox/callback`  
   (existing `http://localhost:4001` entries must include the full path if using port 4001).
4. Create `secrets/dropbox.json` with app key/secret only:

```json
{
  "client_id": "YOUR_APP_KEY",
  "client_secret": "YOUR_APP_SECRET"
}
```

5. Start the API (`docker compose up freeeed-api` or local uvicorn on port 8000/4001).
6. Complete OAuth in the browser (step 3 in **OAuth** section above).

For local Python runs outside Docker, export `COLLECTOR_SECRETS_DIR=./secrets` and `COLLECTOR_OAUTH_REDIRECT_BASE` (see `secrets/.env.local.example`).

Browse root uses an empty `parent_id` for Dropbox (path-based). Subfolders: `?parent_id=/FolderName`.

### Mock Mode (tests / no creds)

Pass tokens in the connect/browse request body or query string, with `config.mock_refresh: true` and `config.mock_browse` / `config.mock_items` on the connector for unit tests.

## Error Handling

| Code | Meaning |
|------|---------|
| 503 | No secrets configured for connector |
| 401 | Token invalid or refresh failed |
| 400 | No selections saved before trigger |
| 404 | Unknown `connector_id` or job |

## M9 UI (planned)

A dedicated **Collection Selection UI** (milestone **M9**) will provide an interactive, case-scoped experience on top of the endpoints documented above. See [`m9-collection-ui.md`](m9-collection-ui.md) for the design spec.

**Planned capabilities:**

- Lazy folder tree with breadcrumbs and multi-select (folders and files)
- In-app OAuth via WebView or embedded browser (`GET /collect/oauth/{connector_id}/authorize`)
- Project/custodian binding from the active `.project` file
- Selection validation before `POST /collect/projects/{project_id}/trigger`
- Job status polling via `GET /collect/jobs/{job_id}`

**UI options (both documented; web is the default recommendation):**

| Option | Location | Notes |
|--------|----------|-------|
| **Web UI (recommended)** | `api/static/collectors/` served at `/collectors/` | Fastest path for tree rendering and OAuth redirects; ships with the FastAPI container |
| **Swing panel** | `freeeed-processing` Java UI | Reuses the same REST API; suitable when desktop-only workflow is required |

Placeholder scaffold: `api/static/collectors/index.html` — minimal browse call against this API; full implementation tracked under M9.
