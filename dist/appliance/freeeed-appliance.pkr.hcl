# FreeEed Server Appliance -- Packer (QEMU/KVM), built from Ubuntu's official cloud image.
# Headless Ubuntu Server, browser-accessed FreeEed stack. Cloud-image base + cloud-init seed
# (no ISO/autoinstall) -- fast and reliable. Output: a standalone qcow2 -> convert to OVA (ovftool).

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

variable "cloud_image_url" {
  type    = string
  default = "https://cloud-images.ubuntu.com/releases/24.04/release/ubuntu-24.04-server-cloudimg-amd64.img"
}
variable "cloud_image_checksum" {
  type    = string
  default = "sha256:612b2c0cc1bc413a6cb8c38fd611794caf0f2b436c50013d8b3794db12ad7354"
}

source "qemu" "appliance" {
  # Boot the cloud image directly (it's a disk, not an install ISO).
  iso_url      = var.cloud_image_url
  iso_checksum = var.cloud_image_checksum
  disk_image   = true
  disk_size    = "40G"          # root; cloud-init growpart expands it. 500 GB data disk is added at deploy.
  format       = "qcow2"

  accelerator  = "kvm"
  cpus         = 4
  memory       = 8192
  headless     = true
  net_device   = "virtio-net"
  disk_interface = "virtio"

  # cloud-init NoCloud seed: a CD labeled 'cidata' with user-data + meta-data creates the
  # build user + enables SSH so Packer can connect. (Build-only creds; hardened in provisioning.)
  cd_label   = "cidata"
  cd_content = {
    "meta-data" = "instance-id: freeeed-appliance\nlocal-hostname: freeeed\n"
    "user-data" = <<-EOF
      #cloud-config
      ssh_pwauth: true
      users:
        - name: freeeed
          plain_text_passwd: freeeed
          lock_passwd: false
          sudo: "ALL=(ALL) NOPASSWD:ALL"
          groups: [sudo]
          shell: /bin/bash
      EOF
  }

  ssh_username     = "freeeed"
  ssh_password     = "freeeed"
  ssh_timeout      = "20m"
  shutdown_command = "sudo shutdown -P now"

  output_directory = "output"
  vm_name          = "freeeed-appliance.qcow2"
}

build {
  sources = ["source.qemu.appliance"]

  # Stage the systemd unit + start/stop scripts for the provisioner to install.
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
    execute_command  = "{{.Vars}} sudo -E bash '{{.Path}}'"
    environment_vars = ["FREEEED_PACK_URL=${var.freeeed_pack_url}"]
    script           = "scripts/provision-appliance.sh"
  }

  # TODO step 5: qemu-img convert -O vmdk -o subformat=streamOptimized output/freeeed-appliance.qcow2 ... ;
  #   build an OVF descriptor + package with ovftool -> FreeEed-Appliance-<ver>.ova (ESXi-importable).
}
