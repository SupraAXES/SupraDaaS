#!/usr/bin/env python3
import logging
import winrm


def run(adm_name, adm_passwd, script):
    s = winrm.Session('127.0.0.1', auth=(adm_name, adm_passwd))
    r = s.run_ps(script)
    ret = f'std_out: {r.std_out}, std_err: {r.std_err}'
    if r.status_code != 0:
        raise Exception(ret)
    return ret


if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    script = """$strComputer = $Host
Clear
$RAM = WmiObject Win32_ComputerSystem
$MB = 1048576

"Installed Memory: " + [int]($RAM.TotalPhysicalMemory /$MB) + " MB" """
    print(run('supra', 'supra', script))
