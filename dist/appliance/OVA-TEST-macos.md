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

### Mac-2017 round 5b (freeeed-a0, 2026-09-29) — OPERATOR CONSOLE APPEARS ✅ (blocker 4 fixed)
Fix under test: `ControlPanel.sh` first-run EULA `read` failed with no tty under openbox autostart;
image now pre-accepts the EULA, seeds `.env`, logs the console, has an xterm fallback + VMware X drivers.

**Download:** S3 `Last-Modified: Wed, 30 Sep 2026 04:15:03 GMT`, `ETag: "4c8e8a74d383da40d2faa641c4866243-423"`,
3,545,968,640 bytes; `sha256 = 344d3d3f21971917903973f6a6926aef2724476ea8b2dccdfba28d406938f881` — **matches**. ✅

**(a) Import (WITHOUT `--lax`): completed successfully** ✅ — only the two expected no-manifest warnings;
`virtualhw.version = "13"`, `memsize = "12288"`, 4 vCPU, `e1000`. Imported as `FreeEed-Appliance-r5b.vmx`.

**(b) Operator console on the VM console: PASS** ✅ — booted with a GUI window. The Swing app renders on
VMware's display: the **FreeEed Player** window ("FreeEed™ - FreeEed sample project") with full menu bar
(File/Edit/Process/Review/Settings/Backup/Restore/Help), **Open Project / New Project**, status bar
"1 - FreeEed sample project | 3 inputs | Storage used: 446.9 KB". (Screenshot taken by Mark after he
interacted with the console; whether the Service Manager or the Player appears first at boot was not recorded.)

**(c) Guest IP: `getGuestIPAddress -wait` → `192.168.1.199`** ✅ in under a minute; ARP MAC
`00:0c:29:0d:09:ee` matches the .vmx.

**(d) UI: `curl http://192.168.1.199:8090/freeeedui/` → `302`** ✅ on the first try.

**Minor (not a blocker):** opening Review from the Player shows *"Can't open a browser - just go to
http://localhost:8090/freeeedui"*. Expected (no browser in the VM), but `localhost` is only correct inside
the VM; users reach review from their own machines at `http://<vm-ip>:8090/freeeedui`, so the message
should show the VM's LAN IP (or the appliance should ship a browser — product decision).

**Net verdict:** import → boot → DHCP → desktop operator console → browser review on `:8090` all pass
under VMware Fusion.

### Mac-2017 round 6 (freeeed-a0, 2026-09-30) — in-VM Firefox works, but the CONSOLE's Review can't launch it
New build adds Firefox ESR 140.17.0esr (mozillateam PPA, no snap), set as default browser, locked via
enterprise `policies.json` (telemetry / first-run / updates / captive-portal / safebrowsing off; homepage =
`localhost:8090/freeeedui`). Pack still the Sep-9 daily (`cf77b6f8`), so PR #606 and the localhost→LAN-IP
message fix are NOT in this build.

**Download:** S3 `Last-Modified: Thu, 01 Oct 2026 03:22:43 GMT`, `ETag: "21920df486c342e51e619da74dc1412c-458"`,
3,835,156,480 bytes; `sha256 = dcf060288ac3aba8cdb2740930efa743f5f07e0e31a972e54e09e6f00ffc4eb6` — **matches**. ✅

**Import (WITHOUT `--lax`): completed successfully** ✅ — only the two expected no-manifest warnings;
`virtualhw.version = "13"`, `memsize = "12288"`, 4 vCPU, `e1000`. Imported as `FreeEed-Appliance-r6.vmx`.

**(a) Review from the operator console: FAIL** ❌ — in the Player, Review shows *"Can't open a browser - just
go to http://localhost:8090/freeeedui"*. **Likely cause (from code, not confirmed in the guest):**
`UtilUI.openBrowser` calls `Desktop.browse` only if `hasBrowser()` is true, and on Linux `hasBrowser()` scans
`PATH` for fixed names (`firefox`, `chromium`, `google-chrome`, …). The PPA installs **`/usr/bin/firefox-esr`**,
which is not in the list, so it never tries — setting the default browser / `xdg-open` doesn't help.
Same check in `cf77b6f8` and on `dev`. **Image-only fix:** symlink `firefox` → `firefox-esr` (plus
`x-www-browser` alternative). **Code follow-up:** add `firefox-esr` to the `hasBrowser()` list.

**In-VM Firefox itself: PASS** ✅ — opened via the openbox root menu (right-click the black desktop →
Applications → Internet → Firefox Web Browser; also "Web browser" at the top). It went **straight to the
FreeEed review screen**.
**(b) No first-run page / default-browser nag / telemetry prompt** ✅ — none appeared (policy works).

**(c) Guest IP: `192.168.1.200`** ✅ (MAC `00:0c:29:7e:05:e3` matches the .vmx); **`:8090/freeeedui/` → `302`** ✅.

**No-egress capture:** not performed this round.

**UX / security notes:**
- Users won't know to right-click a black desktop; the console's Review / Open UI must be the path in.
- The root menu offers **Terminal emulator** → a shell as `freeeed`, which has NOPASSWD sudo — i.e. anyone at the
  VM console has root. Acceptable on the customer's own server (console = IT), but worth a deliberate decision
  for the shipped build.

**Net verdict:** browser + lockdown work under VMware Fusion; **one blocker left — the console can't launch
the browser (`firefox-esr` name)**.

### Mac-2017 round 7 (freeeed-a0, 2026-10-01) — PARTIAL: console Review still can't launch the browser; full workflow NOT yet run
Build changes: Firefox registered as system default for http/https (`mimeapps.list`) + `/usr/bin/firefox`
symlink; Player autostarts directly (no Service Manager → no "Start All"); openbox root menu reduced to
"Open FreeEed Review" + "Operator Console (Player)" (no terminal); no-egress sample data at
`/opt/freeeed/sample-data` (4 files) + `~/Getting-Started.txt`.

