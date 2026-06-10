# Connectors (v1)

## Registered Connectors

| ID | Provider | Auth | Env Prefix |
|----|----------|------|------------|
| `box` | Box.com | OAuth2 | `COLLECTOR_BOX_*` |
| `google_drive` | Google Drive | OAuth2 | `COLLECTOR_GOOGLE_DRIVE_*` |
| `dropbox` | Dropbox | OAuth2 | `COLLECTOR_DROPBOX_*` |
| `template` | Example stub | API token | — |

## Required Secrets

Each connector expects at minimum:

- `access_token` — current OAuth access token
- `refresh_token` — for automatic refresh (recommended)

Provider app credentials (shared across jobs):

- Box: `BOX_CLIENT_ID`, `BOX_CLIENT_SECRET`
- Google Drive: `GOOGLE_DRIVE_CLIENT_ID`, `GOOGLE_DRIVE_CLIENT_SECRET`
- Dropbox: `DROPBOX_CLIENT_ID`, `DROPBOX_CLIENT_SECRET`

Dropbox browse/collect requires app scopes **`files.metadata.read`** and **`files.content.read`**. See [browse-and-select.md](./browse-and-select.md#dropbox-dev-setup-freeeedcollector) for dev setup.

Job `config` JSON may include:

- `folder_id` / `path` — scope collection to a folder
- `since` — ISO datetime for incremental listing
- `selected_items` — list of `{id, type, name, path}` for browse/select collection
- `selected_item_ids` / `selected_paths` — shorthand file-only selection
- `mock_items` / `mock_browse` — test-only inline item trees
- `processing` — auto-chain project settings (when `auto_chain=true`)

See [browse-and-select.md](./browse-and-select.md) for project-scoped browse/select API and dev secret setup.

## Rate Limits

Connectors honor `Retry-After` headers. Repeated failures open a per-connector circuit breaker (default threshold: 5).

## Adding a Connector

Copy `template/new_connector_template.py`, implement all ABC methods, decorate with `@register_connector("your_id")`, and import the module in `main.py`.
