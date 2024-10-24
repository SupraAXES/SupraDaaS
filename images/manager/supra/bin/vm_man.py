#!/usr/bin/env python3
import os
import asyncio
import logging

from supra_rpc_client import gen_supra_rpc_call

from vm_ins import _to_ins_id

_vm_man_nodes = ['127.0.0.1']
_vm_man_urls = [f'https://{x}:1711/vm/' for x in _vm_man_nodes]


def _gen_machine_man(vm_man_rpc):
    async def _list():
        try:
            return await vm_man_rpc('list_machine')
        except Exception as e:
            logging.warning(f'list machine {e}')
            return []


    async def _stop(machine_id):
        try:
            ins_id = _to_ins_id(machine_id)
            await vm_man_rpc('stop_vm', serv_name=ins_id)
        except Exception as e:
            logging.warning(f'stop machine {e}')


    async def _backup(machine_id):
        try:
            await vm_man_rpc('backup', vm_id_type='machine', vm_id=machine_id)
        except Exception as e:
            logging.warning(f'backup machine {e}')


    async def _list_vm(machine_id=''):
        try:
            return await vm_man_rpc('list_vm', start_with=_to_ins_id(machine_id))
        except Exception as e:
            logging.warning(f'backup machine {e}')
            return []


    class _inner:
        list = _list
        stop = _stop
        backup = _backup
        list_vm = _list_vm


    return _inner


async def _null_task_proc(manchine_man):
    logging.debug('null task')
    logging.debug(manchine_man)
    res = await manchine_man.list()
    logging.debug(res)
    res = await manchine_man.list_vm()
    logging.debug(res)
    return res


async def for_each_machine_man(*, task_proc):
    global _vm_man_urls
    tasks = []
    for vm_man_url in _vm_man_urls:
        vm_man_rpc = gen_supra_rpc_call(vm_man_url)
        machine_man = _gen_machine_man(vm_man_rpc)
        tasks.append(asyncio.create_task(task_proc(machine_man)))
    return [await task for task in tasks]


if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)

    async def main():
        logging.debug('test-man')
        rets = await for_each_machine_man(task_proc=_null_task_proc)
        logging.debug(rets)


    asyncio.run(main())
