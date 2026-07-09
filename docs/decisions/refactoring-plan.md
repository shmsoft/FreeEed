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

## Installers — bundle a JRE (decided)
**Bundle a JRE via `jpackage`/`jlink` on every platform** → **no Java prerequisite**
("download, double-click, done"), which matters for the lawyer/forensic audience. This
replaces the current "require system Java" model everywhere, so the install docs
(wiki/README "Java 11+ required") become **obsolete** and need updating. `jpackage` builds a
native runtime per OS/arch and **can't cross-compile** — which reinforces the per-platform CI
matrix.

- **Linux** — `.run` (makeself) wrapping a jpackage/jlink runtime image.
- **Windows** — `.exe` (NSIS, or jpackage's MSI/EXE) with bundled JRE; `AiAdvisor.exe` still
  native-Windows.
- **macOS** — **TWO `.dmg`s**: `FreeEed-<ver>-macOS-arm64.dmg` (Apple Silicon) and
  `-x86_64.dmg` (Intel), because the bundled JRE is **per-arch** (matches the per-arch
  AiAdvisor builds and the two mac CI runners `macos-14`/`macos-13`). **codesign + notarize**
  each. Download page auto-detects arch / labels them "Apple Silicon" vs "Intel".
- **Apple Developer account: yes (getting one)** — needed for notarization. Provides the
  **Developer ID Application cert**, **Team ID**, and **notarytool credentials** (app-specific
  password or API key) → stored as CI secrets. Program ~$99/yr. See #559.

## AI Advisor start-up (UX) — paid-only, auto-start
AI stays **paid-only** (gated by ai_advisor's `license_check_middleware`; free users don't get
it). Fix only the *trigger*: replace the current "go to a certain tab to start it" step with
**auto-start in the background for licensed users** — non-blocking at launch, or lazily on
first AI use. Do **not** start it for free users: no point paying the heavy PyInstaller process
+ port-8000 + error-noise cost for a feature they can't use. (Considered "always start for
everyone" — safe re: leakage since ai_advisor self-gates by license, but wasteful; rejected.)

**Bring-your-own-key (BYOK):** AI requires the user's **own OpenAI API key** — this keeps the
API cost on the user, not on us, which is what makes offering AI viable. When a licensed user
turns on AI, **prompt for the key in-app** and store it (`~/.freeeed/.env`, which already holds
`OPENAI_API_KEY=`). Replaces today's manual `.env` editing with a proper key-entry UX; validate
the key on entry and surface a clear message if it's missing/invalid.

## Suggested order
1. Consolidate FreeEedUI into FreeEed (subtree + Maven reactor). *(unblocks CI simplification)*
2. Fold AI Advisor build into the release script (per-platform one-script).
3. Adopt **jpackage/jlink** (bundle JRE) across platforms; **2 signed+notarized Mac `.dmg`s**
   (arm64 + x86_64); update install docs (drop "Java required").
4. Move it all into the GitHub Actions OS matrix → one-tag releases.

Related: [[pst-processing]] (engine/Piranha), [[freeeed-server-rename]], [[k3-cloud-engagement]].
