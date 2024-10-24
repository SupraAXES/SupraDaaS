#!/usr/bin/env python3
import os
import asyncio
import functools
import logging
import json
import subprocess
import time

import psutil
from aiohttp import web
from jsonrpcserver import method, Result, Success, async_dispatch


def post_in_thread(thread_proc, *args, **kwargs):
    loop = asyncio.get_running_loop()
    p_proc = functools.partial(thread_proc, *args, **kwargs)
    return loop.run_in_executor(None, p_proc)


def _psexec_shutdown(adm_name, adm_passwd):
    cmd = f'psexec.py {adm_name}:{adm_passwd}@127.0.0.1 "shutdown /s /t 15" -path c:\\\\windows\\\\system32\\\\'
    process = subprocess.Popen([cmd], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, shell=True)
    time.sleep(1)
    try:
        process.stdin.write('\n')
        process.stdin.flush()
    except:
        pass
    process.wait()
    if process.returncode != 0:
        raise Exception(f'psexec failed with {process.returncode}')
    return 'shutdown issued success'


# don't use it to get hostname. it is not working for xp. we just use it to check smb1 working
# alse smb1 is not used for file-sharing here!
def _psexec_smb1(adm_name, adm_passwd):
    cmd = f'psexec.py {adm_name}:{adm_passwd}@127.0.0.1 "hostname" -path c:\\\\windows\\\\system32\\\\'
    process = subprocess.Popen([cmd], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, shell=True)
    time.sleep(1)
    try:
        process.stdin.write('\n')
        process.stdin.flush()
    except:
        pass
    process.wait()
    if process.returncode != 0:
        raise Exception(f'psexec failed with {process.returncode}')
    return 'Running'


@method
async def shutdown(*, adm_name, adm_passwd) -> Result:
    return Success(f'{await post_in_thread(_psexec_shutdown, adm_name, adm_passwd)}')


def _ps_status_qemu():
    for proc in psutil.process_iter():
        if 'qemu' in proc.cmdline()[0]:
            return 'Running'
    return 'Not-Running'


@method
async def status(*, adm_name, adm_passwd, unit: str) -> Result:
    if unit == 'qemu':
        return Success(f'{await post_in_thread(_ps_status_qemu)}')
    elif unit == 'smb1':
        return Success(f'{await post_in_thread(_psexec_smb1, adm_name, adm_passwd)}')
    else:
        raise Exception(f'Not supported unit: {unit}')


@method
async def new_rdp_user(*, adm_name, adm_passwd, name, passwd) -> Result:
    raise Exception('not support')


@method
async def remove_rdp_user(*, adm_name, adm_passwd, name) -> Result:
    raise Exception('not support')


@method
async def new_adm_user(*, adm_name, adm_passwd, name, passwd) -> Result:
    raise Exception('not support')


@method
async def remove_adm_user(*, adm_name, adm_passwd, name) -> Result:
    raise Exception('not support')


@method
async def init_and_format_all_raw_data_disk(*, adm_name, adm_passwd) -> Result:
    raise Exception('not support')


@method
async def try_init_and_format_data_disk(*, adm_name, adm_passwd, data_disk_idx) -> Result:
    raise Exception('not support')


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
    web.run_app(app, host='0.0.0.0', port=12345)  # wait for further dev. use 0.0.0.0 for now
