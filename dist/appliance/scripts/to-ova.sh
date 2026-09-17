#!/bin/bash
# Package the built appliance qcow2 into an ESXi/vCenter-importable OVA -- Option A:
# qemu-img (streamOptimized VMDK) + a hand-rolled OVF + manifest + tar. No ovftool needed.
# Run from dist/appliance after `packer build`. ESXi import at the customer is the acceptance test.
set -euo pipefail
cd "$(dirname "$0")/.."

QCOW="output/freeeed-appliance.qcow2"
[ -f "$QCOW" ] || { echo "ERROR: $QCOW not found -- run packer build first" >&2; exit 1; }
VERSION="$(sed -n 's/.*String V = "\([^"]*\)".*/\1/p' ../../freeeed-processing/src/main/java/org/freeeed/main/Version.java 2>/dev/null || echo 10.8.7-PREVIEW)"
NAME="FreeEed-Appliance-${VERSION}"
OUT="output/${NAME}"
mkdir -p "$OUT"
VMDK="${OUT}/${NAME}-disk1.vmdk"
OVF="${OUT}/${NAME}.ovf"
MF="${OUT}/${NAME}.mf"
OVA="output/${NAME}.ova"

echo "=== 1. qcow2 -> stream-optimized VMDK (required for OVA/ESXi) ==="
# adapter_type=lsilogic so the VMDK descriptor matches the OVF's lsilogic SCSI controller.
qemu-img convert -p -O vmdk -o subformat=streamOptimized,adapter_type=lsilogic "$QCOW" "$VMDK"

# Read ONLY the top-level virtual-size. (A plain grep also catches the nested children->file
# node's virtual-size on newer qemu-img, which put two numbers in ovf:capacity and broke the
# VMware/ESXi import -- caught by the Mac Fusion test, 2026-09-16.)
CAP=$(qemu-img info --output=json "$QCOW" | python3 -c 'import json,sys; print(json.load(sys.stdin)["virtual-size"])')
[[ "$CAP" =~ ^[0-9]+$ ]] || { echo "ERROR: bad capacity from qemu-img: [$CAP]" >&2; exit 1; }
FSIZE=$(stat -c%s "$VMDK")
echo "  capacity=$CAP bytes, vmdk file=$FSIZE bytes"

echo "=== 2. OVF descriptor (4 vCPU / 8 GB / SCSI disk / E1000 NIC; vmx-13 broad compat) ==="
cat > "$OVF" <<OVFEOF
<?xml version="1.0" encoding="UTF-8"?>
<Envelope xmlns="http://schemas.dmtf.org/ovf/envelope/1"
  xmlns:ovf="http://schemas.dmtf.org/ovf/envelope/1"
  xmlns:rasd="http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_ResourceAllocationSettingData"
  xmlns:vssd="http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_VirtualSystemSettingData"
  xmlns:vmw="http://www.vmware.com/schema/ovf">
  <References>
    <File ovf:href="${NAME}-disk1.vmdk" ovf:id="file1" ovf:size="${FSIZE}"/>
  </References>
  <DiskSection>
    <Info>Virtual disk information</Info>
    <Disk ovf:capacity="${CAP}" ovf:capacityAllocationUnits="byte" ovf:diskId="vmdisk1"
      ovf:fileRef="file1" ovf:format="http://www.vmware.com/interfaces/specifications/vmdk.html#streamOptimized"/>
  </DiskSection>
  <NetworkSection>
    <Info>The list of logical networks</Info>
    <Network ovf:name="VM Network"><Description>The VM Network</Description></Network>
  </NetworkSection>
  <VirtualSystem ovf:id="${NAME}">
    <Info>FreeEed Server Appliance (browser-accessed eDiscovery/FOIA)</Info>
    <Name>${NAME}</Name>
    <OperatingSystemSection ovf:id="94" ovf:version="24.04" vmw:osType="ubuntu64Guest">
      <Info>Ubuntu Linux (64-bit)</Info>
    </OperatingSystemSection>
    <VirtualHardwareSection>
      <Info>Virtual hardware requirements</Info>
      <System>
        <vssd:ElementName>Virtual Hardware Family</vssd:ElementName>
        <vssd:InstanceID>0</vssd:InstanceID>
        <vssd:VirtualSystemType>vmx-13</vssd:VirtualSystemType>
      </System>
      <Item>
        <rasd:AllocationUnits>hertz * 10^6</rasd:AllocationUnits>
        <rasd:Description>Number of Virtual CPUs</rasd:Description>
        <rasd:ElementName>4 virtual CPU(s)</rasd:ElementName>
        <rasd:InstanceID>1</rasd:InstanceID>
        <rasd:ResourceType>3</rasd:ResourceType>
        <rasd:VirtualQuantity>4</rasd:VirtualQuantity>
      </Item>
      <Item>
        <rasd:AllocationUnits>byte * 2^20</rasd:AllocationUnits>
        <rasd:Description>Memory Size</rasd:Description>
        <rasd:ElementName>8192 MB of memory</rasd:ElementName>
        <rasd:InstanceID>2</rasd:InstanceID>
        <rasd:ResourceType>4</rasd:ResourceType>
        <rasd:VirtualQuantity>8192</rasd:VirtualQuantity>
      </Item>
      <Item>
        <rasd:Address>0</rasd:Address>
        <rasd:ElementName>SCSI Controller</rasd:ElementName>
        <rasd:InstanceID>3</rasd:InstanceID>
        <rasd:ResourceSubType>lsilogic</rasd:ResourceSubType>
        <rasd:ResourceType>6</rasd:ResourceType>
      </Item>
      <Item>
        <rasd:AddressOnParent>0</rasd:AddressOnParent>
        <rasd:ElementName>Hard Disk 1</rasd:ElementName>
        <rasd:HostResource>ovf:/disk/vmdisk1</rasd:HostResource>
        <rasd:InstanceID>4</rasd:InstanceID>
        <rasd:Parent>3</rasd:Parent>
        <rasd:ResourceType>17</rasd:ResourceType>
      </Item>
      <Item>
        <rasd:AddressOnParent>7</rasd:AddressOnParent>
        <rasd:AutomaticAllocation>true</rasd:AutomaticAllocation>
        <rasd:Connection>VM Network</rasd:Connection>
        <rasd:ElementName>Network adapter 1</rasd:ElementName>
        <rasd:InstanceID>5</rasd:InstanceID>
        <rasd:ResourceSubType>E1000</rasd:ResourceSubType>
        <rasd:ResourceType>10</rasd:ResourceType>
      </Item>
    </VirtualHardwareSection>
  </VirtualSystem>
</Envelope>
OVFEOF

echo "=== 3. manifest (SHA256) ==="
( cd "$OUT" && sha256sum "$(basename "$OVF")" "$(basename "$VMDK")" \
   | sed -E 's/^([0-9a-f]+)  (.*)$/SHA256(\2)= \1/' > "$(basename "$MF")" )

echo "=== 4. tar into OVA (ovf first, then vmdk, then mf) ==="
( cd "$OUT" && tar -cf "../$(basename "$OVA")" "$(basename "$OVF")" "$(basename "$VMDK")" "$(basename "$MF")" )

echo "=== done: $OVA ($(du -h "$OVA" | cut -f1)) ==="
ls -la "$OVA"
