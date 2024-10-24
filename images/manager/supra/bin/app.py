#!/usr/bin/env python3
import os
import logging
from jsonrpcserver import method, Result, Success
from supra_rpc import init, run, post_in_thread

import machine_man
import meta_man
import runner_man
import template_man
import backup_man


def check_id(norm_id):
    chars_allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-"
    if not all(char in chars_allowed for char in norm_id):
        raise Exception(f'malformed id: {norm_id}')


@method
async def list_meta() -> Result:
    ret = await post_in_thread(meta_man.list)
    return Success(ret)


@method
async def list_template() -> Result:
    ret = await post_in_thread(template_man.list)
    return Success(ret)


@method
async def template_meta(*, template_id) -> Result:
    ret = await post_in_thread(template_man.meta, template_id)
    return Success(ret)


@method
async def create_template(*, template_id, sys_from_type, sys_from_id) -> Result:
    check_id(template_id)
    check_id(sys_from_id)
    if sys_from_type == 'meta':
        await post_in_thread(template_man.create, template_id, sys_from_id)
    elif sys_from_type == 'template':
        await post_in_thread(template_man.clone, template_id, sys_from_id)
    else:
        raise Exception(f'unsupported sys_from_type: {sys_from_type}')
    return Success(f'template: {template_id} created')


@method
async def delete_template(*, template_id) -> Result:
    check_id(template_id)
    await post_in_thread(template_man.delete, template_id)
    return Success(f'template: {template_id} deleted')


@method
async def list_machine() -> Result:
    ret = await post_in_thread(machine_man.list)
    return Success(ret)


@method
async def create_machine(*, machine_id, sys_from_type, sys_from_id, data_disk_sizes) -> Result:
    check_id(machine_id)
    check_id(sys_from_id)
    if sys_from_type == 'template':
        await post_in_thread(machine_man.create, machine_id, sys_from_id, data_disk_sizes)
    elif sys_from_type == 'machine':
        await post_in_thread(machine_man.create_from_machine, machine_id, sys_from_id, data_disk_sizes)
    else:
        raise Exception(f'unsupported sys_from_type: {sys_from_type}')
    return Success(f'machine: {machine_id} created')


@method
async def rebase_machine(*, machine_id, sys_from_type, sys_from_id) -> Result:
    check_id(machine_id)
    check_id(sys_from_id)
    if sys_from_type == 'template':
        await post_in_thread(machine_man.rebase, machine_id, sys_from_id)
    elif sys_from_type == 'machine':
        await post_in_thread(machine_man.rebase_from_machine, machine_id, sys_from_id)
    else:
        raise Exception(f'unsupported sys_from_type: {sys_from_type}')
    return Success(f'machine: {machine_id} rebased')


@method
async def clone_machine(*, machine_id, machine_from_id) -> Result:
    check_id(machine_id)
    check_id(machine_from_id)
    await post_in_thread(machine_man.clone, machine_id, machine_from_id)
    return Success(f'machine: {machine_id} cloned')


@method
async def delete_machine(*, machine_id) -> Result:
    check_id(machine_id)
    await post_in_thread(machine_man.delete, machine_id)
    return Success(f'machine: {machine_id} deleted')


@method
async def run_vm(*, serv_name, serv_token, container_network, os_type, vm_id_type, vm_id, vm_opts={}) -> Result:
    check_id(vm_id)
    storage_id = vm_id
    if vm_id_type == 'template':
        storage_dir = template_man.to_real_dir(vm_id)
    elif vm_id_type == 'machine':
        storage_dir = machine_man.to_real_dir(vm_id)
    elif vm_id_type == 'meta':
        storage_dir = meta_man.to_real_dir(vm_id)
    else:
        raise Exception('Unsupported vm_id_type: {vm_id_type}')
    await runner_man.run(serv_name, serv_token, container_network, os_type, storage_id, storage_dir, vm_opts)
    return Success(f'vm: {serv_name} runned')


@method
async def stop_vm(*, serv_name) -> Result:
    await runner_man.stop(serv_name)
    return Success(f'vm: {serv_name} stopped')


@method
async def list_vm(*, start_with) -> Result:
    return Success(await runner_man.list(start_with))


@method
async def backup(*, vm_id_type, vm_id):
    check_id(vm_id_type)
    check_id(vm_id)
    return Success(await post_in_thread(backup_man.backup, vm_id_type, vm_id))


@method
async def list_snapshot(*, vm_id_type, vm_id):
    check_id(vm_id_type)
    check_id(vm_id)
    return Success(await post_in_thread(backup_man.list_snapshot, vm_id_type, vm_id))


@method
async def remove_snapshot(*, snapshot_id):
    check_id(snapshot_id)
    return Success(await post_in_thread(backup_man.remove_snapshot, snapshot_id))


@method
async def restore(*, snapshot_id):
    check_id(snapshot_id)
    return Success(await post_in_thread(backup_man.restore, snapshot_id))


vm_runner_url = 'http://127.0.0.1:22345'

supra_host_base = os.environ.get('SUPRA_HOST_BASE')
assert supra_host_base is not None, 'please set SUPRA_HOST_BASE'

runner_man.init(vm_runner_url, f'{supra_host_base}/vmlog')
logging.debug(f'vm_runner_url: {vm_runner_url}')
logging.debug(f'supra_host_base: {supra_host_base}')

vm_storage_base = os.environ.get('VM_STORAGE_BASE')
assert vm_storage_base is not None, 'please set VM_STORAGE_BASE'

meta_man.init(vm_storage_base)
template_man.init(vm_storage_base)
machine_man.init(vm_storage_base)
logging.debug(f'vm_storage_base: {vm_storage_base}')
backup_man.init()


init()
run(port=12345)
