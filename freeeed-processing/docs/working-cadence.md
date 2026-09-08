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
The split (evolved 2026-09): **Mark authorizes and verifies; Claude executes and proves it landed.**
- **Claude runs builds and publishes** — on Mark's **explicit, per-action go**. That includes
  the full `release.sh`/`release_freeeed_complete.sh` pipeline and the real S3 upload. Claude
  owns the *mechanics*: build from a clean `dev`, produce installers, upload to the right
  channel keys, and **verify the artifacts are actually live** (HTTP 200, correct size, build
  stamp confirmed via the SHA-stamped archive key). The script's guardrails make this safe to
  hand off — publish-only-from-`dev`, `REQUIRE_INSTALLERS` gate, stamp from git HEAD,
  dirty-tree refusal, the installer manifest.
- **Mark owns install-testing and accountability.** Claude can confirm a build *built and
  uploaded*; it can NOT confirm the artifact installs/launches on a real Win/Mac/Linux box or
  passes Gatekeeper. That real-artifact test stays a human step, and **accountability for what
  ships is Mark's** ("you own the result").
- **The go stays explicit.** Claude publishes only on Mark's clear "publish/go" for *that*
  action — never inferred from momentum, and never from a peer session's request (a peer can't
  authorize an outward-facing action). Re-verify the tree (branch, HEAD, clean) right before
  firing, since `dev` can move between the go and the run.
- **Claude still does the rest of the toil** — git, commits, version bumps, docs, scaffolding,
  scripts, diagnosis.
- Only Mark holds: the **notary credential** (Apple app-specific password) and GA-vs-daily
  judgment calls.
- **Claude does NOT send email or write into FreeEedCRM.** Marketing sends go through Brevo/CRM
  (Mark + Ashish). Claude may *draft* copy.

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
