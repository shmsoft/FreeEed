# Refactoring Plan — repos, build unification, CI

**Status:** Planning / working record. (2026-07)

Goal: simplify the repo/product topology and make releases **one hands-off action
across all platforms**, instead of separate manual builds per project/platform.

## Phasing (roadmap)
Three phases. **Phase 1 is the body of this document** (everything from *Target topology*
through *Suggested order*); Phases 2–3 build on the clean base.

- **Phase 1 — Polished release (functional parity).** No new user features — end result is the
  *same* functionality, but one-script / one-tag builds, bundled JRE (no Java prereq), signed Mac
  installers, a CI matrix, and a download people can actually use. **Repos stay as they are** —
  the earlier FreeEed+FreeEedUI merge is dropped (see below). **ai_advisor keeps working exactly
  as today** (bundled as-is); its rework is Phase 3.
- **Phase 2 — FreeEed Viewer (publish & hand-off).** A standalone, portable viewer that packages
  the lawyer's **work product** — the reviewed/produced set (Bates, coding/tags, redactions, load
  files) — into a self-contained deliverable the attorney hands to an **investigator, co-counsel,
  expert, or client** who doesn't run the full FreeEed stack. Ties to the production-engine-split
  (FreeEed produces; FreeEedUI orchestrates, #61) and the Viewer lifecycle.
- **Phase 3 — AI (local-first).** Swap ai_advisor's LLM to a **local model** (Ollama,
  OpenAI-compatible) + an **outbound-monitoring / certification** layer, wire the **auto-start UX**
  for licensed users, and demote external OpenAI/BYOK to secondary. Court-defensible (*Morgan v.
  V2X*). See the "AI Advisor start-up" section below + the local-AI decision record.

## Target topology (open-core) — repos stay separate
- **FreeEed** — open (Apache-2.0), Java: processing engine + Swing UI. The flagship/release repo.
- **FreeEedUI** — open (Apache-2.0), Java/Spring war: the web review app. **Separate repo**
  (see decision below); built as a sibling checkout, not merged in.
- **ai_advisor → "FreeEed Server"** — commercial (closed), Python/FastAPI: AI advisors +
  licensing. Separate repo (different language + license). See [[freeeed-server-rename]].
- **FreeEedCloud** (Scaia-ai) — AWS-serverless cloud platform; hosts the collector framework
  (see [[k3-cloud-engagement]]).

## Repos — keep FreeEed and FreeEedUI separate (decided 2026-07)
An earlier version of this plan proposed merging FreeEedUI *into* FreeEed as a Maven module.
**Dropped.** The merge was a convenience, not a requirement, and the advantages don't hold up:

- **No real decoupling won.** FreeEedUI is a separate Spring war with its own lifecycle;
  co-locating it in one repo doesn't make the two any less independent — it just moves files.
- **The team is growing.** Separate repos give cleaner per-repo access (work the review UI
  without the engine, and vice-versa) and avoid a disruptive restructure mid-onboarding.
- **CI doesn't need it.** "One tag → all platforms" works with the orchestrator checking out
  FreeEedUI as a **sibling repo** — it's ours, so a PAT or git submodule is trivial (the
  release script already clones it as a sibling today). See CI below.
- **Migration cost/risk** (history rewrite, release-script paths, contributor confusion) buys
  mostly a cosmetic tidy.

