#!/usr/bin/env python3
import asyncio
import logging

from aiohttp import ClientSession
from jsonrpcclient import Ok, Error, request, parse


_runner_url = ''
_vm_log = ''

def init(runner_url, vm_log):
    global _runner_url
    global _vm_log
    _runner_url = runner_url
    _vm_log = vm_log


async def _call_vm_runner(method, para):
    global _runner_url
    async with ClientSession() as session:
        async with session.post(_runner_url, json=request(method, params=para)) as response:
            parsed = parse(await response.json())
            if isinstance(parsed, Ok):
                return parsed.result
            elif isinstance(parsed, Error):
                raise Exception(parsed.data)  # We always use exception under hook, and data contains the description


async def run(serv_name, serv_token, container_network, os_type, storage_id, storage_dir, vm_opts):
    para = {'serv_name': serv_name, 'serv_token': serv_token, 'container_network': container_network,
            'os_type': os_type, 'storage_id': storage_id, 'storage_dir': storage_dir, 'vm_log': _vm_log,
            'vm_opts': vm_opts}
    return await _call_vm_runner('run_vm', para)


async def stop(serv_name):
    return await _call_vm_runner('stop_vm', {'serv_name': serv_name})


async def list(start_with):
    return await _call_vm_runner('list_vm', {'start_with': start_with})
