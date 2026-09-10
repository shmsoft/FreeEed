#!/bin/bash
# Provision the FreeEed Ubuntu VM image (run by Packer as root). SCAFFOLD 2026-09-10 --
# correct in shape; verify each step on the first real build once KVM is live.
set -euo pipefail

FREEEED_PACK_URL="${FREEEED_PACK_URL:?set by Packer}"
TARGET_USER="freeeed"
TARGET_HOME="/home/${TARGET_USER}"

echo "=== base dependencies (so the pack 'just works' -- no install walls) ==="
export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y \
  default-jre \
  libreoffice \
  pst-utils \
  tesseract-ocr \
  unzip curl

echo "=== fetch + unpack the FreeEed complete pack ==="
install -d -o "$TARGET_USER" -g "$TARGET_USER" "${TARGET_HOME}/FreeEed"
curl -fSL "$FREEEED_PACK_URL" -o /tmp/freeeed_pack.zip
unzip -q /tmp/freeeed_pack.zip -d "${TARGET_HOME}/FreeEed"
# The zip contains freeeed_complete_pack/ -- flatten so ControlPanel.sh sits at the root.
if [ -d "${TARGET_HOME}/FreeEed/freeeed_complete_pack" ]; then
  shopt -s dotglob
  mv "${TARGET_HOME}/FreeEed/freeeed_complete_pack/"* "${TARGET_HOME}/FreeEed/"
  rmdir "${TARGET_HOME}/FreeEed/freeeed_complete_pack"
fi
chown -R "$TARGET_USER:$TARGET_USER" "${TARGET_HOME}/FreeEed"
rm -f /tmp/freeeed_pack.zip

echo "=== desktop shortcut + autostart of the Control Panel on login ==="
install -d -o "$TARGET_USER" -g "$TARGET_USER" "${TARGET_HOME}/.config/autostart" "${TARGET_HOME}/Desktop"
DESKTOP_ENTRY="[Desktop Entry]
Type=Application
Name=FreeEed Control Panel
Exec=bash -lc 'cd ${TARGET_HOME}/FreeEed && ./ControlPanel.sh'
Icon=utilities-terminal
Terminal=false
X-GNOME-Autostart-enabled=true"
printf '%s\n' "$DESKTOP_ENTRY" > "${TARGET_HOME}/.config/autostart/freeeed.desktop"
printf '%s\n' "$DESKTOP_ENTRY" > "${TARGET_HOME}/Desktop/FreeEed.desktop"
chmod +x "${TARGET_HOME}/Desktop/FreeEed.desktop"
chown -R "$TARGET_USER:$TARGET_USER" "${TARGET_HOME}/.config" "${TARGET_HOME}/Desktop"

echo "=== TODO: preload a sample case + a README on the Desktop ==="
# TODO: drop a small sample data set (e.g. the mbox demo) and a Getting-Started.txt so the
#       first-run experience shows the FOIA mbox->PDF flow with zero setup.

echo "=== provision complete ==="
