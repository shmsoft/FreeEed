# FreeEed Server Appliance (VM for VMware / Proxmox)

An **Ubuntu VM** running the full FreeEed stack. Two ways to work, on one box:

- **Operator console (desktop):** open the VM's **console** in your hypervisor
  (vSphere/VMware/Proxmox). The appliance auto-logs in and launches the **FreeEed operator
  console** (the Swing Control Panel) — create cases, ingest, process, produce, launch the
  player. This is the full-power path.
- **Review (browser):** staff go to `http://<vm-ip>:8090/freeeedui` to search, review, tag, and
  export — no per-client install.

All data stays on the org's own server (local-first).

First customer: a public-sector (school district) deployment — VMware → Proxmox. Customer details are kept in the private record, not in this repo.

## Why the desktop console ships (changed 2026-09-29)
The appliance was originally browser-only. Per Mark: *"the console is always needed, because
create case in the browser is a weak version of create case in the console."* So the appliance
now includes a **minimal desktop** (Xorg + openbox — no full DE) that **auto-launches the
operator console**. Browser review on `:8090` is unchanged.

## Build host (Ubuntu box)
```
cd dist/appliance && packer init . && packer build freeeed-appliance.pkr.hcl
./scripts/to-ova.sh                    # qcow2 -> OVA (VMware); Proxmox can use the qcow2 directly
```
Publish (per the build/publish rule, `--profile shmsoft`):
```
aws s3 cp output/FreeEed-Appliance-<ver>.ova        s3://shmsoft/appliance/ --acl public-read --profile shmsoft
aws s3 cp output/FreeEed-Appliance-<ver>.ova.sha256 s3://shmsoft/appliance/ --acl public-read --profile shmsoft
```

## Deploy (what the customer's IT does)
- **VMware:** Deploy OVF Template → `FreeEed-Appliance-<ver>.ova`.
- **Proxmox (later):** `qm importovf` the OVF, or import the qcow2 disk.
- Give it the basics (see sizing) + a **~500 GB** data disk. Power on.
- **Operator:** open the VM **console** → the FreeEed console appears automatically (~1-2 min).
- **Reviewers:** browse **`http://<vm-ip>:8090/freeeedui`** → log in **admin/admin**, change on first use.

## Sizing
- **4 vCPU / 12 GB RAM** default — desktop console + player JVM + Solr + Tika + Tomcat are
  co-resident; **8 GB is too tight** now. Bump to **16 GB** for heavy document volumes.
- OS+app disk ~40 GB; **separate ~500 GB data disk** for cases/output.

## What's inside (scripts/provision-appliance.sh)
- Ubuntu Server 24.04. **Full (GUI-capable) JRE** — the console is a Swing app, so NOT
  `-headless` — plus fonts. LibreOffice (imaging/PDF), readpst (PST), tesseract (OCR),
  **open-vm-tools** (VMware integration). FreeEed pack at `/opt/freeeed`.
- **Minimal desktop:** Xorg + openbox, **auto-login `freeeed` on tty1 → startx → operator
  console** (`ControlPanel.sh`) via the openbox autostart. No display manager.
- **In-VM review browser:** Firefox ESR (mozillateam PPA, no snap) set as the default handler so the
  console's Review opens it at `localhost:8090/freeeedui`; locked down for **no egress** via an
  enterprise `policies.json` (telemetry/first-run/update/captive-portal/safebrowsing off).
- **systemd `freeeed.service`** starts Solr + Tika + Tomcat/FreeEedUI on boot (always-on review).
  Auto-restart on failure.
- Tomcat bound to `0.0.0.0:8090`; ufw allows 8090 + SSH.

## Networking
- Interface-agnostic netplan (`match: e*`) + cloud-init's network layer disabled, so it DHCPs on
  **any** hypervisor NIC name/driver (KVM/VMware/ESXi/Proxmox). Fixed after the Fusion test found
  the guest didn't DHCP on VMware's NIC name (freeeed-57, 2026-09-18).

## OVA for ESXi/vCenter
- **stream-optimized VMDK** (lsilogic) + hand-rolled **OVF**, tarred **WITHOUT a `.mf`** (the
  manifest is omitted: ovftool and qemu-img disagree on the streamOptimized digest, which fails
  vSphere's manifest check a customer can't skip through the UI). Integrity via an external
  `.sha256` over HTTPS. **vmx-13**, **E1000** NIC for broad compatibility.

## Hardening
- AJP (8009) disabled. **OS password LOCKED, SSH password-auth OFF.** The desktop uses
  **autologin** (no password needed), so adding the console does **not** regress this. IT gets OS
  access via the hypervisor console or by adding their own SSH key.

## Validation history
- **Round 4 (2026-09-29, Intel Mac-2017 / Fusion):** the browser-only OVA imported without
  `--lax`, booted, DHCP'd (192.168.1.197), `:8090/freeeedui` → 302. ESXi-ready for the browser path.
- **Round 5 (pending):** re-validate this **desktop-console** build — the operator console
  auto-launches on the VM console AND `:8090` still serves.

## Open items
- Confirm the console autostarts cleanly on the VMware console and there's no conflict with the
  always-on systemd services (the Control Panel's "Start All" would double-start what systemd
  already runs — operator guidance or a detect-running tweak).
- Data-volume auto-mount (v1: documented manual step in SETUP.md).
- HTTPS/reverse proxy if off-LAN browser access is ever wanted (out of scope for v1).
