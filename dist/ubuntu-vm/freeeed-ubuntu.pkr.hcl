# FreeEed downloadable Ubuntu VM -- Packer template (QEMU/KVM builder).
# SCAFFOLD (2026-09-10): shape is correct; values marked TODO need finishing before a
# real build. Build on the Ubuntu box once KVM is enabled (see README.md).

packer {
  required_plugins {
    qemu = {
      source  = "github.com/hashicorp/qemu"
      version = "~> 1"
    }
  }
}

variable "freeeed_pack_url" {
  type    = string
  # Pin a GA pack for release images; -daily- only for internal test builds.
  default = "https://shmsoft.s3.amazonaws.com/releases/freeeed_complete_pack-daily.zip"
}

variable "ubuntu_iso_url" {
  type    = string
  default = "https://releases.ubuntu.com/24.04/ubuntu-24.04-desktop-amd64.iso" # TODO pin + checksum
}

variable "ubuntu_iso_checksum" {
  type    = string
  default = "sha256:TODO"
}

source "qemu" "freeeed" {
  iso_url      = var.ubuntu_iso_url
  iso_checksum = var.ubuntu_iso_checksum

  accelerator  = "kvm"
  cpus         = 4
  memory       = 8192
  disk_size    = "40G"
  format       = "qcow2"
  headless     = true

  # Ubuntu 24.04 autoinstall (subiquity) -- serves http/user-data + meta-data. TODO: author these.
  http_directory = "http"
  boot_wait      = "5s"
  boot_command = [
    "c<wait>",
    "linux /casper/vmlinuz autoinstall ds='nocloud-net;s=http://{{.HTTPIP}}:{{.HTTPPort}}/' ---<enter><wait>",
    "initrd /casper/initrd<enter><wait>",
    "boot<enter>"
  ]

  # SSH created by the autoinstall user-data (TODO: match credentials there).
  ssh_username     = "freeeed"
  ssh_password     = "freeeed"          # TODO: build-only throwaway; not shipped
  ssh_timeout      = "40m"
  shutdown_command = "echo 'freeeed' | sudo -S shutdown -P now"

  output_directory = "output"
  vm_name          = "freeeed-ubuntu.qcow2"
}

build {
  sources = ["source.qemu.freeeed"]

  provisioner "shell" {
    execute_command = "echo 'freeeed' | {{.Vars}} sudo -S -E bash '{{.Path}}'"
    environment_vars = [
      "FREEEED_PACK_URL=${var.freeeed_pack_url}"
    ]
    script = "scripts/provision.sh"
  }

  # TODO post-processing to OVA (VirtualBox/VMware importable):
  # qemu-img convert -O vmdk output/freeeed-ubuntu.qcow2 output/freeeed-ubuntu.vmdk
  # then generate an OVF descriptor + `tar` into FreeEed-<ver>.ova (see scripts/to-ova.sh).
}
