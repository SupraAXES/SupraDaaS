#!/usr/bin/env python3
import winrm
import time

t1 = time.perf_counter()

s = winrm.Session('127.0.0.1', auth=('supra', 'supra'))
r = s.run_cmd('sc query TermService')
#r = s.run_cmd('shutdown /s /f /t 0')

t2 = time.perf_counter()

print(f'cost: {t2-t1} sec')
print(f'status_code: {r.status_code}')
print(r.std_out.decode('utf-8'))

