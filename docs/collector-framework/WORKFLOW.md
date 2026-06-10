# External Data Collection Framework — GitHub Workflow

This document describes how collector-framework work is tracked, branched, reviewed, and promoted between the **public** FreeEed platform repo and the **private** companion repo.

## Repository strategy

| Repository | Visibility | Role |
|------------|------------|------|
| [shmsoft/FreeEed](https://github.com/shmsoft/FreeEed) | **Public** | Upstream eDiscovery platform; receives merged collector code only when release-ready |
| `shmsoft/FreeEed-collectors` (planned) | **Private** | All development branches, PRs, issues, CI, project boards, and internal discussion |

**Why a private companion repo?** The public FreeEed repo cannot host hidden issues, draft PRs, or internal milestone tracking. Collector work stays segregated until deliberately promoted at **M8**.

### What lives where

| Artifact | Private repo | Public repo (now) | Public repo (after M8) |
|----------|--------------|-------------------|------------------------|
| Issue templates (`.github/ISSUE_TEMPLATE/`) | Yes | Staged locally / copy on bootstrap | Optional subset |
| PR template | Yes | Staged locally | Optional |
| `collectors-ci.yml` | Yes | Staged locally | Yes (when `api/collectors/` lands) |
| `api/collectors/` source | Yes (during dev) | No until promotion | Yes |
| `docs/collector-framework/` | Yes | Yes (tracking docs only) | Yes |

The `.github/` and `docs/collector-framework/` files in this tree can be **copied wholesale** when creating `FreeEed-collectors`, or kept here as a reference until the private repo exists.

---

## Milestones (M0–M9)

Create these milestones in the private repo (GitHub → Issues → Milestones):

| Milestone | Scope | Primary deliverables |
|-----------|-------|----------------------|
| **M0 — GitHub & scaffolding** | Work tracking setup | Private repo, GitHub Project v2, labels, issue/PR templates, CI skeleton, `api/collectors/` package scaffold |
| **M1 — Core framework** | Foundation | `BaseCollector` ABC, types, registry, orchestrator skeleton, logging/state/security managers |
| **M2 — Ingestion & storage** | Data path | Ingestion pipeline, streaming engine, metadata normalizer, filesystem + SQLite stores |
| **M3 — Trigger & processing** | FreeEed integration | Trigger manager (manual + scheduled), `.project` writer, `processing_engine_client`, auto-chain |
| **M4 — Box connector** | Reference connector | Box auth, list/fetch/sync, rate limits, unit tests |
| **M5 — Drive & Dropbox** | Remaining v1 connectors | Google Drive + Dropbox collectors, `new_connector_template.py` |
| **M6 — API & deployment** | Surface + ops | FastAPI `/collect/*` routes, Dockerfile + docker-compose updates, env/secrets docs |
| **M7 — Tests & docs** | Quality + knowledge | Integration tests (VCR/sandbox), `architecture.md`, `connectors.md`, `ingestion_flow.md` |
| **M8 — Public promotion** | Upstream merge | Curated PR to public `shmsoft/FreeEed`, sanitized release notes, CHANGELOG |
| **M9 — Collection Selection UI** | Interactive browse/select | Case-scoped browser for Box/Drive/Dropbox; in-app OAuth; lazy tree, breadcrumbs, multi-select; project/custodian binding, validation, trigger + job status. **Default:** small web UI via FastAPI static mount; **alternative:** Swing panel |

Align every issue and PR with exactly one milestone unless explicitly spanning tightly coupled work (document in both issue bodies).

---

## Label taxonomy

Apply labels consistently for filtering and project automation.

### Area

| Label | Use for |
|-------|---------|
| `area:core` | Registry, orchestrator, base types, trigger manager |
| `area:ingestion` | Pipeline, streaming, metadata normalization |
| `area:storage` | Filesystem/SQLite stores, state persistence |
| `area:connectors` | Box, Drive, Dropbox, template connector |
| `area:api` | FastAPI routes, request/response models |
| `area:security` | Secrets, token encryption, log sanitization |
| `area:ci` | GitHub Actions, test gates |
| `area:docs` | Markdown docs, README sections |

### Connector

| Label | Use for |
|-------|---------|
| `connector:box` | Box.com connector |
| `connector:gdrive` | Google Drive connector |
| `connector:dropbox` | Dropbox connector |
| `connector:template` | New-connector boilerplate |

Use **no connector label** (or `connector: none` in issue forms) for cross-cutting framework work.

### Type

| Label | Use for |
|-------|---------|
| `type:feature` | New capability |
| `type:bug` | Defect fix |
| `type:tech-debt` | Refactor, cleanup |
| `type:spike` | Time-boxed research |
| `type:docs` | Documentation-only change |

### Priority

| Label | Use for |
|-------|---------|
| `priority:P0` | Blocker / production break |
| `priority:P1` | Current milestone must-have |
| `priority:P2` | Nice-to-have / next milestone |

### Epic & status

| Label | Use for |
|-------|---------|
| `epic:collector-framework` | **All** initiative issues |
| `status:blocked` | Waiting on dependency or decision |
| `status:needs-review` | PR ready for maintainer review |

---

## GitHub Project (v2)

**Board name:** External Data Collection Framework

| Column / status | Meaning |
|-----------------|---------|
| Backlog | Planned, not started |
| In progress | Active implementation |
| In review | Open PR awaiting review |
| Blocked | External dependency |
| Done | Merged to private repo `main` |

Link every issue and PR to this project. Update status when work moves forward.

---

## Branch naming

| Pattern | Use for |
|---------|---------|
| `feature/collector-<short-description>` | New features, connectors, subsystems |
| `fix/collector-<short-description>` | Bug fixes |
| `docs/collector-<short-description>` | Documentation-only changes |

Examples:

- `feature/collector-box-auth`
- `feature/collector-ingestion-pipeline`
- `fix/collector-rate-limit-backoff`
- `docs/collector-oauth-setup`

**Rules:**

- Branch from private repo `main` (or `dev` if the private repo mirrors public conventions).
- Do **not** push in-progress collector feature branches to the public `FreeEed` remote.
- One logical change set per branch; prefer one issue per PR.

---

## Traceability rules

1. **One issue per PR** — use `Closes #NNN` in the PR description. For tightly coupled work, `Closes #a, Closes #b` is acceptable if documented in both issues.
2. **No drive-by commits** — every commit should relate to the linked issue.
3. **Commit message format:**
   ```
   <type>(collector): <short summary> (#<issue>)
   ```
   Types: `feat`, `fix`, `docs`, `test`, `refactor`, `ci`, `chore`.

   Example: `feat(collector): add Box OAuth refresh (#42)`
4. **Issue title prefix:** `[COLLECTOR]` on all framework issues.
5. **Project board** — move issues when status changes (manual or via GitHub Actions `actions/add-to-project`).
6. **CHANGELOG** — maintain `api/collectors/CHANGELOG.md` in the private repo; entries reference issue/PR numbers.
7. **Traceability matrix** — update [`traceability.md`](traceability.md) at milestone boundaries.

---

## Creating the private repo with GitHub CLI

Prerequisites: [GitHub CLI](https://cli.github.com/) authenticated (`gh auth login`).

### 1. Create the private repository

```bash
gh repo create shmsoft/FreeEed-collectors \
  --private \
  --description "Private development repo for FreeEed External Data Collection Framework" \
  --clone
```

If org policy requires a personal fork first, create under your account and transfer later.

### 2. Bootstrap from FreeEed upstream

```bash
cd FreeEed-collectors
git remote add upstream https://github.com/shmsoft/FreeEed.git
git fetch upstream
git checkout -b main
git pull upstream main   # or merge a specific baseline commit
```

Copy staged artifacts from the public repo (or this local tree):

```bash
# From your FreeEed clone
cp -r .github/ISSUE_TEMPLATE .github/pull_request_template.md \
      .github/workflows/collectors-ci.yml .github/CODEOWNERS \
      /path/to/FreeEed-collectors/.github/
cp -r docs/collector-framework /path/to/FreeEed-collectors/docs/
```

Commit and push:

```bash
git add .github docs/collector-framework
git commit -m "chore(collector): bootstrap GitHub tracking artifacts (M0)"
git push -u origin main
```

### 3. Create milestones

```bash
for title in \
  "M0 — GitHub & scaffolding" \
  "M1 — Core framework" \
  "M2 — Ingestion & storage" \
  "M3 — Trigger & processing" \
  "M4 — Box connector" \
  "M5 — Drive & Dropbox" \
  "M6 — API & deployment" \
  "M7 — Tests & docs" \
  "M8 — Public promotion" \
  "M9 — Collection Selection UI"
do
  gh api repos/shmsoft/FreeEed-collectors/milestones -f title="$title" -f state=open
done
```

### 4. Create labels

```bash
# Area
for label in core ingestion storage connectors api security ci docs; do
  gh label create "area:$label" --repo shmsoft/FreeEed-collectors --color "1D76DB" --force
done

# Connector
for label in box gdrive dropbox template; do
  gh label create "connector:$label" --repo shmsoft/FreeEed-collectors --color "5319E7" --force
done

# Type
for label in feature bug tech-debt spike docs; do
  gh label create "type:$label" --repo shmsoft/FreeEed-collectors --color "0E8A16" --force
done

# Priority
for label in P0 P1 P2; do
  gh label create "priority:$label" --repo shmsoft/FreeEed-collectors --color "B60205" --force
done

# Epic & status
gh label create "epic:collector-framework" --repo shmsoft/FreeEed-collectors --color "006B75" --force
gh label create "status:blocked" --repo shmsoft/FreeEed-collectors --color "D93F0B" --force
gh label create "status:needs-review" --repo shmsoft/FreeEed-collectors --color "FBCA04" --force
```

### 5. Create GitHub Project v2

Use the GitHub UI: **Projects → New project → Board** → name **External Data Collection Framework**. Add columns: Backlog, In progress, In review, Blocked, Done.

Optionally link the project to the repo under project settings.

### 6. Seed M0 issues

Open tasks using the **Collector Task** template, for example:

- `[COLLECTOR] Create milestones and labels`
- `[COLLECTOR] Configure collectors-ci workflow`
- `[COLLECTOR] Scaffold api/collectors/ package layout`

---

## Promotion path to public repo (M8)

When a milestone (typically **M8**) is release-ready:

1. **Sanitize** — remove internal ticket references, customer names, and sandbox credentials from PR title/body.
2. **Verify security checklist** — no secrets, no telemetry, logs sanitized (see PR template).
3. **Run full CI** — unit tests required; integration tests with recorded cassettes or documented skip.
4. **Open upstream PR** to `shmsoft/FreeEed` `main` or `dev` (per team convention):
   - Single squash/rebase PR preferred for the initial promotion.
   - Include user-facing CHANGELOG entry and docs links.
5. **Close promoted issues** in the private repo; note the public PR URL in each issue.
6. **Decide ongoing model** — either continue private-first for v2 connectors, or shift new work to public repo with `area:connectors` labels.

Per-milestone partial promotion is allowed (e.g. docs-only PRs) if the security and sanitization bars are met.

---

## Agent / Cursor instructions

When implementing collector work:

1. Create or reference a GitHub issue in the **private** repo before coding.
2. Branch using `feature/collector-*`, `fix/collector-*`, or `docs/collector-*`.
3. Open PR against private repo `main`; link issue; set milestone and labels.
4. Do not push feature branches to the public FreeEed remote.
5. Record the PR URL in the issue for audit trail.
6. Update [`traceability.md`](traceability.md) at milestone completion.

---

## Related files

| File | Purpose |
|------|---------|
| [`.github/ISSUE_TEMPLATE/`](../.github/ISSUE_TEMPLATE/) | Issue forms (feature, bug, task) |
| [`.github/pull_request_template.md`](../.github/pull_request_template.md) | PR checklist |
| [`.github/workflows/collectors-ci.yml`](../.github/workflows/collectors-ci.yml) | CI for collector unit/integration tests |
| [`traceability.md`](traceability.md) | Milestone → issues → PRs → deliverables matrix |
