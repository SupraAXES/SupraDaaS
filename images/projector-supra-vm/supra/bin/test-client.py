#!/usr/bin/env python3
import asyncio

from aiohttp import ClientSession
from jsonrpcclient import Ok, Error, request, parse


async def test_rpc(session, method, para):
    async with session.post('http://127.0.0.1:12345', json=request(method, params=para)) as response:
        resp_json = await response.json()
        print(method)
        print(para)
        print(resp_json)
        parsed = parse(resp_json)
        if isinstance(parsed, Ok):
            print('Ok')
        elif isinstance(parsed, Error):
            print('Error')


async def test_status(session, adm_name, adm_passwd, unit):
    test_para = {'adm_name': adm_name, 'adm_passwd': adm_passwd, 'unit': unit}
    await test_rpc(session, 'status', test_para)


async def test_shutdown(session, adm_name, adm_passwd):
    test_para = {'adm_name': adm_name, 'adm_passwd': adm_passwd}
    await test_rpc(session, 'shutdown', test_para)


async def test_remove_rdp_user(session, adm_name, adm_passwd, name):
    test_para = {'adm_name': adm_name, 'adm_passwd': adm_passwd, 'name': name}
    await test_rpc(session, 'remove_rdp_user', test_para)


async def test_new_rdp_user(session, adm_name, adm_passwd, name, passwd):
    test_para = {'adm_name': adm_name, 'adm_passwd': adm_passwd, 'name': name, 'passwd': passwd}
    await test_rpc(session, 'new_rdp_user', test_para)


async def test_remove_adm_user(session, adm_name, adm_passwd, name):
    test_para = {'adm_name': adm_name, 'adm_passwd': adm_passwd, 'name': name}
    await test_rpc(session, 'remove_adm_user', test_para)


async def test_new_adm_user(session, adm_name, adm_passwd, name, passwd):
    test_para = {'adm_name': adm_name, 'adm_passwd': adm_passwd, 'name': name, 'passwd': passwd}
    await test_rpc(session, 'new_adm_user', test_para)


async def test_init_and_format_all_raw_data_disk(session, adm_name, adm_passwd):
    test_para = {'adm_name': adm_name, 'adm_passwd': adm_passwd}
    await test_rpc(session, 'init_and_format_all_raw_data_disk', test_para)


async def test_try_init_and_format_data_disk(session, adm_name, adm_passwd, data_disk_idx):
    test_para = {'adm_name': adm_name, 'adm_passwd': adm_passwd, 'data_disk_idx': data_disk_idx}
    await test_rpc(session, 'try_init_and_format_data_disk', test_para)


async def main() -> None:
    async with ClientSession() as session:
        await test_rpc(session, 'test', {'p1': 1, 'p2': 2, })
        await test_status(session, 'root', 'supra', 'ssh')
        await test_status(session, 'supra', 'supra', 'xxx')
        await test_status(session, 'supra', 'supra', 'rdp')
        await test_status(session, 'supra', 'supra', 'qemu')
        #await test_remove_rdp_user(session, 'supra', 'supra', 'qemu-fake-user')
        #await test_remove_rdp_user(session, 'supra', 'supra', 'test123')
        #await test_new_rdp_user(session, 'supra', 'supra', 'test123', 'test123')
        #await test_remove_rdp_user(session, 'supra', 'supra', 'test123')
        #await test_remove_adm_user(session, 'supra', 'supra', 'fake-adm-123')
        #await test_new_adm_user(session, 'supra', 'supra', 'adm-123', 'adm-123')
        #await test_remove_adm_user(session, 'supra', 'supra', 'adm-123')
        #await test_init_and_format_all_raw_data_disk(session, 'supra', 'supra')
        #await test_try_init_and_format_data_disk(session, 'supra', 'supra', 0)
        #await test_try_init_and_format_data_disk(session, 'supra', 'supra', 1)
        #await test_try_init_and_format_data_disk(session, 'supra', 'supra', 2)
        #await test_shutdown(session, 'supra', 'supra')
        pass


if __name__ == '__main__':
    asyncio.run(main())
