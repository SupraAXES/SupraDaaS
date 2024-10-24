#!/usr/bin/env python3
import winrm

ps_script = """$strComputer = $Host
Clear
$RAM = WmiObject Win32_ComputerSystem
$MB = 1048576

"Installed Memory: " + [int]($RAM.TotalPhysicalMemory /$MB) + " MB" """

s = winrm.Session('127.0.0.1', auth=('supra', 'supra'))
r = s.run_ps(ps_script)

print(r.status_code)
print(r.std_out)
print(r.std_err)
