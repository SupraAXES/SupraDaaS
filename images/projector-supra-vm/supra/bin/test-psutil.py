#!/usr/bin/env python3
import psutil
import time

t1 = time.perf_counter()

for proc in psutil.process_iter():
    if 'qemu' in proc.cmdline()[0]:
        print(f'get {proc.cmdline()}')

t2 = time.perf_counter()

print(f'cost: {t2-t1} sec')

