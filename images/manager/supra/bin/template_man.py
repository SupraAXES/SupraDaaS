#!/usr/bin/env python3
import os
from shutil import rmtree
from pathlib import Path
from typing import List, Dict
import subprocess
import json

import meta_man

_real_base = '/opt/vm-storage'

_local_base = '/vm-storage'


def init(base_dir : str):
    global _real_base
    _real_base = base_dir


def to_path(template_id : str) -> str:
    return f'{_local_base}/template/{template_id}/sys.qcow2'

def to_dir(template_id : str) -> str:
    return f'{_local_base}/template/{template_id}'

def to_real_dir(template_id : str) -> str:
    p = to_dir(template_id)
    if not os.path.exists(p):
        raise FileNotFoundError(f'{template_id} not found')
    return f'{_real_base}/template/{template_id}'

def copy_from(path_from, path_to):
    cmd = f'cp -r {path_from} {path_to}'
    subprocess.check_call(cmd, shell=True)


def list() -> List[str]:
    p = Path(f'{_local_base}/template')
    return [x.name for x in p.iterdir() if x.is_dir()]


def meta(template_id: str) -> Dict:
    p = to_dir(template_id)
    with open(f'{p}/info.json', 'r') as f:
        return json.load(f)


def delete(template_id : str):
    p = to_dir(template_id)
    rmtree(p, ignore_errors=True)


def create(template_id : str, from_meta_id : str):
    p = to_dir(template_id)
    if os.path.exists(p):
        raise FileExistsError(f'{template_id} exists')

    try:
        os.makedirs(p, exist_ok=True)
        copy_from(meta_man.to_path(from_meta_id), to_path(template_id))
    except:
        rmtree(p, ignore_errors=True)
        raise


def clone(template_id : str, from_template_id : str):
    p = to_dir(template_id)
    if os.path.exists(p):
        raise FileExistsError(f'{template_id} exists')

    try:
        copy_from(to_dir(from_template_id), p)
    except:
        rmtree(p, ignore_errors=True)
        raise


if __name__ == '__main__':
    init('/home/supra/test-vm-mnt')
    meta_man.init('/home/supra/test-vm-mnt')
    print(to_path('test1'))
    print(to_dir('test1'))
    print(list())
    #delete('template-test1')
    #print(list())
    #create('template-test1', 'win10-supra')
    #print(list())
    #clone('template-test1','supra-test0')
    print(to_real_dir('test1'))
