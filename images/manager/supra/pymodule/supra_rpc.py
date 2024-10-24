#!/usr/bin/env python3
import os
import asyncio
import functools
import logging
from concurrent.futures import ThreadPoolExecutor
import json

import ipaddress
import netifaces

from aiohttp import web
from jsonrpcserver import method, Result, Success, async_dispatch


def post_in_thread(thread_proc, *args, **kwargs):
    loop = asyncio.get_running_loop()
    p_proc = functools.partial(thread_proc, *args, **kwargs)
    return loop.run_in_executor(None, p_proc)


#set log level
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


#get ips listened
try:
    serv_nets = os.environ.get('SERV_NETS').split(',')
except:
    serv_nets = []

target_nets = set([ipaddress.ip_network(x, False) for x in serv_nets])
listen_ips = set()
listen_ips.add('127.0.0.1')

interfaces = netifaces.interfaces()
for interface in interfaces:
    addrs = netifaces.ifaddresses(interface)
    if netifaces.AF_INET in addrs:
        for addr in addrs[netifaces.AF_INET]:
            if_ip = addr['addr']
            if_mask = addr['netmask']
            if_subnet = ipaddress.ip_network(f"{if_ip}/{if_mask}", False)
            if if_subnet in target_nets:
                listen_ips.add(if_ip)


_health_checker = None

def init(*, health_checker=None):
    global _health_checker
    _health_checker = health_checker

@method
async def health_check() -> Result:
    global _health_checker
    ret = 'OK'
    if _health_checker is not None:
        ret = await post_in_thread(_health_checker)
    return Success(f'{ret}')


async def handle(request: web.Request) -> web.Response:
    return web.Response(text=await async_dispatch(await request.text()), content_type='application/json')


def _filter_and_log_req(rpc_req, is_debug):
    if is_debug:
        logging.debug(f'rpc_req: {rpc_req}')
    else:
        logging.info(f'rpc_req: {rpc_req}')

def _filter_and_log_resp(rpc_resp, is_debug):
    if is_debug:
        logging.debug(f'rpc_resp: {rpc_resp}')
    else:
        logging.info(f'rpc_resp: {rpc_resp}')

async def logging_filter(app, handler):
    async def middleware(request):
        is_debug = False
        try:
            rpc_req = await request.json()
            is_debug = (rpc_req['method'] == 'health_check')
            _filter_and_log_req(rpc_req, is_debug)
        except:
            pass  # let it be

        response = await handler(request)
        try:
            rpc_resp = json.loads(response.text)
            _filter_and_log_resp(rpc_resp, is_debug)
        except:
            pass  # let it be

        return response

    return middleware


def run(*, port=12345):
    app = web.Application(middlewares=[logging_filter])
    app.router.add_post('/', handle)
    web.run_app(app, host=listen_ips, port=port)
