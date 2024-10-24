#!/usr/bin/env python3
import os
from pathlib import Path
from typing import List, Dict
import json

_real_base = '/opt/vm-storage'

_local_base = '/vm-storage'


def init(base_dir : str):
    global _real_base
    _real_base = base_dir


def to_path(meta_id : str) -> str:
    return f'{_local_base}/meta/{meta_id}/sys.qcow2'

def to_dir(meta_id : str) -> str:
    return f'{_local_base}/meta/{meta_id}'

def to_real_dir(meta_id : str) -> str:
    p = to_dir(meta_id)
    if not os.path.exists(p):
        raise FileNotFoundError(f'{meta_id} not found')
    return f'{_real_base}/meta/{meta_id}'


def get_desc(meta_id: str) -> Dict:
    with open(f'{to_dir(meta_id)}/meta.info') as f:
        meta_info = json.load(f)
    desc = {"id": meta_id, "info": meta_info}
    return desc

def list() -> List[Dict]:
    p = Path(f'{_local_base}/meta')
    return [get_desc(x.name) for x in p.iterdir() if x.is_dir()]


if __name__ == '__main__':
    init('/home/supra/test-vm-mnt')
    print(to_path('test1'))
    print(to_dir('test1'))
    print(list())
    print(to_real_dir('test1'))
