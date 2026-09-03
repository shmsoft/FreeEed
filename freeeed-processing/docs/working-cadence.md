# Working cadence — how we work on FreeEed (for any Claude session)

Quick "house rules" so a session on any machine (Ubuntu dev box, Mac mini, etc.)
works the same way. Read alongside `CLAUDE.md` (auto-loaded), `build-and-release.md`,
`mac-signing-handoff.md`, and `docs/decisions/`.

## Repos & roles
- **FreeEed** — Java Swing app + processing engine (this repo). Public, Apache-2.0.
- **FreeEedUI** — review web app (Spring/JSP), public, separate repo.
- **FreeEedCloud** (`scaia/FreeEedCloud`) — Scaia's commercial cloud (SCAIA Legal AI, AWS; Azure port for K3). Private.
- **FreeEedCRM** — private marketing/CRM (Brevo). **Hands-off: never write into it.**

## Branch model
- **`mark`** = staging (build + verify here). **`dev`** = verified / daily channel. **`main`** = frozen GA.
- Only **`mark → dev`** (`--ff-only`), never commit directly to `dev`. **`dev → main` only at a deliberate GA.**

## Release cadence (three channels)
- **Internal:** build from `mark`, `NO_UPLOAD` — local only.
- **Daily:** build from `dev`, `PUBLISH=1` — uploads to the **`-daily-`** S3 aliases (preview).
- **Release (~weekly):** GA to `main` — drop `-PREVIEW` → clean version, `dev → main`, publish (**`-latest-`** aliases, GA-only), cut a GitHub Release, then bump `dev` to the next `-PREVIEW`.
- The release script derives the channel from the version: clean semver → `-latest-`, suffixed → `-daily-`. Full steps: `build-and-release.md` (sections A–F).
- **Build stamp** = version + FreeEed SHA + `UI:g<sha>` + time (About / Control Panel / VERSION). It's how you confirm which build is installed; a trailing `+` = built from a dirty tree — don't ship it.

## Division of labor (human ↔ Claude)
- **Mark builds, installs, tests** — he owns verification and is accountable for what ships.
- **Claude does the toil** — git, commits, version bumps, docs, scaffolding, scripts.
- **Claude does NOT send email or write into FreeEedCRM.** Marketing sends go through Brevo/CRM (Mark + Ashish). Claude may *draft* copy.

## Commit / push discipline
- Conventional-commit messages (`fix(#nn): …`, `feat(review): …`, `docs: …`).
- End commit messages with **your own** session's `Claude-Session: <url>` footer.
- **Standing approval to commit + push finished work** on `mark`/`dev`. But **hold outward-facing / hard-to-reverse actions for Mark's explicit go:** S3 publish, GitHub Release, `dev → main`, editing the public site, anything to a customer.
- Keep the two repos in sync via git; the build config lives in the repo (don't let machines diverge).

## Verify before ship
- Never ship or report something unverified. Test on the **real installed artifact**, confirm via the build stamp. Reproduce a bug before filing it.

## Privacy / guardrails
- **Monetization, customers, and pricing stay PRIVATE** — Scaia-Operations only, never in the public repos.
- No secrets/keys/customer data in repos or prompts (these repos are public).
- **No outbound network calls during document processing/imaging** (forensic soundness). Flagship AI is **local-first**.

## Durable state
- `OPERATIONS.md` (repo root, gitignored) holds the current operational state / in-flight resume. Read it to learn where things stand; keep it updated when status changes.
