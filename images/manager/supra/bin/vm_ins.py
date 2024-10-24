#!/usr/bin/env python3
import os
import asyncio
import logging
import requests

_dev_adm = os.environ.get('VM_ADMIN_HOSTNAME')
assert _dev_adm is not None, 'please set VM_ADMIN_HOSTNAME'
logging.debug(f'vm_ins VM_ADMIN_HOSTNAME: {_dev_adm}')


def _to_ins_id(vm_id):
    return f'{vm_id}'


async def try_shutdown(vm_id):
    try:
        ins_id = _to_ins_id(vm_id)
        post_url = f'http://{_dev_adm}:8080/api/admin/resource/vm/op?id={ins_id}&action=stop'
        logging.debug(f'{post_url}')
        res = await asyncio.to_thread(requests.post, post_url)
        logging.debug(f'{res}')
    except Exception as e:
        logging.warning(f'try shutdonw vm ins {e}')


if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    asyncio.run(try_shutdown('test-vm'))
