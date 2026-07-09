# Refactoring Plan — repos, build unification, CI

**Status:** Planning / working record. (2026-07)

Goal: simplify the repo/product topology and make releases **one hands-off action
across all platforms**, instead of separate manual builds per project/platform.

## Target topology (open-core)
- **FreeEed** — open (Apache-2.0), Java. **Consolidate FreeEedUI *into* this repo** as a
  module → one open Java repo for engine + review.
  ```
  FreeEed/
    freeeed-processing/     (existing)
    freeeedui/              (moved in from shmsoft/FreeEedUI)
    pom.xml                 (parent reactor builds both: jar + war)
    release_freeeed_complete.sh
  ```
  Then archive `shmsoft/FreeEedUI`.
- **ai_advisor → "FreeEed Server"** — commercial (closed), Python/FastAPI: AI advisors +
  licensing. Stays a **separate** repo (different language + license). See [[freeeed-server-rename]].
- **FreeEedCloud** (Scaia-ai) — AWS-serverless cloud platform; hosts the collector framework
  (see [[k3-cloud-engagement]]).

Rationale: FreeEed + FreeEedUI are both open, both Java/Maven, and already released +
versioned in lockstep — a natural monorepo. Consolidating matches the open/closed line
(one open Java repo; commercial Python separate) and drops the CI orchestration from 3
repos to 2.

## Repo consolidation — how
- Import FreeEedUI under `freeeedui/` with **`git subtree add`** (or `git-filter-repo`) to
  **preserve history + authorship** — do NOT plain-copy.
- Wire into the Maven reactor (parent pom → `freeeed-processing` + `freeeedui`); one `mvn`
  build yields the processing jar and the war.
- Update `release_freeeed_complete.sh` paths (`$FREEEED_UI_PROJECT` → in-repo `freeeedui/`),
  CI, CONTRIBUTING, and the memory notes that say "three repos".
- One-time migration; low risk (both are ours, no external PRs pending on FreeEedUI). Do it
  deliberately, not mid-release.

## Build unification (one script, no separate steps)
- **Fold the AI Advisor PyInstaller build into `release_freeeed_complete.sh`**
  (`.venv/bin/python build.py` before the `ai_advisor/releases` copy) → one script builds
  Java + war + AI Advisor + pack + installers.
- **Hard constraint:** PyInstaller/Nuitka **can't cross-compile**. A single machine cannot
  build all platforms' AI binaries or the Mac `.dmg`. So "same script, no separate steps"
  means: the *identical* script, run **once per platform**, builds that platform's full set.
- (Optional protection upgrade, not required: PyInstaller → **Nuitka** for native compile.)

## CI — the real "one tag → all platforms" (endgame)
GitHub Actions with an **OS matrix**:
```
tag pushed
   ├─ ubuntu-latest    → Linux AiAdvisor + Java + war + .run (makeself)
   ├─ macos-14 (arm)   → mac AiAdvisor + .dmg  (+ sign/notarize)
   ├─ macos-13 (intel) → mac_intel AiAdvisor + .dmg
   └─ windows-latest   → AiAdvisor.exe + NSIS .exe
        ▼
   assembly/release job → gather all AI binaries, assemble complete pack,
                          create GitHub Release ("latest") + upload to S3
```
- **Orchestrator** workflow lives in **FreeEed** (the flagship/release repo).
- **Cross-repo:** needs a **PAT / GitHub App** to check out the other repo(s). Simplest:
  `ai_advisor` runs its **own** workflow that publishes per-OS `AiAdvisor` binaries as
  release artifacts; the orchestrator **downloads** them (no source checkout of the closed
  repo). After the FreeEedUI merge, that's the only cross-repo hop left.
- **Payoff loop already wired:** GitHub Release (+ S3) → freeeed.org/download **auto-tracks
  the latest GitHub release**. So: **tag → build all → release → website updates**, hands-off.
- Jonathan's collector PR already added CI/issue/PR templates to build on.

## Installers
- **Linux** `.run` (makeself) — works today, cross-builds on Linux.
- **Windows** `.exe` (NSIS/makensis) — installer cross-builds on Linux, but `AiAdvisor.exe`
  needs a Windows build.
- **macOS** `.dmg` — basic `hdiutil` step exists, but a *distributable* Mac installer needs
  **jpackage + codesign + notarization** and both arches. **GATING: an Apple Developer ID**
  (Apple Developer Program, ~$99/yr) — without notarization, Gatekeeper blocks it. Both Macs
  (Intel + Apple Silicon) or CI mac runners cover the two arches. See issue #559.

## Suggested order
1. Consolidate FreeEedUI into FreeEed (subtree + Maven reactor). *(unblocks CI simplification)*
2. Fold AI Advisor build into the release script (per-platform one-script).
3. Apple Developer ID → proper signed/notarized Mac `.dmg`.
4. Move it all into the GitHub Actions OS matrix → one-tag releases.

Related: [[pst-processing]] (engine/Piranha), [[freeeed-server-rename]], [[k3-cloud-engagement]].
