#!/bin/bash
# Post-installation script for FreeEed Linux Installer
echo "FreeEed extracted successfully."


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

INSTALL_DIR="$HOME/.local/share/FreeEed"
DESKTOP_FILE="$HOME/.local/share/applications/FreeEed.desktop"

echo "Installing FreeEed to $INSTALL_DIR..."
mkdir -p "$INSTALL_DIR"

# Clear any previously-exploded review webapp + its cached JSPs from an earlier
# install. Otherwise Tomcat keeps serving the OLD exploded copy and cached JSP
# classes, so a FreeEedUI update silently ships stale code (real bug: the q=*
# fix appeared "not deployed" until this dir was removed).
rm -rf "$INSTALL_DIR/freeeed-tomcat/webapps/freeeedui"
rm -rf "$INSTALL_DIR/freeeed-tomcat/work/Catalina/localhost/freeeedui"

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

# Refresh the desktop menu so the entry shows up without a logout/login.
# Best-effort: these tools may be absent on a minimal system.
update-desktop-database "$HOME/.local/share/applications" 2>/dev/null || true
xdg-desktop-menu forceupdate 2>/dev/null || true

echo "Installation complete."
echo "Launch 'FreeEed Control Panel' from your application menu (under Accessories/Office),"
echo "or run: $INSTALL_DIR/ControlPanel.sh"
