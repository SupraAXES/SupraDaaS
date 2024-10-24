#!/bin/bash -e

#	-e VM_GUEST_RESTRICT=1 \
#	-e VM_GUEST_TCP_FWDS='("192.168.199.1:8080:192.168.119.135:8080")' \
sudo docker run --name vm-base-dev \
	-d \
	--init \
	-e LOG_LEVEL='debug' \
	-e SUPRA_APPS=windows \
	-e VM_OS_SUB_VER=windows-10 \
	-e VM_STORAGE_ID="win10test" \
	-e VM_CPU_CORES=4 \
	-e VM_RAM=8G \
	-e VM_GUEST_LAN=192.168.199.0/24 \
	-e VM_MAC=00:00:00:01:02:03 \
	-e VM_TCP_PORTS='("0.0.0.0:3389:3389" "127.0.0.1:5985:5985")' \
	-e VM_GUEST_VNC=1 \
	-p 3389:3389 \
	-p 5999:5999 \
	-v /opt/supra.daas_vm/storage/machine/win10test:/opt/vm/windows/win10test \
	--device /dev/kvm:/dev/kvm \
	supraaxes/projector-supra-vm
