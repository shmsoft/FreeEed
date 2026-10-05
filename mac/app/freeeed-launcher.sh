#!/bin/bash
# freeeed-launcher.sh — what FreeEed.app actually runs (exec'd by the native stub
# Contents/MacOS/FreeEed).
#
# WHY: the complete pack cannot run in place. A .dmg is read-only, and an app run
# from Downloads is translocated to a read-only random path, but the pack writes
# logs/, Tomcat temp/work, Solr indexes and freeeed.db next to itself. Run from the
# mounted .dmg, start_all.sh hung and open_player.sh could not even create
# player.log, so the Player never appeared (#612, #613). So the app carries a
# pristine copy of the pack in Contents/Resources/pack and installs it into a
# writable home folder, then runs everything from there.
#
# Each launch:
#   1. Find a working Java (find_java.sh); if none, say so with a download link.
#   2. First launch only: EULA dialog (Agree / Quit).
#   3. Install the pack into $FREEEED_HOME, or refresh its program files when the
#      app is a newer build -- never deleting anything, and keeping the user's
#      FreeEed/settings.properties, so indexes, the DB, logs and output survive.
#   4. Create ~/.freeeed/.env if missing (same defaults as ControlPanel.sh).
#   5. exec the Control Panel with --start-all: services start, then the Player.
#
# GUI app: no terminal. Every message is a dialog (osascript) and everything else
# goes to ~/Library/Logs/FreeEed/launcher.log.

set -u

APP_CONTENTS="$(cd "$(dirname "$0")/.." && pwd)"
PACK_SRC="$APP_CONTENTS/Resources/pack"
FREEEED_HOME="${FREEEED_HOME:-$HOME/FreeEed}"
MARKER=".freeeed-app-install"
CONFIG_DIR="$HOME/.freeeed"
EULA_ACCEPTED_FILE="$CONFIG_DIR/.eula_accepted"
LOG_DIR="$HOME/Library/Logs/FreeEed"
JAVA_DOWNLOAD_URL="https://adoptium.net/temurin/releases/?os=mac&package=jdk&version=21"

mkdir -p "$LOG_DIR"
exec >>"$LOG_DIR/launcher.log" 2>&1
echo "==== $(date '+%Y-%m-%d %H:%M:%S') FreeEed.app launch from $APP_CONTENTS"

# AppleScript string literal: escape backslashes and double quotes.
as_quote() { local s=${1//\\/\\\\}; s=${s//\"/\\\"}; printf '"%s"' "$s"; }

# dialog MESSAGE BUTTON... -> prints the button clicked (last button is default).
# Cancel/close prints nothing.
dialog() {
    local msg=$1; shift
    local buttons="" b
    for b in "$@"; do buttons="${buttons:+$buttons, }$(as_quote "$b")"; done
    osascript \
        -e "set r to display dialog $(as_quote "$msg") with title \"FreeEed\" buttons {$buttons} default button $(as_quote "${!#}") with icon note" \
        -e 'button returned of r' 2>/dev/null
}

fail() {
    echo "ERROR: $1"
    dialog "$1

Details: ~/Library/Logs/FreeEed/launcher.log" "Quit" >/dev/null
    exit 1
}

[ -d "$PACK_SRC" ] || fail "This copy of FreeEed is incomplete (no Contents/Resources/pack). Please download it again."
APP_VERSION="$(head -1 "$PACK_SRC/VERSION" 2>/dev/null)"
APP_BUILD="$(cat "$PACK_SRC/VERSION" 2>/dev/null | tr '\n' ' ')"
echo "App build: $APP_BUILD"

# Don't start a second Control Panel; LaunchServices normally prevents this, but
# a copy of the app elsewhere, or one started by ControlPanel.sh, would not. Say
# so -- quitting silently looks like the app is broken.
if pgrep -f "java.*org.freeeed.ui.ControlPanelUI" >/dev/null 2>&1; then
    echo "Control Panel already running: $(pgrep -fl 'java.*org.freeeed.ui.ControlPanelUI' | cut -c1-200)"
    dialog "FreeEed is already running. Look for the FreeEed Manager window.

If you can't find it, quit FreeEed (or restart the Mac) and open it again." "OK" >/dev/null
    exit 0
fi

# ---- 1. Java ---------------------------------------------------------------
. "$PACK_SRC/find_java.sh"
if ! freeeed_find_java; then
    answer="$(dialog "FreeEed needs Java 11 or newer, and none was found on this Mac.

Install a Java runtime (the free Eclipse Temurin JDK is recommended), then open FreeEed again." "Quit" "Download Java")"
    [ "$answer" = "Download Java" ] && open "$JAVA_DOWNLOAD_URL"
    exit 1
fi
echo "Using Java: $JAVA_CMD"

# ---- 2. EULA (first launch) ------------------------------------------------
mkdir -p "$CONFIG_DIR"
if [ ! -f "$EULA_ACCEPTED_FILE" ]; then
    while :; do
        answer="$(dialog "Welcome to FreeEed $APP_VERSION.

To use FreeEed you must accept the FreeEed End User License Agreement, including its disclaimer of warranties and limitation of liability." "Read License" "Quit" "Agree")"
        case "$answer" in
            "Agree") break ;;
            "Read License") open -e "$PACK_SRC/EULA.txt" ;;
            *) echo "EULA not accepted; quitting."; exit 0 ;;
        esac
    done
    { echo "accepted=$(date -u +%Y-%m-%dT%H:%M:%SZ)"; echo "via=FreeEed.app $APP_VERSION"; } > "$EULA_ACCEPTED_FILE"
    echo "EULA accepted."
