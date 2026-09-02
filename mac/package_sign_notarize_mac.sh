#!/usr/bin/env bash
#
# package_sign_notarize_mac.sh — build a signed + notarized macOS FreeEed app.
#
# Produces a Gatekeeper-clean deliverable:
#   fat jar (Maven)  ->  jpackage .app (bundled JRE)
#   ->  codesign (Developer ID Application + hardened runtime + entitlements)
#   ->  notarytool submit --wait  ->  stapler staple  ->  signed .dmg (stapled)
#
# WHY jpackage (not the hdiutil-of-the-whole-pack in release_freeeed_complete.sh):
#   Notarization requires EVERY Mach-O inside the artifact to be signed with a
#   Developer ID + hardened runtime. The complete-pack is full of unsigned
#   binaries (Solr/Tika/Tomcat launchers) and assumes system Java, so it can
#   never notarize. jpackage bundles a JRE into ONE signable/notarizable unit.
#   See freeeed-processing/docs/mac-signing-handoff.md.
#
# SCOPE / KNOWN LIMITATION (read this):
#   This packages the Swing desktop control panel (org.freeeed.ui.ControlPanelUI)
#   with a bundled JRE. It does NOT yet bundle the sibling services (Solr, Tika,
#   Tomcat/review-app) inside the .app. This is the correct FIRST notarizable
#   deliverable — prove the toolchain end to end on the Java app, then decide how
#   to ship the services (bundle their jars under the .app's app dir and launch
#   them with the bundled JRE, or ship them separately). Do that as a follow-up.
#
# USAGE:
#   Set the signing identity + notary credentials (see CONFIG), then:
#     ./mac/package_sign_notarize_mac.sh
#
#   Dry-run the packaging only, skip signing/notarization (no Apple account yet):
#     SKIP_SIGN=1 ./mac/package_sign_notarize_mac.sh
#
# PREREQS (verify with the checks below; the script fails fast if missing):
#   - macOS with Xcode command-line tools (xcode-select --install)
#   - JDK 17+ (for a modern jpackage) and Maven on PATH
#   - Apple Developer Program membership + a "Developer ID Application" cert in
#     the login keychain (security find-identity -v -p codesigning)
#   - A notarytool credential profile stored in the keychain (one-time):
#       xcrun notarytool store-credentials FreeEed-Notary \
#         --apple-id "you@example.com" --team-id "TEAMID" \
#         --password "app-specific-password"     # from appleid.apple.com
#
set -euo pipefail

# ---------------------------------------------------------------------------
# CONFIG — override any of these via the environment.
# ---------------------------------------------------------------------------
APP_NAME="${APP_NAME:-FreeEed}"
MAIN_CLASS="${MAIN_CLASS:-org.freeeed.ui.ControlPanelUI}"
VENDOR="${VENDOR:-SHMsoft, Inc.}"
BUNDLE_ID="${BUNDLE_ID:-org.freeeed.FreeEed}"

# Signing identity: the exact string from `security find-identity -v -p codesigning`
#   e.g. "Developer ID Application: SHMsoft, Inc. (ABCDE12345)"
DEVELOPER_ID="${DEVELOPER_ID:-}"
# notarytool keychain profile name created with `notarytool store-credentials`
NOTARY_PROFILE="${NOTARY_PROFILE:-FreeEed-Notary}"

# Set SKIP_SIGN=1 to build+package only (toolchain smoke test, no Apple account).
SKIP_SIGN="${SKIP_SIGN:-}"

# Optional universal2 JDK path (Azul Zulu / BellSoft Liberica universal build)
# to produce an Intel+Apple-Silicon app. If unset, jpackage uses the JDK on
# PATH, giving an app for THIS machine's architecture only.
JPACKAGE_RUNTIME_JDK="${JPACKAGE_RUNTIME_JDK:-}"

# ---------------------------------------------------------------------------
# Paths
# ---------------------------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
PROC_DIR="$REPO_ROOT/freeeed-processing"
VERSION_JAVA="$PROC_DIR/src/main/java/org/freeeed/main/Version.java"
ENTITLEMENTS="$SCRIPT_DIR/FreeEed.entitlements"
BUILD_DIR="$REPO_ROOT/target/mac"
OUT_DIR="${OUT_DIR:-$REPO_ROOT/target/mac-installers}"

