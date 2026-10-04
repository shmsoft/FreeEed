#!/bin/bash
# Provision the FreeEed headless server appliance (run by Packer as root).
# SCAFFOLD 2026-09-11 -- correct in shape; verify each step on the first real build.
set -euo pipefail

FREEEED_PACK_URL="${FREEEED_PACK_URL:?set by Packer}"
INSTALL_DIR="/opt/freeeed"
SVC_USER="freeeed"

echo "=== dependencies (server stack + minimal desktop for the operator console) ==="
export DEBIAN_FRONTEND=noninteractive
apt-get update
# Full (GUI-capable) JRE -- the operator console is a Swing app, so NOT the -headless variant.
apt-get install -y --no-install-recommends \
  default-jre \
  libreoffice-core libreoffice-writer libreoffice-calc libreoffice-impress \
  pst-utils \
  tesseract-ocr \
  unzip curl ufw \
  fonts-dejavu-core fontconfig
# Minimal X for the operator console. WITH recommends so Xorg pulls its video/input drivers
# (incl. VMware) -- a --no-install-recommends X often boots with no keyboard/mouse or no display.
apt-get install -y \
  xserver-xorg xserver-xorg-video-vmware xserver-xorg-input-libinput \
  xinit openbox x11-xserver-utils xterm open-vm-tools

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

echo "=== interface-agnostic networking (DHCP on ANY NIC name/driver) ==="
# The image is built under KVM (virtio -> ens3), but the customer runs it on VMware/ESXi
# (E1000/vmxnet -> ens33/ens160) or Proxmox. A netplan pinned to the build-time NIC name (or
# relying on cloud-init to guess the NIC at the customer, which does not DHCP reliably on a
# plain OVF deploy with no datasource) leaves the appliance with NO IP -> :8090 unreachable.
# (Found via the Mac Fusion test, freeeed-57, 2026-09-18: guest never DHCP'd under bridged/NAT.)
# Fix: our own netplan that matches EVERY ethernet interface, and disable cloud-init's network
# layer so it can't override this or leave a stale NIC-pinned 50-cloud-init.yaml.
cat > /etc/netplan/99-freeeed-net.yaml <<'NETEOF'
network:
  version: 2
  ethernets:
    alleth:
      match:
        name: "e*"
      dhcp4: true
      dhcp6: false
      optional: true
NETEOF
chmod 600 /etc/netplan/99-freeeed-net.yaml            # netplan warns on world-readable configs
echo 'network: {config: disabled}' > /etc/cloud/cloud.cfg.d/99-disable-network-config.cfg
rm -f /etc/netplan/50-cloud-init.yaml 2>/dev/null || true   # drop any NIC-pinned cloud-init netplan

echo "=== minimal desktop: autologin $SVC_USER -> openbox -> FreeEed operator console ==="
# Mark's requirement (2026-09-29): the desktop operator console (Swing Control Panel) must ship
# in the VM -- "create case in the browser is a weak version of create case in the console".
# Browser review on :8090 is unchanged; this adds the console, reachable via the hypervisor's VM
# console. No display manager (minimal): auto-login $SVC_USER on tty1 and startx. The OS password
# stays LOCKED and SSH password-auth stays OFF (autologin needs no password) -> no hardening regression.
FHOME="$(getent passwd "$SVC_USER" | cut -d: -f6)"

# Pre-accept the EULA + seed config so the console launches UNATTENDED. ControlPanel.sh's
# first-run EULA gate uses `read`; from the openbox autostart there is no tty, so read returns
# empty -> "You must accept the EULA" -> exit 1 -> black screen (found in round-5, 2026-09-29).
# Pre-accepting also skips the first-run outbound curl to api.freeeed.org (forensic appliance).
install -d -o "$SVC_USER" -g "$SVC_USER" "$FHOME/.freeeed"
cat > "$FHOME/.freeeed/.eula_accepted" <<EULAEOF
accepted=$(date -u +%Y-%m-%dT%H:%M:%SZ)
email=appliance@freeeed.local
EULAEOF
cat > "$FHOME/.freeeed/.env" <<ENVEOF
OPENAI_API_KEY=
CHROMA_PERSIST_DIR=chroma_data
LLM_MODEL=gpt-4o-mini
CHROMA_EMBED_MODEL=text-embedding-3-small
TOP_K=10
PORT=8000
ENVEOF
chown -R "$SVC_USER:$SVC_USER" "$FHOME/.freeeed"

