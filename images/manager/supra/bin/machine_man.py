#!/usr/bin/env python3
import os
import subprocess
from shutil import rmtree, copytree, copyfile
from pathlib import Path
from typing import List
from datetime import datetime

import template_man

_real_base = '/opt/vm-storage'

_local_base = '/vm-storage'


def init(base_dir : str):
    global _real_base
    _real_base = base_dir


def to_sysbase_path(machine_id : str) -> str:
    return f'{_local_base}/machine/{machine_id}/base.qcow2'

def to_sysdisk_path(machine_id : str) -> str:
    return f'{_local_base}/machine/{machine_id}/sys.qcow2'

def to_datadisk_path(machine_id : str, idx : int = 0) -> str:
    return f'{_local_base}/machine/{machine_id}/data{idx}.qcow2'

def to_dir(machine_id : str) -> str:
    return f'{_local_base}/machine/{machine_id}'

def to_real_dir(machine_id : str) -> str:
    p = to_dir(machine_id)
    if not os.path.exists(p):
        raise FileNotFoundError(f'{machine_id} not found')
    return f'{_real_base}/machine/{machine_id}'


def list() -> List[str]:
    p = Path(f'{_local_base}/machine')
    return [x.name for x in p.iterdir() if x.is_dir()]


def delete(machine_id : str):
    p = to_dir(machine_id)
    rmtree(p, ignore_errors=True)


def create(machine_id : str, from_template_id : str, datadisk_sizes : List[str] = ['64G']):
    p = to_dir(machine_id)
    if os.path.exists(p):
        raise FileExistsError(f'{machine_id} exists')

    try:
        os.makedirs(p, exist_ok=True)
        os.link(template_man.to_path(from_template_id), to_sysbase_path(machine_id))
        subprocess.check_call(f'cd {p};qemu-img create -f qcow2 -F qcow2 -b base.qcow2 sys.qcow2', shell=True)
        for idx, disk_size in enumerate(datadisk_sizes):
            subprocess.check_call(f'cd {p};qemu-img create -q -f qcow2 -o lazy_refcounts=on,preallocation=off data{idx}.qcow2 {disk_size}', shell=True)
    except:
        rmtree(p, ignore_errors=True)
        raise


def create_from_machine(machine_id : str, from_machine_id : str, datadisk_sizes : List[str] = ['64G']):
    p = to_dir(machine_id)
    if os.path.exists(p):
        raise FileExistsError(f'{machine_id} exists')

    try:
        os.makedirs(p, exist_ok=True)
        os.link(to_sysbase_path(from_machine_id), to_sysbase_path(machine_id))
        copyfile(to_sysdisk_path(from_machine_id), to_sysdisk_path(machine_id))
        for idx, disk_size in enumerate(datadisk_sizes):
            subprocess.check_call(f'cd {p};qemu-img create -q -f qcow2 -o lazy_refcounts=on,preallocation=off data{idx}.qcow2 {disk_size}', shell=True)
    except:
        rmtree(p, ignore_errors=True)
        raise


def clone(machine_id : str, from_machine_id: str):
    p = to_dir(machine_id)
    if os.path.exists(p):
        raise FileExistsError(f'{machine_id} exists')

    try:
        copytree(to_dir(from_machine_id), p)
    except:
        rmtree(p, ignore_errors=True)
        raise


def _rename_force(from_file, to_file):
    try:
        os.rename(from_file, to_file)
    except:
        pass

def _remove_force(file):
    try:
        os.remove(file)
    except:
        pass

def rebase(machine_id : str, from_template_id : str):
    p = to_dir(machine_id)
    if not os.path.exists(p):
        raise FileNotFoundError(f'{machine_id} not found')

    p_sysbase = to_sysbase_path(machine_id)
    p_sysbase_bak = f'{p_sysbase}.bak'
    p_sysdisk = to_sysdisk_path(machine_id)
    p_sysdisk_bak = f'{p_sysdisk}.bak'
    try:
        os.rename(p_sysbase, p_sysbase_bak)
        os.rename(p_sysdisk, p_sysdisk_bak)
        os.link(template_man.to_path(from_template_id), p_sysbase)
        subprocess.check_call(f'cd {p};qemu-img create -f qcow2 -F qcow2 -b base.qcow2 sys.qcow2', shell=True)
        _remove_force(p_sysbase_bak)
        _remove_force(p_sysdisk_bak)
    except:
        _rename_force(p_sysbase_bak, p_sysbase)
        _rename_force(p_sysdisk_bak, p_sysdisk)
        raise


def rebase_from_machine(machine_id : str, from_machine_id : str):
    p = to_dir(machine_id)
    if not os.path.exists(p):
        raise FileNotFoundError(f'{machine_id} not found')

    p_sysbase = to_sysbase_path(machine_id)
    p_sysbase_bak = f'{p_sysbase}.bak'
    p_sysdisk = to_sysdisk_path(machine_id)
    p_sysdisk_bak = f'{p_sysdisk}.bak'
    try:
        os.rename(p_sysbase, p_sysbase_bak)
        os.rename(p_sysdisk, p_sysdisk_bak)
        os.link(to_sysbase_path(from_machine_id), to_sysbase_path(machine_id))
        copyfile(to_sysdisk_path(from_machine_id), to_sysdisk_path(machine_id))
        _remove_force(p_sysbase_bak)
        _remove_force(p_sysdisk_bak)
    except:
        _rename_force(p_sysbase_bak, p_sysbase)
        _rename_force(p_sysdisk_bak, p_sysdisk)
        raise


# for experimental usage
# it is not safe
def offline_snapshot(machine_id : str):
    p = to_dir(machine_id)
    if not os.path.exists(p):
        raise FileNotFoundError(f'{machine_id} not found')

    p_sysdisk = to_sysdisk_path(machine_id)
    p_sysdisk_bak = f'{p_sysdisk}.bak'
    ts = datetime.now().strftime('%Y%m%d%H%M%S%f')
    sysdisk_ts = f'snapshot-sys-{ts}.qcow2'
    p_sysdisk_ts = f'{to_dir(machine_id)}/{sysdisk_ts}'
    try:
        os.rename(p_sysdisk, p_sysdisk_bak)
        os.link(p_sysdisk_bak, p_sysdisk_ts)
        subprocess.check_call(f'cd {p};qemu-img create -f qcow2 -F qcow2 -b {sysdisk_ts} sys.qcow2', shell=True)
        _remove_force(p_sysdisk_bak)
    except:
        _remove_force(p_sysdisk_ts)
        _rename_force(p_sysdisk_bak, p_sysdisk)
        raise


if __name__ == '__main__':
    init('/home/supra/test-vm-mnt')
    template_man.init('/home/supra/test-vm-mnt')
    print(to_sysdisk_path('test1'))
    print(to_dir('test1'))
    print(list())
    #delete('machine-test1')
    #print(list())
    #create('machine-test1', 'template-test1', ['8G', '32G'])
    #create_from_machine('machine-test2', 'machine-test1', ['32G'])
    #rebase('machine-test1', 'template-test1')
    #rebase_from_machine('machine-test1', 'supra-test0')
    #clone('machine-test1', 'supra-test0')
    #print(to_real_dir('test1'))
    #offline_snapshot('machine-test1')
    pass
