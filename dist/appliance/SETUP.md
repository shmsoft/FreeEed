# FreeEed Server Appliance — Setup Sheet

A ready-to-run virtual machine with FreeEed fully installed. Deploy it on your own VMware
(ESXi/vCenter; Proxmox also works), and everyone uses it from a **web browser** — nothing to
install on anyone's computer. **All data stays on your server; nothing leaves your network.**

## 1. What you need
- **VMware ESXi / vCenter** (or VMware Workstation/Fusion; Proxmox via OVF import).
- **~4 vCPU, 8 GB RAM** for the VM (fine for one or two people at a time).
- The appliance **OVA** (~3 GB) + room for a **data disk** (e.g. ~500 GB) for your documents.

## 2. Download + verify
- **OVA:** `https://shmsoft.s3.amazonaws.com/appliance/FreeEed-Appliance-10.8.7-PREVIEW.ova`
- **Checksum (optional but recommended):**
  `https://shmsoft.s3.amazonaws.com/appliance/FreeEed-Appliance-10.8.7-PREVIEW.ova.sha256`
  Verify after download: `shasum -a 256 FreeEed-Appliance-10.8.7-PREVIEW.ova` — it should match.

## 3. Import
- **vCenter:** *Hosts and Clusters* → right-click your host/cluster → **Deploy OVF Template** →
  select the `.ova` → follow the wizard (name it, pick datastore + network) → Finish.
- **ESXi host client:** *Virtual Machines* → **Create / Register VM** → *Deploy a VM from an OVF
  or OVA file* → select the `.ova` → follow prompts.
- **Proxmox (later):** import the OVF, or the disk via `qm importovf` / `qm importdisk`.

## 4. Add storage for your documents
- After import (before or after first boot), **add a second hard disk** (~500 GB) to the VM in
  vCenter/ESXi (*Edit Settings → Add New Device → Hard Disk*).
- The appliance runs on a 40 GB system disk; the extra disk is for your case data. **We'll help
  you point FreeEed's case/output storage at it** on first setup (a quick one-time step) — just
  ask; auto-mount is coming in a later build.

## 5. Power on + find its address
- Power on the VM. It gets an IP from your network via DHCP (or set a static IP/reservation).
- Find the IP in the VMware console (login prompt shows it) or your DHCP server.

## 6. Open it in a browser
- Go to **`http://<the-VM-IP>:8090/freeeedui`** from any browser on your network.
- Log in with **`admin` / `admin`**.
- **Change the password immediately** (top-right user menu → change password). This is the one
  shared login for the appliance.
- If the page doesn't load right after boot, wait ~1–2 minutes for the services to start, then refresh.

## 7. Use it
Upload documents (mbox, PST, loose files, ZIPs) → process → search, review, and tag → export /
produce. For a FOIA/records request from a Google Vault **mbox**, enable **"Create PDF Images"**
so each message is rendered to a per-document PDF.

## Notes
- **Browser-only:** users never touch the operating system. If your IT needs OS/shell access,
  use the **VMware console** (or add your own SSH key there) — the appliance ships with no OS
  password and SSH password login disabled.
- **Privacy:** everything runs on your server. FreeEed makes no outbound calls during processing.
- **Version:** 10.8.7-PREVIEW. This is a preview/daily build — solid for evaluation and real work,
  with updates coming.
- **Questions / setup help:** contact us (Mark / Scaia) any time.
