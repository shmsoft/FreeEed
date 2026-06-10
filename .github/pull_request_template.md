## Summary

<!-- Brief description of the change -->

## Linked issues

<!-- Required: link at least one issue -->
- Closes #

## Milestone

<!-- e.g. M1 — Core framework -->
- [ ] M0 — GitHub & scaffolding
- [ ] M1 — Core framework
- [ ] M2 — Ingestion & storage
- [ ] M3 — Trigger & processing
- [ ] M4 — Box connector
- [ ] M5 — Drive & Dropbox
- [ ] M6 — API & deployment
- [ ] M7 — Tests & docs
- [ ] M8 — Public promotion

## Labels

<!-- Apply before merge -->
- Area: `area:core` | `area:ingestion` | `area:storage` | `area:connectors` | `area:api` | `area:security` | `area:ci` | `area:docs`
- Connector (if applicable): `connector:box` | `connector:gdrive` | `connector:dropbox` | `connector:template`
- Type: `type:feature` | `type:bug` | `type:tech-debt` | `type:spike` | `type:docs`

## Test plan

- [ ] `pytest api/collectors/tests/unit` passes locally (or N/A — scaffold not yet present)
- [ ] Integration tests run / skipped appropriately (secrets not required for this PR)
- [ ] Manual smoke test described below (if applicable)

<!-- Describe manual verification steps -->

## Security checklist

- [ ] No secrets, tokens, or credentials committed
- [ ] No external telemetry (Sentry, Datadog, etc.) added
- [ ] Log output sanitized (tokens, emails, PII paths redacted)
- [ ] Outbound network limited to target platform APIs (no unexpected endpoints)

## Public promotion ready

<!-- Is this change safe to cherry-pick or PR into public shmsoft/FreeEed? -->
- [ ] **Yes** — ready for upstream promotion (sanitized description, no internal-only references)
- [ ] **No** — private-repo only until a later milestone

**Notes:**

<!-- If "No", explain what remains before public promotion -->
