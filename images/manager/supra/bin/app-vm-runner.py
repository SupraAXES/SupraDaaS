#!/usr/bin/env python3
import os

from jsonrpcserver import method, Result, Success
from supra_rpc import init, run, post_in_thread

import vm


@method
async def run_vm(*, serv_name, serv_token, container_network, os_type, storage_id, storage_dir, vm_log, vm_opts) -> Result:
    await post_in_thread(vm.run, serv_name, serv_token, container_network, os_type, storage_id, storage_dir, vm_log, **vm_opts)
    return Success(f'{serv_name} runned')


@method
async def stop_vm(*, serv_name) -> Result:
    await post_in_thread(vm.stop, serv_name)
    return Success(f'{serv_name} stopped')


@method
async def list_vm(*, start_with) -> Result:
    return Success(await post_in_thread(vm.list, start_with))


init()
run(port=22345)
