#!/bin/bash
# Wrapper script to run the Control Panel on Mac/Linux

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# ---- Ensure ~/.freeeed/.env exists on first launch ----
FREEEED_CONFIG_DIR="$HOME/.freeeed"
ENV_PATH="$FREEEED_CONFIG_DIR/.env"
EULA_ACCEPTED_FILE="$FREEEED_CONFIG_DIR/.eula_accepted"

# Resolve a working Java runtime before anything else, so a Mac with no JDK gets
# an actionable message instead of a Control Panel that opens and does nothing.
. "$SCRIPT_DIR/find_java.sh"
freeeed_require_java || exit 1

mkdir -p "$FREEEED_CONFIG_DIR"

# ---- EULA acceptance on first launch (covers macOS DMG installs) ----
if [ ! -f "$EULA_ACCEPTED_FILE" ]; then
    EULA_FILE="$SCRIPT_DIR/EULA.txt"
    if [ -f "$EULA_FILE" ]; then
        echo ""
        echo "=============================================="
        echo "  END USER LICENSE AGREEMENT"
        echo "=============================================="
        echo ""

        if command -v less &> /dev/null; then
            less "$EULA_FILE"
        else
            cat "$EULA_FILE"
        fi

        echo ""
        echo "I have read and agree to the FreeEed End User License Agreement,"
        echo "including the disclaimer of warranties and limitation of liability."
        echo ""
        read -rp "Do you agree? [y/N] " eula_accept
        if [[ ! "$eula_accept" =~ ^[Yy]$ ]]; then
            echo "You must accept the EULA to use FreeEed."
            exit 1
        fi

        # Record acceptance LOCALLY only -- no network call. FreeEed is a
        # local-first, forensically-sound tool (incl. FOIA / CJIS use); first
        # run must not phone home. (The appliance pre-accepts; the Mac .app
        # records the same marker from its GUI EULA dialog.)
        echo "accepted=$(date -u +%Y-%m-%dT%H:%M:%SZ)" > "$EULA_ACCEPTED_FILE"
        echo "EULA accepted."
    else
        echo "Warning: EULA.txt not found. Skipping EULA check."
    fi
fi

if [ ! -f "$ENV_PATH" ]; then
    echo "Creating default config at $ENV_PATH..."
    cat <<ENVEOF > "$ENV_PATH"
# AI Advisor Configuration
OPENAI_API_KEY=
CHROMA_PERSIST_DIR=chroma_data
LLM_MODEL=gpt-4o-mini
CHROMA_EMBED_MODEL=text-embedding-3-small
TOP_K=10
PORT=8000
ENVEOF
    echo "IMPORTANT: Please edit $ENV_PATH and add your OPENAI_API_KEY before starting."
fi

# Launch the Control Panel using Java.
# We include the processing jar in the classpath
"$JAVA_CMD" -cp "FreeEed/target/*:FreeEed/target/lib/*:FreeEed/target/dependency/*:FreeEed/*" org.freeeed.ui.ControlPanelUI
