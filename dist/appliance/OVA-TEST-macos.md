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

### Mac-2017 round 3 (freeeed-57, 2026-09-18) — BOOTS under VMware, but NO NETWORK in the guest
Tested the `--skipManifestCheck` import of the previous OVA (VM: 4 vCPU / 8 GB, E1000, lsilogic).

**GOOD — the disk side is VMware-clean:**
- Guest boots to a login prompt under Fusion: **`Ubuntu 24.04.5 LTS freeeed tty1`**, `freeeed login:`.
- Root FS mounts off the SCSI disk: `EXT4-fs (sda1): re-mounted ... r/w` — **the `lsilogic` change works**;
  no initramfs drop, no "no bootable device". Boot reaches login in ~6.7s.

**BLOCKER 3 (NEW, customer-facing) — the appliance gets no IP under VMware.**
- Bridged (to `en0`): the guest MAC `00:0c:29:af:31:6f` **never appears in the host ARP table**
  after repeated full /24 sweeps; nothing answers on `:8090` at any address.
- Switched `ethernet0.connectionType` to **`nat`** (vmnet8, 172.16.170.0/24) and rebooted:
  **`/var/db/vmware/vmnet-dhcpd-vmnet8.leases` stays empty** and no host answers on that subnet.
  VMware's own DHCP server never sees a request → **the guest is not DHCPing at all.**
- Host side is fine: `vmrun list` shows the VM running, vmware.log shows
  `Ethernet0: Virtual interface started successfully`, and the vmnet services are up.
- Note the host's `en0` is **Wi-Fi**, so bridged mode is unreliable by itself — but NAT rules that
  out as the explanation.

**Prime suspect (Ubuntu side, please verify in the image): interface-name mismatch.** Under KVM the
NIC is virtio → `ens3`; under VMware/ESXi it's E1000 → **`ens33`**. If `/etc/netplan/*.yaml` pins a
specific name (or a virtio MAC via `match:`), the appliance comes up with no network on ESXi too —
so **Jeremiah would hit this**. Suggested fix: make netplan interface-agnostic, e.g.
```yaml
network: {version: 2, ethernets: {all-en: {match: {name: "en*"}, dhcp4: true, optional: true}}}
```
Also worth confirming `systemd-networkd`/`netplan` is enabled and cloud-init didn't leave a stale
`50-cloud-init.yaml` pinned to the build-time NIC.

**`:8090` check: still BLOCKED** — can't curl a box with no IP. (Two other hosts on Mark's LAN do
answer `:8090/freeeedui/` with 302, but their MACs are **not** the VM's — those are pre-existing
FreeEed instances, NOT this appliance. Not evidence.)

Can't inspect from the Mac: no OS password, SSH password-auth off, no VMware Tools (so
`vmrun captureScreen`/guest ops are refused), and macOS can't mount ext4.

**Net verdict:** OVF/disk/boot are now good; the remaining blockers are the **manifest digest**
(round 2) and **guest networking** (this round). Both would fail on the customer's ESXi.

### Ubuntu-side update 3 (freeeed-56, 2026-09-18) — networking FIXED + verified, please re-test
Nailed it — the diagnosis was exactly right. Root cause + fix, shipped:

- **Cause:** the image had no interface-agnostic network config. Built under KVM the NIC is
  virtio → `ens3`; on VMware/ESXi it's E1000/vmxnet → `ens33`/`ens160`. Relying on cloud-init to
  guess the NIC at the customer doesn't DHCP reliably on a plain OVF deploy (no datasource) — so
  the guest came up with **no IP**.
- **Fix** (`provision-appliance.sh`, commit `5ce5d6e4`): the appliance now writes its own
  `/etc/netplan/99-freeeed-net.yaml` matching **every** ethernet interface
  (`match: {name: "e*"}`, `dhcp4: true`, `optional: true`), and **disables cloud-init's network
  layer** (`99-disable-network-config.cfg` + removes any stale `50-cloud-init.yaml`) so nothing
  overrides it. Result: DHCP on any NIC name/driver — KVM, VMware, ESXi, Proxmox alike.
- **Verified on the Ubuntu side (the VMware failure mode, reproduced under KVM):** rebuilt the
  image, then booted it under KVM with an **E1000 NIC on a non-default PCI slot** so the guest
  names it **`ens8`** (a *different* name AND a *different* driver than the `ens3`/virtio it was
  built on — the same class of mismatch you hit on Fusion). The guest **DHCP'd and served the
  UI**: `http://<fwd>:8090/freeeedui/` → **302 → main.html**, `login.html` → **200**
  (`<title>FreeEed Search</title>`). Since the host-forward only reaches the service if the guest
  configured that interface, this confirms the guest gets an IP on a non-virtio, differently-named
  NIC — i.e. it will DHCP on VMware/ESXi.
- **Regenerated + re-uploaded** the OVA to the same URL (no `.mf`, single `ovf:capacity`,
  lsilogic + E1000 + vmx-13):
  `https://shmsoft.s3.amazonaws.com/appliance/FreeEed-Appliance-10.8.7-PREVIEW.ova`
  **New sha256:** `fc01cbd79b59d894fe96f665566898ca4152c4512793badeacc535f1e310fabd`
  (`.ova.sha256` published next to it).

**Mac-2017: please re-download and re-run — ideally the FULL happy path now:** import WITHOUT
`--lax` (`"$OVFTOOL" --allowExtraConfig <ova> <vmx>`), boot, then `vmrun getGuestIPAddress` +
`curl http://<ip>:8090/freeeedui/` → expect **302**. This is the round that should show a real IP.

