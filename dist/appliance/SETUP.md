# FreeEed Server Appliance — Setup Sheet

A ready-to-run virtual machine with FreeEed fully installed. Deploy it on your own VMware
(ESXi/vCenter; Proxmox also works). **All data stays on your server; nothing leaves your network.**

**Two ways to use it:**
- **Operator (you / IT):** open the VM's **console** in vSphere/VMware — the FreeEed **operator
  console** opens automatically (create cases, load documents, process).
- **Reviewers:** from any browser on your network, go to **`http://<VM-IP>:8090/freeeedui`** to
  search, review, tag, and export. Nothing to install on their computers.

## 1. What you need
- **VMware ESXi / vCenter** (or VMware Workstation/Fusion; Proxmox via OVF import).
- **~4 vCPU, 12 GB RAM** for the VM (use **16 GB** for large document volumes).
- The appliance **OVA** (~4 GB) + room for a **data disk** (e.g. ~500 GB) for your documents.

## 2. Download + verify
- **OVA:** `https://shmsoft.s3.amazonaws.com/appliance/FreeEed-Appliance-10.8.7.ova`
- **Checksum:** `https://shmsoft.s3.amazonaws.com/appliance/FreeEed-Appliance-10.8.7.ova.sha256`
  Verify: `shasum -a 256 FreeEed-Appliance-10.8.7.ova` — it should match.

## 3. Import
- **vCenter:** *Hosts and Clusters* → right-click host/cluster → **Deploy OVF Template** → select
  the `.ova` → name it, pick datastore + network → Finish.
- **ESXi host client:** *Virtual Machines* → **Create / Register VM** → *Deploy a VM from an OVF or
  OVA file* → select the `.ova` → follow prompts.
- **Proxmox (later):** import the OVF, or the disk via `qm importovf` / `qm importdisk`.

## 4. Add storage for your documents
- Add a **second hard disk** (~500 GB) to the VM in vCenter/ESXi (*Edit Settings → Add New Device
  → Hard Disk*). The appliance runs on a 40 GB system disk; the extra disk is for case data.
- **We'll help you point FreeEed's case/output storage at it** on first setup (a quick one-time
  step) — just ask; auto-mount is coming in a later build.

## 5. Power on + find its address
- Power on the VM. It gets an IP from your network via DHCP (or set a static IP/reservation).
- Find the IP in the VM console (login prompt shows it) or your DHCP server.

## 6a. Operator console — create cases + process (in the VM console)
- Open the VM's **console** in vSphere/VMware. After boot (~1-2 min) the appliance auto-logs in
  and the **FreeEed operator console** appears on its own.
- Use it to **create a case, add documents, and process**. This is the full-featured path for
  setting up and running the work.
- **Review opens right here in the VM**: the console's Review / "Open FreeEed UI" opens the
  built-in browser at the local review app — no need to leave the console. (Reviewers on other
  machines still use the browser URL in 6b.)

## 6b. Review in a browser
- Go to **`http://<the-VM-IP>:8090/freeeedui`** from any browser on your network.
- Log in with **`admin` / `admin`**, then **change the password immediately** (top-right user menu).
  This is the one shared login for review.
- If the page doesn't load right after boot, wait ~1-2 minutes for services to start, then refresh.

## 7. Use it
Create a case and add documents in the **operator console**, process them, then **review in the
browser** (search, tag, export/produce). For a FOIA/records request from a Google Vault **mbox**,
enable **"Create PDF Images"** so each message is rendered to a per-document PDF.

## Notes
- **Operator = the VM console; reviewers = the browser.** Users' own PCs need nothing installed.
- The VM **auto-logs in on its console** to show the operator console. The OS account password is
  **locked** and SSH password login is **disabled** — if IT needs shell access, use the VM console
  or add your own SSH key there.
- **Privacy:** everything runs on your server. FreeEed makes no outbound calls during processing.
- **Version:** 10.8.7 (GA).
- **Questions / setup help:** contact us (Mark / Scaia) any time.
