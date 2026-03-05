#!/bin/bash
# Wrapper script to run the Control Panel on Mac/Linux

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# ---- Ensure ~/.freeeed/.env exists on first launch ----
FREEEED_CONFIG_DIR="$HOME/.freeeed"
ENV_PATH="$FREEEED_CONFIG_DIR/.env"

mkdir -p "$FREEEED_CONFIG_DIR"

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
