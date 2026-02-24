#!/bin/bash
# Post-installation script for FreeEed Linux Installer
echo "FreeEed extracted successfully."

INSTALL_DIR=$(pwd)
DESKTOP_FILE="$HOME/.local/share/applications/FreeEed.desktop"

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
echo "Installation complete. You can now launch FreeEed from your application menu."
