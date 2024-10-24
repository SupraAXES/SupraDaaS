#!/usr/bin/env python3
import asyncio

from aiohttp import ClientSession
from jsonrpcclient import Ok, Error, request, parse
import ssl


sslctx = ssl.create_default_context()
sslctx.check_hostname = False
sslctx.verify_mode = ssl.CERT_NONE


async def test_rpc(session, method, para):
    async with session.post('https://127.0.0.1:1711/vm/', json=request(method, params=para), ssl=sslctx) as response:
        resp_json = await response.json()
        print(method)
        print(para)
        print(resp_json)
        parsed = parse(resp_json)
        if isinstance(parsed, Ok):
            print('Ok')
        elif isinstance(parsed, Error):
            print('Error')


async def test_list_method(session, list_type):
    await test_rpc(session, f'list_{list_type}', {})


async def test_template_meta(session, template_id):
    test_para = {'template_id': template_id}
    await test_rpc(session, 'template_meta', test_para)


async def test_create_template(session, template_id, sys_from_type, sys_from_id):
    test_para = {'template_id': template_id, 'sys_from_type': sys_from_type, 'sys_from_id': sys_from_id}
    await test_rpc(session, 'create_template', test_para)


async def test_delete_template(session, template_id):
    test_para = {'template_id': template_id}
    await test_rpc(session, 'delete_template', test_para)


async def test_create_machine(session, machine_id, sys_from_type, sys_from_id, data_disk_sizes):
    test_para = {'machine_id': machine_id, 'sys_from_type': sys_from_type, 'sys_from_id': sys_from_id, 'data_disk_sizes': data_disk_sizes}
    await test_rpc(session, 'create_machine', test_para)


async def test_rebase_machine(session, machine_id, sys_from_type, sys_from_id):
    test_para = {'machine_id': machine_id, 'sys_from_type': sys_from_type, 'sys_from_id': sys_from_id}
    await test_rpc(session, 'rebase_machine', test_para)


async def test_clone_machine(session, machine_id, machine_from_id):
    test_para = {'machine_id': machine_id, 'machine_from_id': machine_from_id}
    await test_rpc(session, 'clone_machine', test_para)


async def test_delete_machine(session, machine_id):
    test_para = {'machine_id': machine_id}
    await test_rpc(session, 'delete_machine', test_para)


async def test_run_vm(session, serv_name, serv_token, container_network, os_type, vm_id_type, vm_id, vm_opts):
    test_para = {'serv_name': serv_name, 'serv_token': serv_token, 'container_network': container_network,
                 'os_type': os_type, 'vm_id_type': vm_id_type, 'vm_id': vm_id,
                 'vm_opts': vm_opts}
    await test_rpc(session, 'run_vm', test_para)


async def test_stop_vm(session, serv_name):
    test_para = {'serv_name': serv_name}
    await test_rpc(session, 'stop_vm', test_para)


async def test_list_vm(session, start_with):
    test_para = {'start_with': start_with}
    await test_rpc(session, 'list_vm', test_para)


async def test_backup(session, vm_id_type, vm_id):
    test_para = {'vm_id_type': vm_id_type, 'vm_id': vm_id}
    await test_rpc(session, 'backup', test_para)


async def test_list_snapshot(session, vm_id_type, vm_id):
    test_para = {'vm_id_type': vm_id_type, 'vm_id': vm_id}
    await test_rpc(session, 'list_snapshot', test_para)


async def test_remove_snapshot(session, snapshot_id):
    test_para = {'snapshot_id': snapshot_id}
    await test_rpc(session, 'remove_snapshot', test_para)


async def test_restore(session, snapshot_id):
    test_para = {'snapshot_id': snapshot_id}
    await test_rpc(session, 'restore', test_para)


async def main() -> None:
    async with ClientSession() as session:
        #await test_rpc(session, 'test', {'p1': 1, 'p2': 2, })
        #await test_list_method(session, 'meta')
        await test_list_method(session, 'template')
        await test_template_meta(session, 'template-win7')
        #await test_list_method(session, 'machine')
        #await test_list_vm(session, 'v')
        #await test_backup(session, 'meta', 'ubuntu-2204')
        #await test_list_snapshot(session, 'meta', 'ubuntu-2204')
        #await test_remove_snapshot(session, 'aaf80f5f')
        #await test_list_snapshot(session, 'meta', 'ubuntu-2204')
        #await test_list_snapshot(session, 'meta', 'win7-ulti')
        #await test_restore(session, '37de4d79')
        #await test_restore(session, 'c5bae01f')
        #await test_delete_template(session, 'template-test1')
        #await test_delete_template(session, 'template-test2')
        #await test_create_template(session, 'template-win7', 'meta', 'win7-ulti')
        #await test_create_template(session, 'template-test1', 'meta', 'win10-supra')
        #await test_create_template(session, 'template-test2', 'template', 'template-test1')
        #await test_delete_machine(session, 'machine-test1')
        #await test_delete_machine(session, 'machine-test2')
        #await test_delete_machine(session, 'machine-test2-cloned')
        #await test_create_machine(session, 'machine-win7', 'template', 'template-win7', [])
        #await test_create_machine(session, 'machine-test1', 'template', 'template-test1', ['16G'])
        #await test_create_machine(session, 'machine-test2', 'machine', 'machine-test1', ['64G'])
        #await test_rebase_machine(session, 'machine-test1', 'template', 'template-test1')
        #await test_rebase_machine(session, 'machine-test1', 'machine', 'machine-test2')
        #await test_clone_machine(session, 'machine-test2-cloned', 'machine-test2')
        #await test_stop_vm(session, 'test-vm-xyfex')
        #await test_run_vm(session, 'test-vm', 'test', 'host', 'windows-10', 'machine', 'machine-test1', {'guest_vnc': True, 'mac':'00:00:00:01:02:03', 'cores': '2', 'ram': '6G'})

        #await test_stop_vm(session, 'test-vm')
        #await test_run_vm(session, 'test-vm', 'test', 'host', 'windows-7', 'meta', 'win7-ulti', {'guest_vnc': True})
        #await test_run_vm(session, 'test-vm', 'test', 'host', 'windows-10', 'meta', 'win10-2021-ltsc', {'guest_vnc': True})
        #await test_run_vm(session, 'test-vm', 'test', 'host', 'ubuntu-2204', 'meta', 'ubuntu-2204', {'guest_vnc': True})


if __name__ == '__main__':
    asyncio.run(main())
