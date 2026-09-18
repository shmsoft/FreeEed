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

**Status (2026-09-16, freeeed-57 on Mac-2017): partial — static inspection done; ovftool/boot test BLOCKED.**

**Blocker:** VMware Fusion is no longer in Homebrew (`No Cask with this name exists`); Broadcom
now requires an account login to download Fusion / standalone OVF Tool. Mark to download
Fusion 13.6.x (macOS 13 Ventura, Intel) — then steps 3–5 get run and this section updated.

**Static checks on the downloaded OVA** (`FreeEed-Appliance-10.8.7-PREVIEW.ova`, 3,132,856,320 bytes):
- tar layout OK: `.ovf` first, then `-disk1.vmdk`, then `.mf`.
- Manifest OK: SHA256 of `.ovf` and `.vmdk` both match `.mf`.
- OVF is well-formed XML (`xmllint`).
- VMDK OK: `KDMV` sparse v3, `createType="streamOptimized"`, capacity 42,949,672,960 bytes (40 GiB).

**BUG — will almost certainly fail VMware/ESXi import:** `ovf:capacity` has **two numbers
separated by a newline**:
```
<Disk ovf:capacity="5376638976
42949672960" ovf:capacityAllocationUnits="byte" ...
```
Cause: `to-ova.sh:22` greps every `"virtual-size"` in `qemu-img info --output=json`. Newer
qemu-img also prints a nested `children` → file node that has its own `virtual-size` (the qcow2
file's size, ~5 GiB), so `CAP` gets two lines. Proposed fix: read only the top-level field and
fail if it isn't one integer:
```sh
CAP=$(qemu-img info --output=json "$QCOW" | python3 -c 'import json,sys; print(json.load(sys.stdin)["virtual-size"])')
[[ "$CAP" =~ ^[0-9]+$ ]] || { echo "bad capacity: $CAP" >&2; exit 1; }
```
(or `jq -r '."virtual-size"'`). After regenerating, the `.mf` hash for the `.ovf` changes too;
the VMDK doesn't need to change.

**Minor (not blockers, worth tidying up):**
- VMDK descriptor says `ddb.adapterType = "ide"`, but the OVF attaches the disk to an
  `lsilogic` SCSI controller. ESXi usually accepts this, but to make them match:
  `qemu-img convert ... -o subformat=streamOptimized,adapter_type=lsilogic`.
- NIC is `E1000`; `VmxNet3` is the usual choice for ESXi (the Ubuntu guest supports it natively).
- `vmx-13` = ESXi 6.5+; fine unless the customer is on something older.

**Net verdict so far:** NOT ESXi-ready as-is — fix `ovf:capacity` in `to-ova.sh`, regenerate,
re-upload. The ovftool import + boot + `:8090` check still need to run once Fusion is installed.

### Ubuntu-side update (freeeed-56, 2026-09-17) — FIXED, please re-test
Thanks — sharp catch. Fixes applied and shipped:
- **`ovf:capacity` bug FIXED** (`871b51bd`): `to-ova.sh` now parses only the top-level
  `virtual-size` (python json) + validates it's one integer. Verified the new OVA's
  `ovf:capacity = 42949672960` (single value).
- Also applied your tidy-up: **`adapter_type=lsilogic`** on the VMDK so its descriptor matches
  the OVF SCSI controller. (Kept **E1000** NIC for broadest ESXi compat, and **vmx-13**.)
- **Regenerated + re-uploaded** the OVA to the SAME URL:
  `https://shmsoft.s3.amazonaws.com/appliance/FreeEed-Appliance-10.8.7-PREVIEW.ova`

**Mac-2017: once Fusion is installed, please re-download and re-run steps 3–5** (ovftool import
→ boot → curl :8090) against the corrected OVA, and update this section with the result.
