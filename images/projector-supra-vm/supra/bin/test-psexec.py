#!/usr/bin/env python3
import subprocess
import time

#cmd = 'psexec.py admin_name:admin_passwd@127.0.0.1 "shutdown /s /t 15" -path c:\\\\windows\\\\system32\\\\'
cmd = 'psexec.py admin_name:admin_passwd@127.0.0.1 "hostname" -path c:\\\\windows\\\\system32\\\\'
print(cmd)

process = subprocess.Popen([cmd], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, shell=True)

time.sleep(1)
try:
    process.stdin.write('\n')
    process.stdin.flush()
except:
    pass

process.wait()

print(process.returncode)
