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

## Confirmed requirements (first customer, 2026-09-15)
- **Concurrency:** ≤1 user at a time (2 people total) → small VM is fine.
- **Auth:** **single shared login** — the app already seeds a default **`admin` / `admin`**
  on startup (`FSUserDao.createAdminUser()`, full rights), so empty-users does NOT block login.
  Delivery = tell Jeremiah to log in as admin/admin and **change the password on first use**.
  (No custom seeding needed.)
- **Hypervisor:** **VMware ESXi / vCenter** → the OVA must be ESXi-compatible (see below).

## Sizing
- **4 vCPU / 8 GB RAM** (comfortable for one user: imaging + Solr + Tika + Tomcat).
- OS+app disk ~40 GB; **separate ~500 GB data disk** mounted for cases/output.

## OVA for ESXi/vCenter (stricter than VirtualBox)
ESXi will not import a VirtualBox-flavored OVA. Produce:
- a **stream-optimized VMDK**: `qemu-img convert -O vmdk -o subformat=streamOptimized ...`
- a proper **OVF descriptor**, packaged/validated with VMware **`ovftool`** (free download).
- a broadly-compatible virtual hardware version (e.g. vmx-13/14) so it imports on his ESXi.
Proxmox (later) imports the same OVF, or the qcow2 directly.

## What's inside (see scripts/provision-appliance.sh)
- Ubuntu Server LTS, headless. JRE, LibreOffice (headless, imaging/PDF), readpst (PST),
  tesseract (OCR). FreeEed pack at `/opt/freeeed`.
- **systemd `freeeed.service`** starts Solr + Tika + Tomcat/FreeEedUI on boot (NOT the desktop
  Player). Auto-restart on failure.
- Tomcat bound to `0.0.0.0:8090` so the LAN can reach it; ufw allows 8090.

## Verified on the first build (2026-09-16)
- **Services start via systemd `freeeed.service` → `start_dev_services.sh`, which starts ALL
  THREE** (Tomcat + Solr + Tika). `appliance-start.sh` must NOT also call `startup.sh` — that
  double-started Tomcat, the 2nd instance failed to bind 8090/8009/8005, and 8090 ended up
  served by neither. Fixed (start/stop scripts now just call start/stop_dev_services.sh) + verified.
- **Ports:** 8090/8983/9998 all listen; `*:8090` (LAN-reachable — Tomcat binds all interfaces
  by default, no server.xml change needed). `/freeeedui` → 302 → main.html; login.html → 200.
- **Login:** built-in **admin/admin** (`FSUserDao.createAdminUser()`), full rights — empty-users
  does NOT block login.

## Open items before shipping to Jeremiah
- **Test browser workflow end-to-end** on a fresh (rebuilt) image: log in (admin/admin) →
  upload → process → review → produce.
- **Harden the OS build credential** (`freeeed`/`freeeed` + NOPASSWD sudo is build-only) —
  rotate/disable or key-only before shipping; don't ship a known OS password.
- Data volume: mount the 500 GB disk and point FreeEed output there (not the OS disk).
- Auth: tell Jeremiah to log in as admin/admin and change the password on first use.
- OVA: DECIDED — target ESXi/vCenter → stream-optimized VMDK + OVF via `ovftool` (see above).
- HTTPS if he ever wants off-LAN access (reverse proxy) — out of scope for v1.
