# FreeEed Server Appliance (headless VM for VMware / Proxmox)

A **headless Ubuntu Server** image running the full FreeEed stack, accessed entirely from a
**browser** — no desktop, no per-client install. An org's IT drops it on their own hypervisor
(VMware today, Proxmox later) and staff go to `http://<vm-ip>:8090/freeeedui` to **upload,
process, review, and produce** documents. All data stays on the org's own server (local-first).

First customer pulling this: Grand Valley Local Schools (Jeremiah Peckol) — VMware→Proxmox,
"basics + ~500 GB, reach it from the browser."

**Status:** scaffold (2026-09-11). Not yet built/tested — completed once KVM is live on the
build box. Reproducible via Packer (no hand-snapshot).

## Why headless works now
FreeEedUI drives the whole workflow from the browser: `FileUploadController` (upload) and
`CaseController` **runs the ingest engine itself** (`ProcessBuilder`, async). So the Swing
Player/desktop is NOT needed — only Solr + Tika + Tomcat(+FreeEedUI) run as background
services. (Desktop-download users get the separate `dist/ubuntu-vm/` OVA instead.)

## Build host (Ubuntu box, once KVM is enabled)
```
sudo apt install -y qemu-kvm libvirt-daemon-system packer
sudo usermod -aG kvm,libvirt $USER   # log out/in
cd dist/appliance && packer init . && packer build freeeed-appliance.pkr.hcl
./scripts/to-ova.sh                    # qcow2 -> OVA (VMware) ; qcow2 is used directly by Proxmox (TODO)
```

## Deploy (what Jeremiah does)
- **VMware:** Deploy OVF Template → pick `FreeEed-Appliance-<ver>.ova`.
- **Proxmox (later):** `qm importovf` the OVF, or import the raw/qcow2 disk.
- Give it the basics (see sizing) + a **~500 GB** data disk. Power on.
- Find its IP (DHCP; or set static), then browse to **`http://<vm-ip>:8090/freeeedui`**.

## Sizing (starting point — tune to concurrent users)
- 4 vCPU / 8 GB RAM for a few concurrent reviewers; scale up for more.
- OS+app disk ~40 GB; **separate ~500 GB data disk** mounted for cases/output.

## What's inside (see scripts/provision-appliance.sh)
- Ubuntu Server LTS, headless. JRE, LibreOffice (headless, imaging/PDF), readpst (PST),
  tesseract (OCR). FreeEed pack at `/opt/freeeed`.
- **systemd `freeeed.service`** starts Solr + Tika + Tomcat/FreeEedUI on boot (NOT the desktop
  Player). Auto-restart on failure.
- Tomcat bound to `0.0.0.0:8090` so the LAN can reach it; ufw allows 8090.

## Open items before shipping to Jeremiah
- **Test browser workflow end-to-end** on a fresh build: upload → process → review → produce,
  and **login** (FSUserDao first-run/empty-users must not block login on a shared server).
- Confirm Tomcat connector binds `0.0.0.0` (not `127.0.0.1`) in the pack.
- Data volume: mount the 500 GB disk and point FreeEed output there (not the OS disk).
- Decide auth model: open-on-LAN vs individual logins (ask Jeremiah).
- OVA hardware/OVF version compatible with his VMware (ESXi/vCenter vs Workstation).
- HTTPS if he ever wants off-LAN access (reverse proxy) — out of scope for v1.
