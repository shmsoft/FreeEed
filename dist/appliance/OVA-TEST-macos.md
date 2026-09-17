# HANDOFF → Mac-2017 Claude: validate the FreeEed appliance OVA via VMware Fusion

**From:** the Ubuntu FreeEed session (freeeed-56). **When:** 2026-09-16.
**Why you:** you're on an Intel Mac with VMware Fusion. We need the one test we can't do on the
Ubuntu box — that the appliance **OVA imports with VMware's OVF parser** (the closest proxy to
Jeremiah's ESXi). Fusion **bundles `ovftool`**, so this is all CLI — no GUI needed.

## Context (already verified on the Ubuntu side)
- Headless Ubuntu Server appliance; FreeEed review app served at **`:8090/freeeedui`**.
- Its **disk boots + serves the UI** (verified under KVM). App login = built-in **admin/admin**.
- OS has **no password** and **SSH password-auth is off** (browser-only appliance) — so you can't
  SSH into the guest; test it over HTTP only.
- The OVA was hand-rolled (`qemu-img` streamOptimized VMDK + OVF), so **VMware's importer is the
  unverified piece** — that's exactly what you're checking.

## Steps
1. **Download the OVA** (public S3):
   ```
   curl -fL -o ~/FreeEed-Appliance.ova \
     https://shmsoft.s3.amazonaws.com/appliance/FreeEed-Appliance-10.8.7-PREVIEW.ova
   ```
2. **Install Fusion** if needed (`brew install --cask vmware-fusion`, or the free Broadcom download),
   then locate its ovftool:
   ```
   OVFTOOL="/Applications/VMware Fusion.app/Contents/Library/VMware OVF Tool/ovftool"
   ```
3. **Import with VMware's parser — THE TEST.** Report any errors/warnings verbatim:
   ```
   "$OVFTOOL" --lax --allowExtraConfig ~/FreeEed-Appliance.ova ~/FreeEed-Appliance.vmx
   ```
   - If it errors, capture the exact message — that's the ESXi-compatibility signal, and tells us
     what to fix in `dist/appliance/scripts/to-ova.sh` (the OVF descriptor).
4. **Run headless + get the IP:**
   ```
   vmrun -T fusion start ~/FreeEed-Appliance.vmx nogui
   vmrun -T fusion getGuestIPAddress ~/FreeEed-Appliance.vmx -wait
   ```
5. **Confirm the UI:** `curl -s -o /dev/null -w '%{http_code}\n' http://<ip>:8090/freeeedui/` → expect **302**.

## Report back (edit this file's "RESULTS" section below, commit to dev, and/or tell Mark)
- ovftool import: clean / warnings (paste them) / failed (paste error)?
- Boot: yes/no. `:8090/freeeedui` HTTP code?
- Net verdict: is the OVA ESXi-ready as-is, or does the OVF need fixing?

If ovftool rejects it, the fixes on the Ubuntu side are: adjust the OVF in `to-ova.sh`, or
regenerate the OVA with ovftool directly. Ping Mark and he'll relay to the Ubuntu session.

## RESULTS (Mac-2017 Claude: fill this in)
_(pending)_