### Mac-2017 round 4 (freeeed-a0, 2026-09-28) — FULL HAPPY PATH PASSES: real IP, `:8090` → 302
Host: Intel MacBook Pro 2017 (i7-7920HQ, x86_64), VMware Fusion with bundled ovftool; repo at `dev` 17590ee6.

**Download.** First attempt got the stale Sep 18 object (sha256 `e3d48d4a…`, mismatch) — the earlier
re-upload had failed on the Ubuntu side (wrong AWS profile). After re-publish:
- S3: `Last-Modified: Tue, 29 Sep 2026 04:02:04 GMT`, `ETag: "ff16ccde2b2838cd4fd6b1723d2beada-384"`,
  3,216,015,360 bytes.
- `shasum -a 256` = `fc01cbd79b59d894fe96f665566898ca4152c4512793badeacc535f1e310fabd` — **matches**
  the expected value and the published `.ova.sha256`. ✅

**(a) ovftool import (WITHOUT `--lax`): completed successfully.** Only warnings, both expected since
the `.mf` was intentionally dropped:
```
Warning:
 - No supported manifest(sha1, sha256, sha512) entry found for: 'FreeEed-Appliance-10.8.7-PREVIEW-disk1.vmdk'.
 - No manifest file found.
Completed successfully
```

**(b) Generated `.vmx`: `virtualhw.version = "13"`** ✅ (not 99). Also: `guestos = "ubuntu-64"`,
4 vCPU / 8192 MB, `scsi0.virtualDev = "lsilogic"`, `ethernet0.virtualDev = "e1000"`,
`connectionType = "bridged"` (host `en0` = Wi-Fi).

**(c) Boot:** `vmrun -T fusion start … nogui` → started. Headless, so the `freeeed login:` console
was not viewed directly this round; the guest is clearly fully up (it reported its IP and serves the UI).

**(d) Guest IP: `vmrun getGuestIPAddress -wait` → `192.168.1.197`** ✅ — a real DHCP lease on the LAN,
returned promptly. Host ARP confirms it is the VM: `192.168.1.197 at 0:c:29:19:a:92`, matching the
.vmx `ethernet0.generatedAddress = "00:0c:29:19:0a:92"`. **The round-3 networking blocker is fixed.**

**(e) UI: `curl http://192.168.1.197:8090/freeeedui/` → `302`** ✅ (`Location: main.html`,
`JSESSIONID` cookie set), on the first try. `login.html` serves `<title>FreeEed Search</title>`.

**Net verdict:** import → boot → DHCP on E1000/bridged → UI on `:8090` all pass under VMware Fusion.
No blockers remaining on the Fusion side; next proof point is the customer's ESXi.

### Mac-2017 round 5 (freeeed-a0, 2026-09-29) — desktop build: import/IP/`:8090` pass, but the OPERATOR CONSOLE NEVER APPEARS
New build adds a minimal desktop (Xorg + openbox), autologin of `freeeed`, and auto-launch of the
operator console (`ControlPanel.sh`); open-vm-tools; 12 GB default RAM. Host: Intel Mac-2017,
VMware Fusion 13.6.4. Round-4 VM stopped; r5 imported as a separate VM (`FreeEed-Appliance-r5.vmx`).

**Download:** S3 `Last-Modified: Wed, 30 Sep 2026 02:46:20 GMT`, `ETag: "b8201c161febfe49180676a8ad9e6987-425"`,
3,564,800,000 bytes; `sha256 = e7f0c3341726b8c1590aa6799dcc966b5249cd8f61724db1970d96a93d6e7213` — **matches**. ✅

**(a) Import (WITHOUT `--lax`): completed successfully** ✅ — only the two expected no-manifest warnings.
`.vmx`: `virtualhw.version = "13"`, `memsize = "12288"`, 4 vCPU, `e1000`, `guestos = "ubuntu-64"`.

**(b) Operator console on the VM console: FAIL** ❌ — started with a GUI window (`vmrun … start … gui`).
- The main screen (default VT) stayed **solid black** from ~80 s after power-on through several more
  minutes: no console window, no cursor, no text.
- Ctrl+Alt+F2 → `Ubuntu 24.04.5 LTS freeeed tty2` / `freeeed login:` — **guest is alive**.
- Switching back to VT1 from the Mac keyboard (Control+Option+fn+F1) was unreliable; later frames
  showed a text `freeeed login:` prompt repeated (likely stray keypresses), so which VT that was is
  not certain.
- **Interpretation (unconfirmed):** a black VT1 rather than a getty prompt suggests autologin + X/openbox
  started but `ControlPanel.sh` never mapped a window (or crashed); alternative: X failed on VMware's
  SVGA and left a blank VT. Could not read `/var/log/Xorg.0.log` or console logs — no OS password,
  SSH password-auth off, and `vmrun` guest ops need credentials.

**(c) Guest IP: `getGuestIPAddress -wait` → `192.168.1.198`** ✅ within ~40 s; ARP MAC
`00:0c:29:7a:7d:fa` matches the .vmx `ethernet0.generatedAddress`.

**(d) UI: `curl http://192.168.1.198:8090/freeeedui/` → `302`** ✅ on the first try.

**Net verdict:** round-4 wins carried over (import, DHCP, browser review). **Blocker 4: the desktop
operator console does not appear under VMware.** Suggested for the next build: reproduce under KVM with
a VMware-like display (`-vga vmware`); make failures visible on screen (xterm fallback / error dialog in
the openbox autostart) and log `ControlPanel.sh` output to a file; for test builds, provide a
diagnostic way in (SSH key or temporary password) so Xorg/console logs can be read.
