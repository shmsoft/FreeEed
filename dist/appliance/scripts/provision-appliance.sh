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

echo "=== hardening (before ship) ==="
# 1. Disable the unused AJP connector (removes the 8009 init SEVEREs + a network surface).
AJP='<Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />'
sed -i "s#$AJP#<!-- AJP disabled (unused): $AJP -->#" /opt/freeeed/freeeed-tomcat/conf/server.xml || true

# 2. No shipped OS credential: remove the build-only freeeed/freeeed password and turn OFF SSH
#    password auth. The account + (NOPASSWD) sudo stay so IT can, via the hypervisor CONSOLE,
#    add their own SSH key or set a password. End users never touch the OS -- it's browser-only.
#    (00- sorts before cloud-init's 50-cloud-init.conf, and sshd is first-match-wins, so this wins.)
passwd -l freeeed || true
printf 'PasswordAuthentication no\nKbdInteractiveAuthentication no\n' > /etc/ssh/sshd_config.d/00-freeeed-hardening.conf
# NOTE: not restarting sshd here (would risk Packer's live session); applies on next boot.

# 3. Golden-image prep so every deployed clone boots fresh + unique (avoids reused SSH host
#    keys / duplicate machine-id / DHCP collisions across Jeremiah's clones):
cloud-init clean --logs 2>/dev/null || true          # re-run cloud-init fresh at the customer
: > /etc/machine-id || true                            # systemd regenerates a unique one on boot
rm -f /etc/ssh/ssh_host_* 2>/dev/null || true          # regenerated on first boot
find /opt/freeeed/freeeed-tomcat/logs -type f -delete 2>/dev/null || true

echo "=== provision complete -- appliance will serve http://<ip>:8090/freeeedui on boot ==="