**What we give up (small, accepted):** a cross-cutting change spans two coordinated PRs instead
of one, and we keep the clone-sibling step instead of a single Maven reactor. They already
release in lockstep via `release_freeeed_complete.sh`, so this is a minor tax — not worth the
migration.

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
  repo). FreeEedUI is a **second checkout** — but it's our own open repo, so a PAT or git
  submodule is trivial (the release script already clones it as a sibling).
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
  native-Windows. **Must be Authenticode code-signed (under Scaia)** or Defender SmartScreen
  shows an "Unknown publisher" scare screen (**#581**) — a trust blocker for the CISO/law-firm
  audience. Options: **EV cert** (immediate SmartScreen trust; needs HSM/token, pricier),
  **OV cert** (cheaper; reputation builds over time), or **Azure Trusted Signing** (managed
  cloud signing — evaluate first). This is the **Windows sibling of the Mac notarization** work.
- **macOS** — **TWO `.dmg`s**: `FreeEed-<ver>-macOS-arm64.dmg` (Apple Silicon) and
  `-x86_64.dmg` (Intel), because the bundled JRE is **per-arch** (matches the per-arch
  AiAdvisor builds and the two mac CI runners `macos-14`/`macos-13`). **codesign + notarize**
  each. Download page auto-detects arch / labels them "Apple Silicon" vs "Intel".
- **Apple Developer account — Organization enrollment under Scaia** (the public-facing brand;
  that's the name users see in the macOS "verified developer" prompt, not the private SHMsoft
  entity). Needed for notarization. Provides the **Developer ID Application** cert, **Team ID**,
  and **notarytool** credentials (App Store Connect API key preferred over an app-specific
  password) → stored as CI secrets. Program ~$99/yr. See #559.
  - **Cert type matters:** for **`.dmg`** (our target) you sign the **app bundle** with a
    **Developer ID *Application*** certificate; **Developer ID *Installer*** signs **`.pkg`**
    packages only. So create the **Application** cert (Installer is optional, only if we ever
    ship a `.pkg`).
  - **CI needs a `.p12`, not the `.cer`.** The `.cer` is only the *public* certificate; the
    signing identity = cert **+ private key**, which lives in the Keychain of the Mac that
    generated the CSR. Export it there as a password-protected **`.p12`**, then load that into
    CI as an **encrypted secret** — never commit it to the repo.

## AI Advisor start-up (UX) — paid-only, auto-start  *(Phase 3)*
> **AI model — local-first (flagship).** The primary AI runs as a **local model** (no data
> leaves the machine, **monitored** so the attorney can *certify* no outbound calls — the
> court-defensible position). Legal driver: *Morgan v. V2X, Inc.*, 2026 WL 864223 (D. Colo.),
> which bars uploading Confidential Info to mainstream AI without contractual no-training /
> no-disclosure + documentation. Tech: ai_advisor `langchain_openai.ChatOpenAI` → **local LLM
> via Ollama** (OpenAI-compatible) + an **outbound-monitoring** layer. External OpenAI/BYOK
> (below) is the **secondary** path. See the local-AI decision record.

AI stays **paid-only** (gated by ai_advisor's `license_check_middleware`; free users don't get
it). Fix only the *trigger*: replace the current "go to a certain tab to start it" step with
**auto-start in the background for licensed users** — non-blocking at launch, or lazily on
first AI use. Do **not** start it for free users: no point paying the heavy PyInstaller process
+ port-8000 + error-noise cost for a feature they can't use. (Considered "always start for
everyone" — safe re: leakage since ai_advisor self-gates by license, but wasteful; rejected.)

**Bring-your-own-key (BYOK) — secondary/external mode:** for the *external* AI path only, it
requires the user's **own OpenAI API key** — this keeps the
API cost on the user, not on us, which is what makes offering AI viable. When a licensed user
turns on AI, **prompt for the key in-app** and store it (`~/.freeeed/.env`, which already holds
`OPENAI_API_KEY=`). Replaces today's manual `.env` editing with a proper key-entry UX; validate
the key on entry and surface a clear message if it's missing/invalid.

## Suggested order  *(Phase 1)*
1. Fold AI Advisor build into the release script (per-platform one-script). FreeEedUI stays a
   **sibling checkout** — no repo merge.
2. Adopt **jpackage/jlink** (bundle JRE) across platforms; **2 signed+notarized Mac `.dmg`s**
   (arm64 + x86_64); update install docs (drop "Java required").
3. Move it all into the GitHub Actions OS matrix → one-tag releases (orchestrator checks out
   FreeEedUI as a sibling repo).

Related: [[processing-engine]] (engine/Piranha), [[freeeed-server-rename]], [[k3-cloud-engagement]].
