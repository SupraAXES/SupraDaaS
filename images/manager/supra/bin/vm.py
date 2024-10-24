#!/usr/bin/env python3
import os
import logging
import docker
from docker.types import LogConfig

_proj_image = os.environ.get('SUPRA_PROJECTOR_IMAGE')

assert _proj_image is not None, 'please set SUPRA_PROJECTOR_IMAGE'

logging.info(f'supra_projector_image: {_proj_image}')


def stop(serv_name):
    client = docker.from_env()

    try:
        container = client.containers.get(serv_name)
        container.stop()
    finally:
        client.close()


def _parse_os_type(os_type):
    if os_type in ["ubuntu-2204"]:
        return "linux", os_type
    elif os_type in ["windows-7", "windows-10", "windows-xp", "windows-11", "windows-2003"]:
        return "windows", os_type
    else:
        raise Exception(f'{os_type} currently not support')


def _check_vm_opts(os_class, os_sub_ver, cores, ram):
    cores_num = int(cores)
    ram_num = int(ram[:-1])
    if os_class == 'windows':
        if cores_num < 2:
            raise Exception(f'{os_sub_ver} need cores >= 2')
        if ram_num < 4:
            raise Exception(f'{os_sub_ver} need ram >= 4G')
    elif os_class == 'linux':
        if cores_num < 1:
            raise Exception(f'{os_sub_ver} need cores >= 1')
        if ram_num < 1:
            raise Exception(f'{os_sub_ver} need ram >= 1G')


def run(serv_name, serv_token,
        container_network,
        os_type, storage_id, storage_dir, vm_log,
        *,
        guest_vnc=False, guest_restrict=False, guest_lan='', guest_tcp_fwds='', tcp_ports='', udp_ports='', mac='', cores='2', ram='4G',
        hoppers='', keep_report_quitting=True):
    client = docker.from_env()

    os_class, os_sub_ver = _parse_os_type(os_type)

    vm_mnts = []
    vm_caps = []
    vm_envs = dict()

    vm_mnts.append('/etc/localtime:/etc/localtime:ro')
    vm_mnts.append(f'{storage_dir}:/opt/vm/{os_class}/{storage_id}')

    vm_envs['SUPRA_APPS'] = os_class
    vm_envs['VM_OS_SUB_VER'] = os_sub_ver
    vm_envs['VM_STORAGE_ID'] = storage_id
    vm_envs['VM_CPU_CORES'] = cores
    vm_envs['VM_RAM'] = ram
    if guest_lan != '':
        vm_envs['VM_GUEST_LAN'] = guest_lan
    if mac != '':
        vm_envs['VM_MAC'] = mac
    if tcp_ports != '':
        vm_envs['VM_TCP_PORTS'] = tcp_ports
    if udp_ports != '':
        vm_envs['VM_UDP_PORTS'] = udp_ports
    if guest_vnc:
        vm_envs['VM_GUEST_VNC'] = '1'
    if guest_restrict:
        vm_envs['VM_GUEST_RESTRICT'] = '1'
    if guest_tcp_fwds != '':
        vm_envs['VM_GUEST_TCP_FWDS'] = guest_tcp_fwds

    lc = LogConfig(type=LogConfig.types.SYSLOG, config={
        'tag': '{{.Name}}',
        'syslog-address': f'unixgram://{vm_log}'
    })

    logging.debug(f'vm.run: vm_mnts: {vm_mnts}, vm_log: {vm_log}, vm_envs: {vm_envs}')

    try:
        _check_vm_opts(os_class, os_sub_ver, cores, ram)
        container = client.containers.run(
            detach=True,
            remove=True,
            init=True,
            tty=True,
            network=container_network,
            devices=['/dev/kvm:/dev/kvm', '/dev/urandom:/dev/urandom'],
            volumes=vm_mnts,
            cap_add=vm_caps,
            environment=vm_envs,
            name=serv_name,
            log_config=lc,
            image=_proj_image
        )
    finally:
        client.close()


def list(start_with):
    client = docker.from_env()
    ret = []

    try:
        for container in client.containers.list(all=True):
            if container.name.startswith(start_with):
                ret.append({'name': container.name, 'status': container.status})
    finally:
        client.close()

    return ret


if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    #run('test-vm', 'test', 'host', 'windows-10', 'machine-test1', '/home/supra/test-vm-mnt/machine/machine-test1', guest_vnc=True)
    #stop('test-vm')
    print(list('v'))