# getty auto-login on tty1
install -d /etc/systemd/system/getty@tty1.service.d
cat > /etc/systemd/system/getty@tty1.service.d/autologin.conf <<GETTYEOF
[Service]
ExecStart=
ExecStart=-/sbin/agetty --autologin $SVC_USER --noclear %I \$TERM
GETTYEOF

# start X on tty1 login only
cat > "$FHOME/.bash_profile" <<'PROFEOF'
# Auto-start the FreeEed operator console on the physical VM console (tty1) only.
if [ -z "${DISPLAY:-}" ] && [ "${XDG_VTNR:-}" = "1" ]; then
  exec startx
fi
PROFEOF

cat > "$FHOME/.xinitrc" <<'XINITEOF'
#!/bin/sh
export BROWSER=firefox
exec openbox-session
XINITEOF

install -d "$FHOME/.config/openbox"
cat > "$FHOME/.config/openbox/autostart" <<'OBEOF'
# no screen blanking on the console
xset s off -dpms 2>/dev/null || true
# Operator console = the Player (create case, ingest, process, open review). Launched DIRECTLY
# (not the Service Manager) so the operator never hits "Start All" -- Solr/Tika/Tomcat already run
# via systemd (freeeed.service). Log output; on exit show the log in an xterm, not a blank screen.
(
  cd /opt/freeeed/FreeEed
  ./freeeed_player.sh >"$HOME/freeeed-console.log" 2>&1
  echo "[FreeEed Player exited $? at $(date)]" >>"$HOME/freeeed-console.log"
  command -v xterm >/dev/null 2>&1 && \
    xterm -geometry 120x40 -e sh -c 'cat "$HOME/freeeed-console.log"; echo; echo "[Player exited -- Enter to relaunch]"; read x; exec openbox --exit' &
) &
OBEOF

# Minimal openbox root menu (NO terminal -> no one-click sudo shell at the console; Mark's call
# 2026-10-01). Also fixes the "black desktop" by giving a useful right-click menu.

cat > "$FHOME/.config/openbox/menu.xml" <<'MENUEOF'
<?xml version="1.0" encoding="UTF-8"?>
<openbox_menu xmlns="http://openbox.org/3.4/menu">
  <menu id="root-menu" label="FreeEed">
    <item label="Open FreeEed Review (browser)">
      <action name="Execute"><command>firefox http://localhost:8090/freeeedui</command></action>
    </item>
    <item label="FreeEed Operator Console (Player)">
      <action name="Execute"><command>sh -c 'cd /opt/freeeed/FreeEed &amp;&amp; ./freeeed_player.sh'</command></action>
    </item>
  </menu>
</openbox_menu>
MENUEOF

chown -R "$SVC_USER:$SVC_USER" "$FHOME/.bash_profile" "$FHOME/.xinitrc" "$FHOME/.config"

echo "=== in-VM review browser (Firefox ESR -- no snap, no egress) ==="
# Mark's decision (2026-09-30): ship a browser so Review opens INSIDE the VM from the console.
# The console (UtilUI.openBrowser/hasBrowser) only opens a browser if one named firefox/chromium is
# on PATH, then calls Desktop.browse -> xdg-open. So: install Firefox ESR and guarantee a `firefox`
# on PATH + make it the default handler. ESR via the mozillateam PPA = a real deb (Ubuntu's firefox
# is a snap, awkward to bake into a Packer image).
apt-get install -y --no-install-recommends software-properties-common ca-certificates gnupg
add-apt-repository -y ppa:mozillateam/ppa
cat > /etc/apt/preferences.d/mozilla-firefox <<'PINEOF'
Package: firefox*
Pin: release o=LP-PPA-mozillateam
Pin-Priority: 1001
PINEOF
apt-get update
apt-get install -y --no-install-recommends firefox-esr
# hasBrowser() matches a binary literally named "firefox" -> guarantee one on PATH.
BBIN="$(command -v firefox-esr || command -v firefox || true)"
[ -n "$BBIN" ] || { echo "ERROR: firefox-esr not installed" >&2; exit 1; }
ln -sf "$BBIN" /usr/local/bin/firefox
update-alternatives --install /usr/bin/x-www-browser x-www-browser "$BBIN" 200 2>/dev/null || true