fi

# ---- 3. Install / refresh the pack ----------------------------------------
if [ -e "$FREEEED_HOME" ] && [ ! -f "$FREEEED_HOME/$MARKER" ]; then
    fail "A folder named $FREEEED_HOME already exists and was not created by FreeEed.app, so FreeEed will not touch it.

Rename or move it, then open FreeEed again."
fi

if [ ! -f "$FREEEED_HOME/$MARKER" ]; then
    echo "Installing pack into $FREEEED_HOME"
    mkdir -p "$FREEEED_HOME" || fail "Could not create $FREEEED_HOME."
    rsync -a "$PACK_SRC/" "$FREEEED_HOME/" || fail "Could not copy FreeEed into $FREEEED_HOME (disk full?)."
    echo "$APP_BUILD" > "$FREEEED_HOME/$MARKER"
elif [ "$(cat "$FREEEED_HOME/$MARKER")" != "$APP_BUILD" ]; then
    echo "Refreshing $FREEEED_HOME from build $(cat "$FREEEED_HOME/$MARKER") to $APP_BUILD"
    # No --delete: indexes, freeeed.db, logs and output live alongside the program
    # files and must survive. settings.properties is the user's own; keep it.
    rsync -a --exclude 'FreeEed/settings.properties' "$PACK_SRC/" "$FREEEED_HOME/" \
        || fail "Could not update FreeEed in $FREEEED_HOME (disk full?)."
    echo "$APP_BUILD" > "$FREEEED_HOME/$MARKER"
fi

# ---- 4. ~/.freeeed/.env (same defaults as ControlPanel.sh) -----------------
if [ ! -f "$CONFIG_DIR/.env" ]; then
    cat > "$CONFIG_DIR/.env" <<'ENVEOF'
# AI Advisor Configuration
OPENAI_API_KEY=
CHROMA_PERSIST_DIR=chroma_data
LLM_MODEL=gpt-4o-mini
CHROMA_EMBED_MODEL=text-embedding-3-small
TOP_K=10
PORT=8000
ENVEOF
    echo "Created $CONFIG_DIR/.env"
fi

# ---- 5. Control Panel ------------------------------------------------------
cd "$FREEEED_HOME" || fail "Could not open $FREEEED_HOME."
mkdir -p logs
echo "Starting Control Panel"
exec "$JAVA_CMD" -Xdock:name=FreeEed -Dapple.awt.application.name=FreeEed \
    -cp "FreeEed/target/*:FreeEed/target/lib/*:FreeEed/target/dependency/*:FreeEed/*" \
    org.freeeed.ui.ControlPanelUI --start-all
