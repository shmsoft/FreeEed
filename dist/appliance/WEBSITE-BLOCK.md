# FreeEed Appliance — website download block

Ready-to-paste content for the **freeeed.org** download page. Maintained by the FreeEed
appliance build; handed to the site-editing/CRM session to publish.

> **PUBLISH STATUS:** DRAFT. Do **not** go live publicly until the GA build is published and the
> build session gives the "go live" signal. The **SHA-256 and version are placeholders** below —
> replace them with the final published values (from the `.ova.sha256`) at GA. A **private link to a
> named customer** (e.g. the current VMware/ESXi prospect) is fine before GA.
>
> - Stable OVA URL (unchanged across versions): `https://shmsoft.s3.amazonaws.com/appliance/FreeEed-Appliance-10.8.7.ova`
> - Checksum file: `https://shmsoft.s3.amazonaws.com/appliance/FreeEed-Appliance-10.8.7.ova.sha256`

---

## Plain version (WordPress/Elementor text block)

## FreeEed Appliance — download

**Self-hosted eDiscovery & FOIA, as a ready-to-run virtual machine.** Import it on your own VMware
or Proxmox, and your team works from a browser. **All data stays on your server — nothing leaves
your network.**

**[Download the FreeEed Appliance (OVA, ~3.8 GB)](https://shmsoft.s3.amazonaws.com/appliance/FreeEed-Appliance-10.8.7.ova)**
SHA-256: `{{FILL FROM .sha256 AT GA}}` · [checksum file](https://shmsoft.s3.amazonaws.com/appliance/FreeEed-Appliance-10.8.7.ova.sha256)
Version 10.8.7 · Apache-2.0

**What you need**
- VMware **ESXi / vCenter** (or Workstation/Fusion; **Proxmox** via OVF import)
- **4 vCPU, 12 GB RAM** (16 GB for large volumes)
- ~40 GB system disk **+ a ~500 GB data disk** for your documents

**Get started in 4 steps**
1. **Import** the `.ova` (vCenter -> Deploy OVF Template; ESXi -> Create/Register VM; Proxmox -> `qm importovf`).
2. **Add a ~500 GB data disk** for your case data.
3. **Power on.** The operator console opens on the VM console — create a case, add documents, process.
4. **Review in a browser:** go to `http://<vm-ip>:8090/freeeedui`, log in `admin` / `admin`, and **change the password**.

**Private by design** — everything runs on your server, and FreeEed makes **no outbound calls during
processing**. Questions or setup help? Contact us.

---

## HTML version (Elementor "HTML" widget)

```html
<section class="freeeed-appliance-dl">
  <h2>FreeEed Appliance — download</h2>
  <p><strong>Self-hosted eDiscovery &amp; FOIA, as a ready-to-run virtual machine.</strong>
     Import it on your own VMware or Proxmox, and your team works from a browser.
     <em>All data stays on your server — nothing leaves your network.</em></p>

  <p>
    <a class="btn" href="https://shmsoft.s3.amazonaws.com/appliance/FreeEed-Appliance-10.8.7.ova">
      Download the FreeEed Appliance (OVA, ~3.8&nbsp;GB)</a>
  </p>
  <p style="font-size:.9em;color:#555">
    SHA-256: <code>{{FILL FROM .sha256 AT GA}}</code>
    · <a href="https://shmsoft.s3.amazonaws.com/appliance/FreeEed-Appliance-10.8.7.ova.sha256">checksum file</a>
    · Version 10.8.7 · Apache-2.0
  </p>

  <h3>What you need</h3>
  <ul>
    <li>VMware <strong>ESXi / vCenter</strong> (or Workstation/Fusion; <strong>Proxmox</strong> via OVF import)</li>
    <li><strong>4 vCPU, 12&nbsp;GB RAM</strong> (16&nbsp;GB for large volumes)</li>
    <li>~40&nbsp;GB system disk <strong>+ a ~500&nbsp;GB data disk</strong> for your documents</li>
  </ul>

  <h3>Get started in 4 steps</h3>
  <ol>
    <li><strong>Import</strong> the <code>.ova</code> (vCenter -> Deploy OVF Template; ESXi -> Create/Register VM; Proxmox -> <code>qm importovf</code>).</li>
    <li><strong>Add a ~500&nbsp;GB data disk</strong> for your case data.</li>
    <li><strong>Power on.</strong> The operator console opens on the VM console — create a case, add documents, process.</li>
    <li><strong>Review in a browser:</strong> <code>http://&lt;vm-ip&gt;:8090/freeeedui</code>, log in <code>admin</code>/<code>admin</code>, then change the password.</li>
  </ol>

  <p><strong>Private by design</strong> — everything runs on your server; no outbound calls during processing.
     Questions or setup help? <a href="mailto:mark@scaia.ai">Contact us</a>.</p>
</section>
```