# No-egress enterprise policy: no telemetry / first-run / auto-update / captive-portal /
# safebrowsing pings; homepage = the local review app. (FreeEed no-outbound principle.)
cat > /tmp/ff-policies.json <<'POLEOF'
{
  "policies": {
    "DisableTelemetry": true,
    "DisableFirefoxStudies": true,
    "DisablePocket": true,
    "DisableFirefoxAccounts": true,
    "DisableAppUpdate": true,
    "DisableSystemAddonUpdate": true,
    "ExtensionUpdate": false,
    "DontCheckDefaultBrowser": true,
    "OverrideFirstRunPage": "",
    "OverridePostUpdatePage": "",
    "NetworkPrediction": false,
    "CaptivePortal": false,
    "SearchSuggestEnabled": false,
    "Homepage": { "URL": "http://localhost:8090/freeeedui", "StartPage": "homepage" },
    "Preferences": {
      "browser.safebrowsing.malware.enabled": { "Value": false, "Status": "locked" },
      "browser.safebrowsing.phishing.enabled": { "Value": false, "Status": "locked" },
      "browser.safebrowsing.downloads.enabled": { "Value": false, "Status": "locked" },
      "network.captive-portal-service.enabled": { "Value": false, "Status": "locked" },
      "toolkit.telemetry.enabled": { "Value": false, "Status": "locked" },
      "datareporting.healthreport.uploadEnabled": { "Value": false, "Status": "locked" },
      "app.update.enabled": { "Value": false, "Status": "locked" }
    }
  }
}
POLEOF
# Install the policy where firefox-esr reads it (distribution dir) + the /etc locations.
FFDIR="$(dirname "$(readlink -f "$BBIN")")"
for d in "$FFDIR/distribution" /etc/firefox-esr/policies /etc/firefox/policies; do
  install -d "$d"; install -m 0644 /tmp/ff-policies.json "$d/policies.json"
done
rm -f /tmp/ff-policies.json
# Java's Desktop.browse() launches via gio, which uses the system DEFAULT handler (mimeapps.list),
# NOT update-alternatives or the "firefox" PATH name. So register firefox-esr as the default http/
# https/html handler, AND guarantee a `firefox` on /usr/bin (always on PATH) for hasBrowser().
ln -sf "$BBIN" /usr/bin/firefox
# Register firefox as the default http/https/html handler. Detect the ACTUAL .desktop name (the
# mozillateam firefox-esr package may ship firefox-esr.desktop or firefox.desktop), and write it at
# the USER level (~/.config/mimeapps.list, highest precedence for xdg-open) plus system level.
FFDESK="$(cd /usr/share/applications 2>/dev/null && ls firefox*.desktop 2>/dev/null | head -1)"
[ -n "$FFDESK" ] || FFDESK="firefox-esr.desktop"
for mdir in "$FHOME/.config" /usr/share/applications /etc/xdg; do
  install -d "$mdir"
  cat > "$mdir/mimeapps.list" <<MIMEEOF
[Default Applications]
x-scheme-handler/http=$FFDESK
x-scheme-handler/https=$FFDESK
text/html=$FFDESK
MIMEEOF
done
chown -R "$SVC_USER:$SVC_USER" "$FHOME/.config"
update-desktop-database /usr/share/applications 2>/dev/null || true
echo "firefox default handler = $FFDESK (user + system mimeapps); /usr/bin/firefox -> $BBIN"


echo "=== bake a tiny no-egress sample dataset (for first-run + the full-workflow test) ==="
SD="$INSTALL_DIR/sample-data"
install -d -o "$SVC_USER" -g "$SVC_USER" "$SD"
cat > "$SD/memo1.txt" <<'S1'
Confidential memo re: the Acme acquisition.
This document discusses the budget and is marked PRIVILEGE.
S1
cat > "$SD/notes.txt" <<'S2'
Project notes -- follow up with counsel.
The term PRIVILEGE appears here as well. Contact: jane@example.com
S2
cat > "$SD/report.txt" <<'S3'
Quarterly status report. Routine operations, nothing sensitive.
S3
cat > "$SD/message1.eml" <<'S4'
From: alice@example.com
To: bob@example.com
Subject: Budget review
Date: Mon, 01 Sep 2026 10:00:00 +0000

Bob, here are the budget numbers for review. Regards, Alice.
S4
chown -R "$SVC_USER:$SVC_USER" "$SD"
cat > "$FHOME/Getting-Started.txt" <<'GS'
FreeEed appliance -- quick start
  1. The operator console (Player) opens automatically on this screen.
  2. Create a new case/project, then add the folder:  /opt/freeeed/sample-data
  3. Process it, then open Review (in-VM browser) or browse
     http://<this-vm-ip>:8090/freeeedui from another computer.  Log in admin/admin.
Sample set = 4 files (memo1.txt, notes.txt, report.txt, message1.eml).
Searching "PRIVILEGE" should return 2 hits (memo1.txt, notes.txt).
GS
chown "$SVC_USER:$SVC_USER" "$FHOME/Getting-Started.txt"

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
