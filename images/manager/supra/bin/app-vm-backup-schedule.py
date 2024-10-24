#!/usr/bin/env python3
import os
import asyncio
import time
import datetime
from datetime import timezone, timedelta
import logging
import json

# set log level
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

from scheduler.asyncio import Scheduler
from scheduler.trigger import Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday

import vm_ins
import vm_man

_days = [Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday]
_loop = None
_schedule = None
_quitting = False
_check_gap = 5

_tz_offset_env = os.environ.get('SUPRA_TZ_OFFSET')
_tz_offset = 0 if _tz_offset_env is None else int(_tz_offset_env)
assert(-12 <= _tz_offset <= 12)
_tz = timezone(timedelta(hours=_tz_offset))
_conf = {
    'force_shutdown': True,
    'weekly': [],
}


async def _machine_backup_one_vm_node(machine_man):
    global _conf
    logging.info('machine backup one vm node')
    machines = [ x for x in await machine_man.list() ]

    # NOTE: Neither is safe. Just make it go through...
    if _conf['force_shutdown']:
        for x in machines: await vm_ins.try_shutdown(x)
        await asyncio.sleep(2*60)
        for x in machines: await machine_man.stop(x)
        await asyncio.sleep(30)
    else:
        machines = [ x for x in machines if not await machine_man.list_vm(x) ]

    logging.info(f'preprae to backup machines: {machines}')
    for x in machines: await machine_man.backup(x)


async def _vm_backup():
    await vm_man.for_each_machine_man(task_proc=_machine_backup_one_vm_node)


async def _apply_conf():
    global _conf, _schedule, _days
    weekly_timings = []
    for e in _conf['weekly']:
        day, hour, minute, tz = e['day'], e['hour'], e['minute'], timezone(timedelta(hours=e['tz']))
        weekly_timings.append(_days[day-1](datetime.time(hour=hour, minute=minute, tzinfo=tz)))
    _schedule.delete_jobs()
    for timing in weekly_timings:
        _schedule.weekly(timing, _vm_backup)
    logging.debug(_schedule)


def _set_conf(force_shutdown, weekly):
    global _conf
    for e in weekly:
        day, hour, minute, tz = e['day'], e['hour'], e['minute'], e['tz']
        assert(type(day) is int and 1 <= day <= 7)
        assert(type(hour) is int and 0 <= hour <= 23)
        assert(type(minute) is int and 0 <= minute <= 59)
        assert(type(tz) is int and -12 <= tz <= 12)
    _conf['force_shutdown'] = force_shutdown
    _conf['weekly'] = weekly
    logging.info('backup schedule checked')


async def _main_loop():
    global _loop, _schedule, _quitting, _conf
    assert(_loop is None and _schedule is None)

    loop = asyncio.get_running_loop()
    schedule = Scheduler(loop=_loop, tzinfo=_tz)
    _loop, _schedule = loop, schedule
    logging.info('backup schedule inited')

    await _apply_conf()

    while not _quitting:
        await asyncio.sleep(_check_gap)


with open('/supra/conf/app/vm_backup_schedule.json', 'r') as f:
    backup_schedule_conf = json.load(f)
    logging.debug(f'backup_schdule conf {backup_schedule_conf}')
    _set_conf(**backup_schedule_conf)

asyncio.run(_main_loop())
