# macOS signing & notarization — handoff

**For:** a Claude Code session (or human) picking up the macOS build on the Mac mini.
**Goal:** produce a **no-Gatekeeper-warnings** macOS build — a **signed + notarized** app.

## Context / current state
- **FreeEed 10.8.6 GA is shipped.** `main` = 10.8.6 in `shmsoft/FreeEed` + `shmsoft/FreeEedUI`; installers on S3 (`FreeEed-latest-*`) and a GitHub release. Linux is clean; **Windows signing is Ashish's task**. **macOS is the remaining platform.**
- **The current macOS build is NOT notarizable — do not try to sign it.** `release_freeeed_complete.sh` builds the Mac `.dmg` with `hdiutil` wrapping the whole complete-pack (jars, Tomcat, Solr, Tika, shell scripts) and relies on the user having **system Java**. Notarization requires every Mach-O executable inside signed with Developer ID + hardened runtime — a pack full of unsigned binaries fails that.

## The right path (aligns with the repo's "bundled JRE (jpackage/jlink)" roadmap)
1. **`jpackage` a `.app`** that bundles a JRE → one signable/notarizable unit (also removes the "needs Java installed" problem). Build from the `freeeed-processing` fat jar (`mvn package assembly:single`).
2. **Aim for a universal2 app** (runs on Intel + Apple Silicon) using a **universal2 JDK** (Azul Zulu or BellSoft Liberica). If universal proves finicky, fall back to two arch-specific builds (arm64 on the mini, x86_64 on the 2017).
3. **Sign + notarize:**
   - `codesign` with **Developer ID Application** cert, `--options runtime` (hardened runtime); sign nested binaries.
   - `xcrun notarytool submit <artifact> --wait`
   - `xcrun stapler staple <artifact>`
   - wrap in a `.dmg` and ship.

## Machines
- **Build / sign / notarize on the Mac mini** (Apple Silicon, current macOS — `notarytool` needs a recent OS).
- **2017 Intel Mac = test only** (verify the app launches on real Intel hardware).

## Prereqs to confirm first
- **Apple Developer Program** active → `developer.apple.com/account` shows a Team ID, or you can create a **"Developer ID Application"** cert under Certificates, Identifiers & Profiles. (Free accounts can't create that cert.)
- **Xcode command-line tools:** `xcode-select --install`
- **JDK + Maven** installed.
- **Developer ID cert installed:** `security find-identity -v -p codesigning` lists `Developer ID Application: … (TeamID)`.

## Repo / git hygiene
- Clone or `git pull` `shmsoft/FreeEed` (and `FreeEedUI`). The main dev box is Ubuntu (`/media/mark/data1/SHMsoft/…`); keep the Mac build config **in the repo** — commit + push any new jpackage/sign scripts so the two machines don't diverge.
- Branch model: develop on `mark` → `mark → dev` (ff-only) → `dev → main` only at a GA. The Mac work is new → `mark`/`dev`. (See `build-and-release.md` in this folder.)

## First steps (validate the toolchain before wiring signing)
1. Confirm Xcode CLT + JDK/Maven installed.
2. Confirm the Developer ID cert is present (`security find-identity …`).
3. Minimal `jpackage --type app-image` test on the fat jar to confirm jpackage works, **before** adding `--mac-sign` / notarization.
4. Then layer in codesign → notarytool → stapler → `.dmg`.

## The script (already committed — start here)
`mac/package_sign_notarize_mac.sh` does the whole pipeline: fat jar → jpackage
`.app` (bundled JRE) → codesign (Developer ID + hardened runtime + entitlements
in `mac/FreeEed.entitlements`) → `notarytool submit --wait` → `stapler staple`
→ signed+stapled `.dmg`. It reads the version from `Version.java` and fails fast
on missing prereqs.

- **Toolchain smoke test first (no Apple account needed):**
  `SKIP_SIGN=1 ./mac/package_sign_notarize_mac.sh` — builds the jar and produces
  `FreeEed.app` under `target/mac/`, skipping all signing. Run this to prove
  jpackage works on the mini before touching certs.
- **Full run:** set the identity + notary profile, then run it:
  ```
  xcrun notarytool store-credentials FreeEed-Notary \
    --apple-id "you@…" --team-id "TEAMID" --password "app-specific-pw"   # one-time
  DEVELOPER_ID="Developer ID Application: SHMsoft, Inc. (TEAMID)" \
    ./mac/package_sign_notarize_mac.sh
  ```
- **universal2:** point `JPACKAGE_RUNTIME_JDK` at a universal (Zulu/Liberica) JDK
  runtime image to get an Intel+Apple-Silicon app; otherwise you get this
  machine's arch only.

**Known scope limit (by design):** the script packages the Swing control panel
(`org.freeeed.ui.ControlPanelUI`) with a bundled JRE. It does **not** yet bundle
the Solr/Tika/Tomcat services inside the `.app` — that's the deliberate follow-up
once the notarization pipeline is proven end to end. Optional icon: drop a
`mac/FreeEed.icns` and the script picks it up.
