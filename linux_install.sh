#!/bin/bash
# Post-installation script for FreeEed Linux Installer
echo "FreeEed extracted successfully."

INSTALL_DIR="$HOME/.local/share/FreeEed"
DESKTOP_FILE="$HOME/.local/share/applications/FreeEed.desktop"

echo "Installing FreeEed to $INSTALL_DIR..."
mkdir -p "$INSTALL_DIR"
cp -r ./* "$INSTALL_DIR/"

# ---- Create ~/.freeeed config dir and default .env ----
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
    echo "IMPORTANT: Please edit $ENV_PATH and add your OPENAI_API_KEY."
else
    echo "Config already exists at $ENV_PATH, skipping creation."
fi

echo "Creating Desktop shortcut at $DESKTOP_FILE..."

mkdir -p "$HOME/.local/share/applications"

cat <<EOF > "$DESKTOP_FILE"
[Desktop Entry]
Version=1.0
Type=Application
Name=FreeEed Control Panel
Comment=Start FreeEed E-Discovery Services
Exec="$INSTALL_DIR/ControlPanel.sh"
Icon=$INSTALL_DIR/freeeed.png
Terminal=false
Categories=Utility;Office;
EOF

chmod +x "$DESKTOP_FILE"
chmod +x "$INSTALL_DIR/ControlPanel.sh"
echo "Installation complete. You can now launch FreeEed from your application menu."
