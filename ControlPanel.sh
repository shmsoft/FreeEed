#!/bin/bash
# Wrapper script to run the Control Panel on Mac/Linux

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# ---- Ensure ~/.freeeed/.env exists on first launch ----
FREEEED_CONFIG_DIR="$HOME/.freeeed"
ENV_PATH="$FREEEED_CONFIG_DIR/.env"
EULA_ACCEPTED_FILE="$FREEEED_CONFIG_DIR/.eula_accepted"
EULA_TRACKING_URL="https://api.freeeed.org/eula/accept"

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

        echo ""
        read -rp "Please enter your email address: " user_email
        echo ""

        # Track acceptance (best-effort)
        MACHINE_ID=$(hostname | md5sum 2>/dev/null | cut -d' ' -f1 || hostname | md5 2>/dev/null || hostname)
        VERSION=$(cat "$SCRIPT_DIR/VERSION" 2>/dev/null || echo "unknown")
        OS_NAME="macOS"
        if [[ "$(uname)" != "Darwin" ]]; then
            OS_NAME="Linux"
        fi

        if command -v curl &> /dev/null; then
            echo "Registering EULA acceptance..."
            RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$EULA_TRACKING_URL" \
                -H "Content-Type: application/json" \
                -d "{\"machine_id\":\"$MACHINE_ID\",\"email\":\"$user_email\",\"os\":\"$OS_NAME\",\"version\":\"$VERSION\"}" \
                --connect-timeout 5 --max-time 10 2>&1) || true
            HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
            if [ "$HTTP_CODE" = "201" ]; then
                echo "EULA acceptance registered successfully."
            else
                echo "Warning: Could not register EULA acceptance. Continuing anyway."
            fi
        fi

        # Mark EULA as accepted so we don't prompt again
        echo "accepted=$(date -u +%Y-%m-%dT%H:%M:%SZ)" > "$EULA_ACCEPTED_FILE"
        echo "email=$user_email" >> "$EULA_ACCEPTED_FILE"
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
java -cp "FreeEed/target/*:FreeEed/target/lib/*:FreeEed/target/dependency/*:FreeEed/*" org.freeeed.ui.ControlPanelUI
