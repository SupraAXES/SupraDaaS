#!/usr/bin/env python3
import os
import subprocess
from subprocess import DEVNULL
from typing import List
from typing import TypedDict

import meta_man
import template_man
import machine_man


def _get_target_dir(vm_id_type, vm_id):
    if vm_id_type == 'meta':
        p = meta_man.to_dir(vm_id)
    elif vm_id_type == 'template':
        p = template_man.to_dir(vm_id)
    elif vm_id_type == 'machine':
        p = machine_man.to_dir(vm_id)
    else:
        raise Exception(f'unsupport vm-id-type: {vm_id_type}')
    if not os.path.exists(p):
        raise FileNotFoundError(f'{vm_id_type} {vm_id} not found')
    return p


def init():
    subprocess.run('restic init', stdout=DEVNULL, stderr=DEVNULL, shell=True)  # ignore and restic-check handle errors
    subprocess.run('restic unlock', stdout=DEVNULL, stderr=DEVNULL, shell=True)  # ignore and restic-check handle errors
    subprocess.check_output('restic check', shell=True)


def backup(vm_id_type : str, vm_id : str):
    p = _get_target_dir(vm_id_type, vm_id)
    subprocess.check_output(f'restic backup {p}', shell=True)


class Snapshot(TypedDict):
    id: str
    date: str

def list_snapshot(vm_id_type : str, vm_id : str) -> List[Snapshot]:
    p = _get_target_dir(vm_id_type, vm_id)
    lines = subprocess.check_output(f'restic snapshots --path="{p}"', shell=True).decode().split('\n')
    snapshot_lines = list(filter(lambda x: x.endswith(p), lines))
    return [{'id': x_split[0], 'date': x_split[1]} for x_split in [x.split('  ') for x in snapshot_lines]]


def remove_snapshot(snapshot_id : str):
    subprocess.check_output(f'restic forget {snapshot_id} --prune', shell=True)


def restore(snapshot_id : str):
    subprocess.check_output(f'restic restore {snapshot_id} --target /', shell=True)


if __name__ == '__main__':
    init()
    #backup('meta', 'ubuntu-2204')
    print(list_snapshot('meta', 'ubuntu-2204'))
    #restore('aaf80f5f', 'meta', 'ubuntu-2204')
    #remove_snapshot('29562535')
    pass
