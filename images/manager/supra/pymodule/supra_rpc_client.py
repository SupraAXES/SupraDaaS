#!/usr/bin/env python3
import asyncio
import logging

from aiohttp import ClientSession, ClientTimeout
from jsonrpcclient import Ok, Error, request, parse
import ssl


sslctx = ssl.create_default_context()
sslctx.check_hostname = False
sslctx.verify_mode = ssl.CERT_NONE

timeout = ClientTimeout(total=60*60)


def gen_supra_rpc_call(method_url):
    async def _call_supra_rpc(method, **para):
        async with ClientSession(timeout=timeout) as session:
            async with session.post(method_url, json=request(method, params=para), ssl=sslctx) as response:
                parsed = parse(await response.json())
                if isinstance(parsed, Ok):
                    return parsed.result
                elif isinstance(parsed, Error):
                    raise Exception(parsed.data)  # We always use exception under hook, and data contains the description
    return _call_supra_rpc
