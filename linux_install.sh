#!/bin/bash
# Post-installation script for FreeEed Linux Installer
echo "FreeEed extracted successfully."

EULA_TRACKING_URL="https://shmsoft.com/eula/accept"

# ---- EULA Acceptance Gate ----
if [ -f "EULA.txt" ]; then
    echo ""
    echo "=============================================="
    echo "  END USER LICENSE AGREEMENT"
    echo "=============================================="
    echo ""
    
    # Display EULA with less if available, fallback to cat
    if command -v less &> /dev/null; then
        less EULA.txt
    else
        cat EULA.txt
    fi
    
    echo ""
    echo "I have read and agree to the FreeEed End User License Agreement,"
    echo "including the disclaimer of warranties and limitation of liability."
    echo ""
    read -rp "Do you agree? [y/N] " eula_accept
    if [[ ! "$eula_accept" =~ ^[Yy]$ ]]; then
        echo "You must accept the EULA to install FreeEed. Installation cancelled."
        exit 1
    fi
    echo "EULA accepted."
else
    echo "Warning: EULA.txt not found in package."
fi

# ---- Track EULA acceptance (best-effort) ----
MACHINE_ID=$(hostname | md5sum 2>/dev/null | cut -d' ' -f1 || hostname | md5 2>/dev/null || hostname)
VERSION=$(cat VERSION 2>/dev/null || echo "unknown")

echo ""
read -rp "Please enter your email address: " user_email
echo ""

if command -v curl &> /dev/null; then
    curl -s -X POST "$EULA_TRACKING_URL" \
        -H "Content-Type: application/json" \
        -d "{\"machine_id\":\"$MACHINE_ID\",\"email\":\"$user_email\",\"os\":\"Linux\",\"version\":\"$VERSION\"}" \
        --connect-timeout 5 --max-time 10 \
        > /dev/null 2>&1 || true
fi

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
