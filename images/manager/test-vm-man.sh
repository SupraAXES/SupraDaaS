#!/bin/bash

sudo docker network create vm-test

sudo docker run --name vm-man \
	--network vm-test \
	--rm \
	--init \
	-it \
	-e LOG_LEVEL='debug' \
	-e SUPRA_HOST_BASE='/vm/supra-host1' \
	-e VM_STORAGE_BASE='/vm/storage1' \
	-e SUPRA_TZ_OFFSET='8' \
	-e VM_ADMIN_HOSTNAME='device-admin' \
	-e SUPRA_PROJECTOR_IMAGE='SupraRegistry:5000/supra_projector_vm_base' \
	-v /vm/tmp:/supra/conf/app \
	-v /vm/supra-host1:/supra-host \
	-v /vm/storage1:/vm-storage \
	-v /vm/backup1:/vm-backup \
	-v /var/run/docker.sock:/var/run/docker.sock \
	vm_manager
