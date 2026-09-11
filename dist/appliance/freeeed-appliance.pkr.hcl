# FreeEed Server Appliance -- Packer (QEMU/KVM) template. Headless Ubuntu Server,
# browser-accessed FreeEed stack. SCAFFOLD 2026-09-11: shape correct; TODOs before a real build.

packer {
  required_plugins {
    qemu = { source = "github.com/hashicorp/qemu", version = "~> 1" }
  }
}

variable "freeeed_pack_url" {
  type    = string
  # Pin a GA pack for release appliances; -daily- only for internal test builds.
  default = "https://shmsoft.s3.amazonaws.com/releases/freeeed_complete_pack-daily.zip"
}

variable "ubuntu_iso_url" {
  type    = string
  default = "https://releases.ubuntu.com/24.04/ubuntu-24.04.1-live-server-amd64.iso" # TODO pin + checksum
}
variable "ubuntu_iso_checksum" {
  type    = string
  default = "sha256:TODO"
}

source "qemu" "appliance" {
  iso_url      = var.ubuntu_iso_url
  iso_checksum = var.ubuntu_iso_checksum

  accelerator  = "kvm"
  cpus         = 4
  memory       = 8192
  # OS+app disk; the ~500 GB data volume is a SEPARATE disk the admin attaches at deploy.
  disk_size    = "40G"
  format       = "qcow2"
  headless     = true

  http_directory   = "http"     # Ubuntu Server autoinstall (subiquity) user-data + meta-data. TODO author.
  boot_wait        = "5s"
  boot_command     = [
    "c<wait>",
    "linux /casper/vmlinuz autoinstall ds='nocloud-net;s=http://{{.HTTPIP}}:{{.HTTPPort}}/' ---<enter><wait>",
    "initrd /casper/initrd<enter><wait>",
    "boot<enter>"
  ]

  ssh_username     = "freeeed"
  ssh_password     = "freeeed"   # build-only throwaway; disable/rotate in autoinstall for the shipped image
  ssh_timeout      = "40m"
  shutdown_command = "echo 'freeeed' | sudo -S shutdown -P now"

  output_directory = "output"
  vm_name          = "freeeed-appliance.qcow2"
}

build {
  sources = ["source.qemu.appliance"]

  # Stage the systemd unit + start/stop scripts into /tmp for the provisioner to install.
  provisioner "file" {
    source      = "systemd/freeeed.service"
    destination = "/tmp/freeeed.service"
  }
  provisioner "file" {
    source      = "scripts/appliance-start.sh"
    destination = "/tmp/appliance-start.sh"
  }
  provisioner "file" {
    source      = "scripts/appliance-stop.sh"
    destination = "/tmp/appliance-stop.sh"
  }

  provisioner "shell" {
    execute_command  = "echo 'freeeed' | {{.Vars}} sudo -S -E bash '{{.Path}}'"
    environment_vars = ["FREEEED_PACK_URL=${var.freeeed_pack_url}"]
    script           = "scripts/provision-appliance.sh"
  }

  # TODO post-process to OVA for VMware:
  #   qemu-img convert -O vmdk output/freeeed-appliance.qcow2 output/freeeed-appliance.vmdk
  #   generate OVF descriptor + tar -> FreeEed-Appliance-<ver>.ova  (scripts/to-ova.sh)
  # Proxmox imports the OVF, or the qcow2 disk directly (qm importdisk).
}
