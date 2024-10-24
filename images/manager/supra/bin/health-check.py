#!/usr/bin/env python3
import asyncio

from supra_rpc_client import gen_supra_rpc_call


_health_rpc = gen_supra_rpc_call('https://127.0.0.1:1711/health-checker/')


async def main() -> None:
        await _health_rpc('health_check')


if __name__ == '__main__':
    asyncio.run(main())