log() { printf '\n\033[1;34m==>\033[0m %s\n' "$*"; }
die() { printf '\n\033[1;31mERROR:\033[0m %s\n' "$*" >&2; exit 1; }

[ "$(uname)" = "Darwin" ] || die "This script must run on macOS."

# ---------------------------------------------------------------------------
# Version — single source of truth is Version.java (same as the release script).
# jpackage requires a numeric app-version (X[.Y[.Z]]); strip any -PREVIEW/-SNAPSHOT.
# ---------------------------------------------------------------------------
RAW_VERSION="$(sed -n 's/.*String V = "\([^"]*\)".*/\1/p' "$VERSION_JAVA")"
[ -n "$RAW_VERSION" ] || die "Could not read version from $VERSION_JAVA"
APP_VERSION="${RAW_VERSION%%-*}"   # 10.8.7-PREVIEW -> 10.8.7
log "FreeEed version: $RAW_VERSION (app-version $APP_VERSION)"

# ---------------------------------------------------------------------------
# Prereq checks
# ---------------------------------------------------------------------------
command -v mvn      >/dev/null || die "Maven not found on PATH."
command -v jpackage >/dev/null || die "jpackage not found (need JDK 17+ on PATH)."
if [ -z "$SKIP_SIGN" ]; then
  command -v xcrun >/dev/null || die "xcrun not found (install Xcode command-line tools)."
  [ -n "$DEVELOPER_ID" ] || die "DEVELOPER_ID is empty. Set it to your 'Developer ID Application: … (TEAMID)' identity, or run with SKIP_SIGN=1."
  security find-identity -v -p codesigning | grep -qF "$DEVELOPER_ID" \
    || die "Signing identity not found in keychain: $DEVELOPER_ID"
  xcrun notarytool history --keychain-profile "$NOTARY_PROFILE" >/dev/null 2>&1 \
    || die "notarytool profile '$NOTARY_PROFILE' not usable. Create it with 'xcrun notarytool store-credentials'."
fi

# ---------------------------------------------------------------------------
# 1. Build the fat jar (jar-with-dependencies, mainClass baked in the manifest).
# ---------------------------------------------------------------------------
log "Building fat jar (mvn -pl freeeed-processing -am package -DskipTests)…"
( cd "$REPO_ROOT" && mvn -pl freeeed-processing -am package -DskipTests )

FAT_JAR="$(ls -1 "$PROC_DIR"/target/freeeed-processing-*-jar-with-dependencies.jar 2>/dev/null | head -1)"
[ -n "$FAT_JAR" ] || die "Fat jar not found under $PROC_DIR/target (expected *-jar-with-dependencies.jar)."
log "Fat jar: $FAT_JAR"

# Stage the single input jar jpackage will bundle.
rm -rf "$BUILD_DIR" && mkdir -p "$BUILD_DIR/input" "$OUT_DIR"
cp "$FAT_JAR" "$BUILD_DIR/input/"
INPUT_JAR_NAME="$(basename "$FAT_JAR")"

# ---------------------------------------------------------------------------
# 2. jpackage --type app-image  ->  FreeEed.app (bundled JRE).
# ---------------------------------------------------------------------------
log "Running jpackage (app-image)…"
JPACKAGE_ARGS=(
  --type app-image
  --name "$APP_NAME"
  --app-version "$APP_VERSION"
  --vendor "$VENDOR"
  --input "$BUILD_DIR/input"
  --main-jar "$INPUT_JAR_NAME"
  --main-class "$MAIN_CLASS"
  --dest "$BUILD_DIR"
  --mac-package-identifier "$BUNDLE_ID"
  --java-options "-Xmx2048m"
)
[ -f "$SCRIPT_DIR/FreeEed.icns" ] && JPACKAGE_ARGS+=( --icon "$SCRIPT_DIR/FreeEed.icns" )
[ -n "$JPACKAGE_RUNTIME_JDK" ] && JPACKAGE_ARGS+=( --runtime-image "$JPACKAGE_RUNTIME_JDK" )
jpackage "${JPACKAGE_ARGS[@]}"

APP_BUNDLE="$BUILD_DIR/$APP_NAME.app"
[ -d "$APP_BUNDLE" ] || die "jpackage did not produce $APP_BUNDLE"
log "Built $APP_BUNDLE"

if [ -n "$SKIP_SIGN" ]; then
  log "SKIP_SIGN set — stopping after packaging. App image at: $APP_BUNDLE"
  exit 0
fi

# ---------------------------------------------------------------------------
# 3. codesign — sign nested Mach-O binaries first (inside-out), then the .app.
#    Hardened runtime (--options runtime) + entitlements are REQUIRED for
#    notarization of a Java app (JIT / unsigned exec memory).
# ---------------------------------------------------------------------------
[ -f "$ENTITLEMENTS" ] || die "Entitlements file missing: $ENTITLEMENTS"
log "Codesigning nested binaries…"
# Sign every dylib / .jnilib / executable in the bundled runtime, deepest first.
find "$APP_BUNDLE" -type f \( -name "*.dylib" -o -name "*.jnilib" -o -name "jspawnhelper" \) -print0 \
  | while IFS= read -r -d '' f; do
      codesign --force --timestamp --options runtime \
        --entitlements "$ENTITLEMENTS" --sign "$DEVELOPER_ID" "$f"
    done
# Also sign the bundled java launcher(s) in the runtime.
find "$APP_BUNDLE/Contents/runtime" -type f -perm -111 -print0 2>/dev/null \
  | while IFS= read -r -d '' f; do
      if file "$f" | grep -q "Mach-O"; then
        codesign --force --timestamp --options runtime \
          --entitlements "$ENTITLEMENTS" --sign "$DEVELOPER_ID" "$f" || true
      fi
    done

log "Codesigning the app bundle…"
codesign --force --timestamp --options runtime \
  --entitlements "$ENTITLEMENTS" --sign "$DEVELOPER_ID" "$APP_BUNDLE"
codesign --verify --deep --strict --verbose=2 "$APP_BUNDLE"
log "codesign verify OK"

# ---------------------------------------------------------------------------
# 4. Notarize the .app (zip it for submission), then staple the ticket.
# ---------------------------------------------------------------------------
NOTARIZE_ZIP="$BUILD_DIR/$APP_NAME-$APP_VERSION.zip"
log "Zipping app for notarization…"
/usr/bin/ditto -c -k --keepParent "$APP_BUNDLE" "$NOTARIZE_ZIP"

log "Submitting to Apple notary service (this waits for the result)…"
xcrun notarytool submit "$NOTARIZE_ZIP" --keychain-profile "$NOTARY_PROFILE" --wait \
  || die "Notarization failed. Inspect with: xcrun notarytool log <submission-id> --keychain-profile $NOTARY_PROFILE"

log "Stapling notarization ticket to the app…"
xcrun stapler staple "$APP_BUNDLE"
xcrun stapler validate "$APP_BUNDLE"
rm -f "$NOTARIZE_ZIP"

# ---------------------------------------------------------------------------
# 5. Wrap the stapled .app in a compressed DMG, then staple the DMG too.
# ---------------------------------------------------------------------------
FINAL_DMG="$OUT_DIR/FreeEed-$RAW_VERSION-macOS.dmg"
log "Building DMG: $FINAL_DMG"
DMG_STAGE="$BUILD_DIR/dmg"
rm -rf "$DMG_STAGE" && mkdir -p "$DMG_STAGE"
cp -R "$APP_BUNDLE" "$DMG_STAGE/"
ln -s /Applications "$DMG_STAGE/Applications"
rm -f "$FINAL_DMG"
hdiutil create -volname "FreeEed $RAW_VERSION" -srcfolder "$DMG_STAGE" \
  -ov -format UDZO "$FINAL_DMG"

# Sign + notarize + staple the DMG itself so the download is clean end to end.
codesign --force --timestamp --sign "$DEVELOPER_ID" "$FINAL_DMG"
log "Notarizing the DMG…"
xcrun notarytool submit "$FINAL_DMG" --keychain-profile "$NOTARY_PROFILE" --wait \
  || die "DMG notarization failed."
xcrun stapler staple "$FINAL_DMG"
xcrun stapler validate "$FINAL_DMG"

log "DONE. Signed + notarized deliverable:"
echo "    $FINAL_DMG"
echo
echo "Verify Gatekeeper acceptance on a clean machine (the 2017 Intel Mac):"
echo "    spctl -a -t open --context context:primary-signature -v \"$FINAL_DMG\""
echo "    spctl -a -t exec -vvv /Applications/$APP_NAME.app   # after installing"
