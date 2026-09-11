#!/bin/bash
# Provision the FreeEed headless server appliance (run by Packer as root).
# SCAFFOLD 2026-09-11 -- correct in shape; verify each step on the first real build.
set -euo pipefail

FREEEED_PACK_URL="${FREEEED_PACK_URL:?set by Packer}"
INSTALL_DIR="/opt/freeeed"
SVC_USER="freeeed"

echo "=== headless dependencies (no desktop) ==="
export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y --no-install-recommends \
  default-jre-headless \
  libreoffice-core libreoffice-writer libreoffice-calc libreoffice-impress \
  pst-utils \
  tesseract-ocr \
  unzip curl ufw

echo "=== service user + install dir ==="
id -u "$SVC_USER" >/dev/null 2>&1 || useradd --system --create-home --shell /usr/sbin/nologin "$SVC_USER"
install -d -o "$SVC_USER" -g "$SVC_USER" "$INSTALL_DIR"

echo "=== fetch + unpack the FreeEed pack into /opt/freeeed ==="
curl -fSL "$FREEEED_PACK_URL" -o /tmp/pack.zip
unzip -q /tmp/pack.zip -d "$INSTALL_DIR"
if [ -d "$INSTALL_DIR/freeeed_complete_pack" ]; then
  shopt -s dotglob
  mv "$INSTALL_DIR/freeeed_complete_pack/"* "$INSTALL_DIR/"
  rmdir "$INSTALL_DIR/freeeed_complete_pack"
fi
chown -R "$SVC_USER:$SVC_USER" "$INSTALL_DIR"
rm -f /tmp/pack.zip

echo "=== TODO: Tomcat must bind 0.0.0.0:8090 (LAN reachable), not 127.0.0.1 ==="
# Verify/patch freeeed-tomcat/conf/server.xml connector: port=8090, address absent or 0.0.0.0.
# grep -n 'Connector' "$INSTALL_DIR/freeeed-tomcat/conf/server.xml"

echo "=== TODO: point FreeEed output at the mounted ~500 GB data disk (not the OS disk) ==="
# e.g. mount the data volume at /data, then set the output/case dir there in settings.

echo "=== systemd service (Solr + Tika + Tomcat on boot) ==="
install -m 0644 /tmp/freeeed.service /etc/systemd/system/freeeed.service   # staged by Packer file provisioner
install -m 0755 /tmp/appliance-start.sh "$INSTALL_DIR/appliance-start.sh"
install -m 0755 /tmp/appliance-stop.sh  "$INSTALL_DIR/appliance-stop.sh"
chown "$SVC_USER:$SVC_USER" "$INSTALL_DIR/appliance-start.sh" "$INSTALL_DIR/appliance-stop.sh"
systemctl daemon-reload
systemctl enable freeeed.service

echo "=== firewall: allow SSH + FreeEedUI (8090) on the LAN ==="
ufw allow OpenSSH || true
ufw allow 8090/tcp || true
ufw --force enable || true

echo "=== provision complete -- appliance will serve http://<ip>:8090/freeeedui on boot ==="
