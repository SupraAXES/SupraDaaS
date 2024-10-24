#!/usr/bin/env python3
import os
import asyncio
import functools
import logging
import json

import psutil
from aiohttp import web
from jsonrpcserver import method, Result, Success, async_dispatch
import paramiko


import linux_rdp_user_man as rdp_user_man
import linux_adm_user_man as adm_user_man
import linux_data_disk_man as data_disk_man


def post_in_thread(thread_proc, *args, **kwargs):
    loop = asyncio.get_running_loop()
    p_proc = functools.partial(thread_proc, *args, **kwargs)
    return loop.run_in_executor(None, p_proc)


def _ssh_shutdown(adm_name, adm_passwd):
    if adm_name != 'root':
        raise Exception('linux adm must be root')

    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    try:
        ssh.connect('127.0.0.1', port=5985, username='root', password=adm_passwd)
        stdin, stdout, stderr = ssh.exec_command('shutdown now')
    finally:
        ssh.close()


@method
async def shutdown(*, adm_name, adm_passwd) -> Result:
    await post_in_thread(_ssh_shutdown, adm_name, adm_passwd)
    return Success('shutdown')


def _ssh_status_ssh(adm_name, adm_passwd):
    if adm_name != 'root':
        raise Exception('linux adm must be root')

    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    try:
        ssh.connect('127.0.0.1', port=5985, username='root', password=adm_passwd)
        stdin, stdout, stderr = ssh.exec_command('systemctl status ssh')
        res = stdout.read().decode()
        if 'active (running)' in res:
            return 'Running'
        else:
            return 'Not-Running'
    finally:
        ssh.close()


def _ps_status_qemu():
    for proc in psutil.process_iter():
        if 'qemu' in proc.cmdline()[0]:
            return 'Running'
    return 'Not-Running'


@method
async def status(*, adm_name, adm_passwd, unit: str) -> Result:
    if unit == 'ssh':
        return Success(f'{await post_in_thread(_ssh_status_ssh, adm_name, adm_passwd)}')
    elif unit == 'qemu':
        return Success(f'{await post_in_thread(_ps_status_qemu)}')
    else:
        raise Exception(f'Not supported unit: {unit}')


@method
async def new_rdp_user(*, adm_name, adm_passwd, name, passwd) -> Result:
    await post_in_thread(rdp_user_man.new, adm_name, adm_passwd, name, passwd)
    return Success(f'new-rdp-user: {name}')


@method
async def remove_rdp_user(*, adm_name, adm_passwd, name) -> Result:
    await post_in_thread(rdp_user_man.remove, adm_name, adm_passwd, name)
    return Success(f'remove-rdp-user: {name}')


@method
async def new_adm_user(*, adm_name, adm_passwd, name, passwd) -> Result:
    await post_in_thread(adm_user_man.new, adm_name, adm_passwd, name, passwd)
    return Success(f'new-adm-user: {name}')


@method
async def remove_adm_user(*, adm_name, adm_passwd, name) -> Result:
    await post_in_thread(adm_user_man.remove, adm_name, adm_passwd, name)
    return Success(f'remove-adm-user: {name}')


@method
async def init_and_format_all_raw_data_disk(*, adm_name, adm_passwd) -> Result:
    await post_in_thread(data_disk_man.init_and_format_all_raw, adm_name, adm_passwd)
    return Success(f'init-and-format-all-raw-data-disk')


@method
async def try_init_and_format_data_disk(*, adm_name, adm_passwd, data_disk_idx) -> Result:
    await post_in_thread(data_disk_man.try_init_and_format, adm_name, adm_passwd, data_disk_idx)
    return Success(f'try-init-and-format: {data_disk_idx}')


@method
async def test(*, p1, p2) -> Result:
    if p1 == 1:
        raise Exception('p1 == 1')
    return Success(f'{p1}, {p2}, done')


log_level = os.environ.get('LOG_LEVEL')
if log_level == 'debug':
    logging.basicConfig(level=logging.DEBUG)
elif log_level == 'info':
    logging.basicConfig(level=logging.INFO)
elif log_level == 'warning':
    logging.basicConfig(level=logging.WARNING)
elif log_level == 'error':
    logging.basicConfig(level=logging.ERROR)
elif log_level == 'critical':
    logging.basicConfig(level=logging.CRITICAL)
else:
    logging.basicConfig(level=logging.INFO)


async def handle(request: web.Request) -> web.Response:
    return web.Response(text=await async_dispatch(await request.text()), content_type='application/json')


def _filter_and_log_req(rpc_req):
    logging.info(f'rpc_req: {rpc_req}')

def _filter_and_log_resp(rpc_resp):
    logging.info(f'rpc_resp: {rpc_resp}')

async def logging_filter(app, handler):
    async def middleware(request):
        try:
            rpc_req = await request.json()
            _filter_and_log_req(rpc_req)
        except:
            pass  # let it be

        response = await handler(request)
        try:
            rpc_resp = json.loads(response.text)
            _filter_and_log_resp(rpc_resp)
        except:
            pass  # let it be

        return response

    return middleware


app = web.Application(middlewares=[logging_filter])
app.router.add_post('/', handle)


if __name__ == '__main__':
    #print(_ssh_shutdown('root', 'supra'))
    #print(_ssh_status_ssh('root', 'supra'))
    web.run_app(app, host='0.0.0.0', port=12345)  # wait for further dev. use 0.0.0.0 for now
