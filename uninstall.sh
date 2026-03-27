#!/bin/bash
# FreeEed Uninstaller for Linux and macOS

INSTALL_DIR="$HOME/.local/share/FreeEed"
DESKTOP_FILE="$HOME/.local/share/applications/FreeEed.desktop"

echo "Uninstalling FreeEed..."

# Remove the application files
if [ -d "$INSTALL_DIR" ]; then
    rm -rf "$INSTALL_DIR"
    echo "Removed application folder: $INSTALL_DIR"
else
    echo "Application folder not found at $INSTALL_DIR, skipping."
fi

# Remove the desktop shortcut
if [ -f "$DESKTOP_FILE" ]; then
    rm -f "$DESKTOP_FILE"
    echo "Removed desktop shortcut: $DESKTOP_FILE"
fi

# Ask user whether to remove config/env
echo ""
read -rp "Remove your config (~/.freeeed/.env)? This will delete your API keys. [y/N] " confirm
if [[ "$confirm" =~ ^[Yy]$ ]]; then
    rm -rf "$HOME/.freeeed"
    echo "Removed config directory: ~/.freeeed"
else
    echo "Config kept at ~/.freeeed/.env"
fi

echo ""
echo "FreeEed has been uninstalled."
