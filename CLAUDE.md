# CLAUDE.md — How we work on FreeEed

Guidance for anyone (human or Claude) working in this repo. Non-sensitive; lives in the
open-source repo. New contributors: read this first, then `CONTRIBUTING.md`,
`for_developers_only.md`, and the design docs under `docs/decisions/`.

## What FreeEed is
Open-source (Apache-2.0) eDiscovery: process, search, review, and question sensitive
documents **on your own machine, without sending them to anyone.** Mission = professional
eDiscovery + AI that is affordable, **private, and self-hosted**. Audience: lawyers,
investigators, small firms, forensic examiners.

## Repo layout
- `freeeed-processing/` — the Java processing engine (`org.freeeed.*`): ingest, extraction
  (Tika), OCR, imaging (doc→PDF), PST/OST, indexing (Lucene/Solr). This is the heart.
- `freeeed-test/` — test module.
- `ui/`, `api/`, `src/` — supporting Swing UI / helper code.
- `docs/` — documentation; **`docs/decisions/`** holds the design/decision records (read
  these before proposing architecture changes).
- Root scripts — `start_dev_services.sh` (Solr + Tika for dev), `release_freeeed_complete.sh`
  (builds the distributable pack + installers), `start_all.sh`, etc.
- **FreeEedUI** (the web review app) and **ai_advisor** (the commercial Python AI layer) are
  currently **separate repos**. FreeEedUI is cloned *next to* this folder for review builds.
  Consolidating FreeEedUI into this repo is planned — see
  `docs/decisions/refactoring-plan.md`.

## Build & run (developer)
- **Toolchain:** JDK 11+ (the build targets Java 11; JDK 17 works), Maven 3.8+.
- **First-time:** a few non-Maven-Central jars must be installed into your local `~/.m2`
  (IBM Notes, JPST) — see `for_developers_only.md` for the exact `mvn install:install-file`
  commands. PST processing on \*nix uses `readpst` (install per the wiki).
- **Build:** `mvn -pl freeeed-processing -am install` (or a full `mvn install`).
- **Dev services:** run `./start_dev_services.sh` from a complete-pack directory to bring up
  Solr + Tika before running the app.
- **Run / package:** `./release_freeeed_complete.sh` assembles the pack and installers.
- Secrets/config go in `settings.properties` (copied from the template) — **gitignored**,
  never commit it.

## Branch workflow (see CONTRIBUTING.md for the full version)
- `dev` — shared integration branch; **all builds/releases cut from here.**
- `mark` — Mark's working branch.
- `main` — public/protected; do not merge to it unless explicitly asked.
- **Contributors (interns, external): work on a `feature/<short-desc>` branch or a fork, and
  open a PR into `dev`. Do NOT push directly to `mark`, `dev`, or `main`.** Mark reviews and
  merges. This is the review gate.

## Design/decision records — read before big changes
- `docs/decisions/refactoring-plan.md` — the 3-phase roadmap (1: repo consolidation + polished
  cross-platform release; 2: FreeEed Viewer hand-off; 3: local-first AI).
- `docs/decisions/pst-processing.md` — PST/OST engine direction (libpff, scale-out "Piranha").
- `docs/decisions/local-ai-architecture.md` — Phase 3 local-AI build spec (Ollama/vLLM,
  OpenAI-compatible, local OCR, egress attestation).
- `docs/local-ai-cjis-briefing.md` — why local-first AI (compliance framing).

## Core principles & gotchas
- **No outbound network calls during document processing/imaging** — forensic soundness.
  Processing must never fetch remote content. Treat this as a hard rule.
- **AI is local-first** — the flagship AI runs a **local** model (Ollama, OpenAI-compatible);
  external/cloud AI is secondary. "Nothing leaves the machine," and it can be proven.
- **Build stamp = git commit SHA + build time** (see the About dialog / `-version` / pack
  `VERSION`), so the semantic version (currently `10.8.5-SNAPSHOT` on `dev`) is bumped at
  milestones, not per build.
- **Distribution is moving to a bundled JRE** (jpackage/jlink) so users don't need Java
  installed — see the refactoring plan.
- Review web app serves on **port 8090** (avoids an 8080 conflict).
- Default output goes to a **writable user dir** (`~/FreeEed-output`), not `/out`.

## Working with Claude on this project — the discipline
The through-line: **Claude is a force multiplier on your effort, not a substitute for your
understanding.** Lean on it for the mechanical work — commands, boilerplate, formatting, first
drafts, remembering how things work — so you spend your attention on what matters. But you own
the result.

1. **Delegate toil, own judgment.** Claude drafts and looks things up; *you* decide what's
   correct and are accountable for it. "Claude said so" is never why something shipped.
2. **Verify before you ship.** Never file a bug, commit code, or send output you haven't
   checked yourself. Run it, read it, confirm it. This is the whole game.
3. **Reproduce before you report.** A bug isn't real until you can make it happen again and
   write the exact steps. Claude can help you write it up — it can't do the observing for you.
4. **Give Claude the context.** Point it at this file and the `docs/decisions/` records; let it
   *read before it acts*. When a change touches architecture, check the relevant decision
   record first and keep it updated.
5. **Small, reviewable steps.** One issue / one change at a time, on a `feature/<name>` branch
   → PR into `dev`. Nothing goes straight to `mark`/`dev`/`main`.
6. **Keep it clean and private.** Use **your own** Claude Code login — never another person's
   account, session, or API key. No customer data, credentials, or secrets in the repo or in
   prompts (this repo is public open-source). Don't commit generated artifacts (`output/`,
   `logs/`, `settings.properties`, build output).
7. **When you're stuck, ask.** Getting unstuck fast beats spinning silently — that's true for
   asking Claude *and* for asking Mark.

Details matter — especially in testing, the small stuff *is* the value. Using Claude well
gives you **more** attention for the details, not permission to skip them.