**Download:** S3 `Last-Modified: Thu, 01 Oct 2026 04:54:50 GMT`, `ETag: "1bbd49c6347284cb953fb6a644eb8b8a-460"`,
3,852,400,640 bytes; `sha256 = d3c5cfc8c7a801cdbeb404e06141eff42079fab6681c438959ef153269108c6c` — **matches**. ✅
**Import (WITHOUT `--lax`):** completed successfully ✅ (two expected no-manifest warnings); vmx-13, 12 GB, 4 vCPU, e1000.
**Guest IP:** `192.168.1.201` ✅ (MAC `00:0c:29:17:1d:7b` matches the .vmx). **`:8090/freeeedui/` → `302`** ✅.

| Step | Result |
|---|---|
| 1. First-run / Player | ✅ Player window up ("FreeEed sample project \| 3 inputs \| 41.8 MB"). |
| 2. Create case on `/opt/freeeed/sample-data` + process | ⏸ **Not run** — the Player still shows the pre-existing sample project. |
| 3. Review from the Player | ❌ **FAIL (again)** — *"Can't open a browser - just go to http://localhost:8090/freeeedui"*. Firefox reached only via the desktop right-click menu. |
| 3b. In-VM Firefox + login | ✅ `localhost:8090/freeeedui/search.html`, logged in, **case_1 = 2460 documents**, tag/export controls present, no first-run prompts. |
| 4. Search "PRIVILEGE" = 2 hits | ⏸ Not run (needs the new 4-doc case). |
| 5. Tag persists | ⏸ Not run. |
| 6. Export | ⏸ Not run. |
| 7. From the Mac browser | ⏸ Not run beyond `:8090` → 302. |
| 8. No terminal in the root menu | ⏸ Not confirmed. |

**Review-button root cause (likely, unconfirmed in the guest):** the default-browser registration and the
`firefox` symlink act *after* Java decides whether it can browse. On X11, OpenJDK's `XDesktopPeer` only
enables its GTK-based BROWSE support when `sun.desktop` is `gnome` (derived from `XDG_CURRENT_DESKTOP` /
`GNOME_DESKTOP_SESSION_ID`). Under bare openbox neither is set → `Desktop.isSupported(BROWSE)` is false →
`UtilUI.openBrowser` shows the dialog. **Image-only fix to try:** export `XDG_CURRENT_DESKTOP=GNOME` in the
openbox autostart/environment before launching the Player. **Code fallback (later pack):** on Linux, try
`xdg-open <url>` when BROWSE is unsupported (as `FreeEedUI.openBrowserToBackup` already does).
**Process note:** this click has now failed on Fusion twice after passing build-side checks — the next
build must be verified by actually clicking Player → Review.

**Net verdict:** infrastructure + in-VM browser + review data path are solid; **blocker: console → browser
launch**. The full create-case → process → search → tag → export workflow remains **untested** and is the
gating test for a public OVA release.

### Mac-2017 round 8 (freeeed-a0, 2026-10-04) — FULL WORKFLOW PASSES ✅ (release candidate)
Build: pack **10.8.7-PREVIEW** / FreeEed `f35655cd` (PR #606 mail fallback, #608 direct browser launch,
#609 handler gate + no EDT freeze), FreeEedUI `aaca5fb`. Ubuntu's pre-publish check ran the real
`UtilUI.openBrowser` in the openbox session: `handler=[]` → skip xdg-open → direct launch → Firefox opened.

**Download:** S3 `Last-Modified: Sun, 04 Oct 2026 15:31:24 GMT`, `ETag: "81ae4ff006cc1590849bb12464e718a8-466"`,
3,900,733,440 bytes; `sha256 = af4f713b025ce86d775c3f8c8b8b114a7463d729f06de2b151b4d27b2d1fd64d` — **matches**. ✅
**Import (WITHOUT `--lax`):** completed successfully ✅ (two expected no-manifest warnings); vmx-13, 12 GB, 4 vCPU, e1000.
**Guest IP:** `192.168.1.202` ✅ (MAC `00:0c:29:06:25:5d` matches the .vmx). **`:8090/freeeedui/` → `302`** ✅.

| Step | Result |
|---|---|
| 1. First run / Edition chooser / registration | ✅ (Mark: "everything works") |
| 2. New case on `/opt/freeeed/sample-data` + process | ✅ |
| 3. **Player → Review opens in-VM Firefox** | ✅ — **blocker from rounds 6–7 fixed** |
| 4. Search "PRIVILEGE" | ✅ |
| 5. Tag | ✅ |
| 6. Export | ✅ behaves correctly: PDF export without PDF renditions shows *"No PDF renditions exist … Enable 'Create PDF images' and reprocess"* (Player: Edit → Project options → Imaging) |
| 7. Mac browser at `:8090` | partial — HTTP 302 + version checked from the Mac; case/search not checked from the Mac |
| 8. No terminal in root menu | not separately confirmed |
| 9. Version | ✅ review top bar `v10.8.7-PREVIEW · gaaca5fb` |

Results for steps 1–6 as reported by Mark after running them in the VM console.

**Minor (fix before GA):** the login page footer still reads *"FreeEed™ Review V10.8.4-SNAPSHOT"* (stale
hard-coded label in FreeEedUI), while the top bar correctly shows 10.8.7-PREVIEW.

**Net verdict:** import → boot → DHCP → operator console → create case → process → Review button opens
in-VM Firefox → search → tag → export path all work under VMware Fusion. **Release candidate.**
Next (Mark, 2026-10-04): release as **10.8.7** (GA label); Windows installer ships **unsigned** for now.
