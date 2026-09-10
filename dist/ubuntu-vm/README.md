# FreeEed downloadable Ubuntu VM (Packer)

A preconfigured **Ubuntu Desktop OVA** with FreeEed fully installed — "download and run,"
so users who hit native-install friction (locked-down Windows, no JRE, blocked ports)
get an environment where everything already works. Keeps data on the user's own hardware,
so the local-first / "nothing leaves the machine" story is preserved.

**Status:** scaffold (2026-09-10). Not yet built/tested — completed once KVM is live on the
build box (see below). Build is reproducible via Packer — do NOT hand-snapshot a VM.

## Build host requirements (the Ubuntu box, once KVM is enabled)
```
sudo apt install -y qemu-kvm libvirt-daemon-system virt-manager cpu-checker packer
sudo usermod -aG kvm,libvirt $USER   # then log out/in
kvm-ok                               # must say KVM acceleration can be used
```
No VirtualBox needed on the build host (we build with QEMU/KVM, then package to OVA).

## Build
```
cd dist/ubuntu-vm
packer init .
packer build freeeed-ubuntu.pkr.hcl        # produces output/freeeed-ubuntu.qcow2
./scripts/to-ova.sh                          # qcow2 -> vmdk -> OVF -> FreeEed-<ver>.ova  (TODO)
```

## Test (as an end user)
Import `FreeEed-<ver>.ova` into **VirtualBox** on a Windows or Mac host (the target audience) —
this is the real acceptance test. The **AWS Windows VM** is a good place to do it.
On boot, FreeEed services autostart; a sample case is preloaded; a desktop shortcut opens the
Control Panel.

## What the image contains (see scripts/provision.sh)
- Ubuntu Desktop LTS + a JRE (so the pack's find_java resolves — no "install Java" wall)
- LibreOffice (imaging / mbox→PDF), readpst (PST), tesseract (OCR)
- The FreeEed complete pack (pinned version), autostart on login, sample data
- Free registration (#549) still applies inside the VM

## Open items before first release
- OVA post-processing (`to-ova.sh`): qcow2 → vmdk → OVF descriptor → tar to `.ova`.
- Ubuntu autoinstall `http/user-data` (subiquity) for the headless install.
- Pin which pack version the image ships (a GA build, not `-daily-`).
- Size/compression + S3 hosting (like the installers); in-VM "Update FreeEed" to avoid rot.
- Hosting/torrent decision for the multi-GB download.
