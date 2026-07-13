# New Contributor / Intern Onboarding

Welcome! This gets you from zero to a first merged pull request. Read `CLAUDE.md` (repo root)
first for how the project works; this note is the hands-on checklist.

## 0. Access & accounts (do these first)
- **GitHub username:** send it to Mark (an email address isn't enough to grant repo access).
  You'll get **least-privilege** access: read + open pull requests, **no** push to protected
  branches.
- **Claude Code:** use **your own** login (your own account or an assigned team seat). Never
  use anyone else's account, session, or API key.
- **Contributor License Agreement (CLA):** sign the project CLA (`CLA.md`) before your first
  PR is merged — it lets your work ship cleanly under Apache-2.0. (This is separate from any
  internship/HR paperwork.)

## 1. Clone
```bash
git clone https://github.com/<your-fork-or-shmsoft>/FreeEed.git
cd FreeEed
```

## 2. Make a feature branch (never work on mark/dev/main)
```bash
git checkout dev && git pull
git checkout -b feature/<short-description>
```
You commit to `feature/<name>` and open a **PR into `dev`**. Mark reviews and merges.

## 3. Build
- Install **JDK 11+** (JDK 17 is fine) and **Maven 3.8+**.
- One-time: install the non-central jars into `~/.m2` (IBM Notes, JPST) — commands in
  `for_developers_only.md`.
- Build:
  ```bash
  mvn -pl freeeed-processing -am install
  ```
- Note the direction of travel: releases are moving to a **bundled JRE** (jpackage/jlink) so
  end users won't need Java installed — see `docs/decisions/refactoring-plan.md`.

## 4. Run FreeEed once
- Start dev services (Solr + Tika) from a complete-pack directory:
  ```bash
  ./start_dev_services.sh
  ```
- Launch the app (`./start_all.sh` / `./start_freeeed.sh`), register on first launch, and
  process a small sample so you see the ingest → review flow end to end. The review web app
  serves on **port 8090**.

## 5. Your first assignment: Windows QA on every `dev` build
Your starting role is **testing FreeEed on Windows against each new `dev` build** — install
the daily Windows build, run the checklist, and file any problems as GitHub issues. Follow
**`docs/windows-test-plan.md`** (it has the download link, the test cases, and a bug-report
template). This gets you deep into the product fast and directly feeds Mark's fix list. You
don't need to build from source for this — the steps below are for when you move on to code.

## 6. A good first code issue (later, low blast radius)
When you move from testing to code, start with something that teaches the build, the module
layout, and the PR flow **without** risking production behavior. Good starters:
- **Add unit tests** for a small, self-contained utility in `freeeed-processing` via the
  `freeeed-test` module (great first PR — safe, reviewable, and it forces you to learn the
  build).
- **Docs fixes** — tighten `for_developers_only.md` or a wiki page you found confusing while
  onboarding (you're the best-positioned person to spot gaps).
- A **small, scoped item** from the refactoring / PST / UI / test backlog — ask Mark to point
  you at one sized for a first landing.

Avoid, at first: anything touching the processing/imaging core, licensing/activation, or
release scripts.

## 7. Open your PR (for code work)
```bash
git push -u origin feature/<name>
```
Then open a PR `feature/<name> → dev`, describe what and why, and request Mark's review.

## Ground rules
- No direct push to `mark`, `dev`, or `main` — always PR into `dev`.
- **No outbound network calls in document-processing code** (forensic soundness) — a hard rule.
- Never commit secrets or generated output (`settings.properties`, `output/`, `logs/`).
- Keep any customer data or credentials out of this repo entirely.
