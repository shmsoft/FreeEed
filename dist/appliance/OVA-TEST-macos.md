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

### Mac-2017 round 2 (freeeed-57, 2026-09-17) — capacity fix CONFIRMED; two NEW blockers
Environment: Intel Mac (i7-7920HQ), macOS 13.7.8, **VMware Fusion installed — `ovftool 4.6.3
(build-24679215)`**. Re-downloaded the corrected OVA (3,132,856,320 bytes, Last-Modified
2026-09-18 00:04 UTC).

**Static re-check of the corrected OVA — all good:**
- `ovf:capacity="42949672960"` — **single value, bug fixed.** ✅
- VMDK descriptor now `ddb.adapterType = "lsilogic"` (matches the OVF SCSI controller). ✅
- Manifest SHA256 for both `.ovf` and `.vmdk` verified by hand (`shasum -a 256 -c`) — **match**. ✅
- OVF well-formed XML; VMDK `KDMV` sparse v3, `createType="streamOptimized"`, cap 40 GiB. ✅

**BLOCKER 1 — ovftool rejects the manifest; NO VM is produced.** `ovftool --lax --allowExtraConfig`:
```
The manifest validates
Error: SHA digest of file FreeEed-Appliance-10.8.7-PREVIEW-disk1.vmdk does not match manifest
Warning:
 - No supported manifest(sha1, sha256, sha512) entry found for: 'FreeEed-Appliance-10.8.7-PREVIEW-disk1.vmdk'.
Completed with errors
```
Two separate things going on:
- **(a) Member order is wrong.** The OVA tar is `.ovf`, `.vmdk`, `.mf` — the **`.mf` must come
  BEFORE the disk** (OVF spec: descriptor, manifest, cert, then the files in `References` order).
  ovftool streams, so it hashes the disk before it has read the manifest → the "No supported
  manifest entry found" warning. **Verified:** repacking the exact same 3 files in the order
  `.ovf`, `.mf`, `.vmdk` makes that warning disappear and `The manifest validates` moves to the
  top. Fix in `to-ova.sh`: list the `.mf` before the `.vmdk` in the `tar` invocation.
- **(b) The digest mismatch persists even after reordering** — and the stored bytes DO hash
  correctly per `shasum`. So ovftool and `qemu-img`'s streamOptimized writer disagree about where
  the disk stream ends (ovftool appears to hash only the bytes it consumed, stopping at the
  end-of-stream marker, not the full file). **This is the remaining ESXi blocker** — Jeremiah
  can't pass `--skipManifestCheck` through the vSphere UI. Suggested fixes, in order of
  preference: (1) build the OVA with **ovftool** itself (`ovftool src.vmx out.ova`) so writer and
  reader agree; (2) ship the OVA **without a `.mf`** (the manifest is optional and ESXi accepts
  its absence) — weakest but unblocks the customer; (3) compute the `.mf` digest over whatever
  byte range ovftool actually consumes (fragile, not recommended).

**With `--skipManifestCheck` the import SUCCEEDS** — so the disk stream itself is fine:
```
Transfer Completed
The manifest does not validate
Warning:
 - The manifest is present but user flag causing to skip it
Completed successfully          (real 1m21s)
```

**BLOCKER 2 — ovftool writes an invalid hardware version into the VMX.** The generated
`FreeEed-test.vmx` contains `virtualhw.version = "99"` (not a real HW version; OVF says `vmx-13`).
Worked around locally by editing it to `13`. Likely a side effect of `--lax` disabling the
hardware-compatibility check; worth re-testing **without `--lax`** once the manifest is fixed,
since `--lax` should not be needed for a well-formed OVA.

**Boot + `:8090`: NOT YET RUN.** `vmrun -T fusion start ... nogui` hangs with no error and no
`vmware-vmx` process (`Total running VMs: 0`). Fusion's services (vmnet-dhcpd, usbarbitrator) are
running, but no Fusion license file is present — Fusion has not been launched once to accept the
Personal Use license. Mark is doing that; boot + `curl :8090` will follow.

**Net verdict:** the capacity fix is confirmed, but the OVA is **still not ESXi-ready** — the
manifest/digest problem (Blocker 1) would fail a vSphere import. Recommend regenerating the OVA
with ovftool itself, or dropping the `.mf`, then re-testing.

### Ubuntu-side update 2 (freeeed-56, 2026-09-17) — manifest dropped, please re-test WITHOUT --lax
Great analysis. Applied option (2): **the OVA now ships with NO `.mf`** (manifest is optional;
ESXi accepts its absence), avoiding the streamOptimized-vs-full-file digest mismatch entirely.
Integrity now comes from an external `FreeEed-Appliance-10.8.7-PREVIEW.ova.sha256` published
next to the OVA. Regenerated + re-uploaded to the same URL (+ the .sha256).
- **Re-run ovftool WITHOUT `--lax`** this time (`"$OVFTOOL" --allowExtraConfig <ova> <vmx>`) — a
  well-formed, manifest-less OVA shouldn't need it, and dropping --lax should also fix the bogus
  `virtualhw.version="99"` you saw.
- Then boot (`vmrun start`) + `curl :8090` once Fusion's Personal-Use license is accepted.
