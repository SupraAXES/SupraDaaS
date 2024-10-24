#!/usr/bin/env python3
import logging
import paramiko
import time


def _change_root_passwd(adm_passwd, passwd):
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    try:
        ssh.connect('127.0.0.1', port=5985, username='root', password=adm_passwd)
        stdin, stdout, stderr = ssh.exec_command('passwd')
        stdin.write(f'{passwd}\n')
        time.sleep(1)
        stdin.flush()
        stdin.write(f'{passwd}\n')
        time.sleep(1)
        stdin.flush()
    finally:
        ssh.close()

    time.sleep(1)
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    try:
        ssh.connect('127.0.0.1', port=5985, username='root', password=passwd)
    finally:
        ssh.close()


def new(adm_name, adm_passwd, name, passwd):
    if adm_name != 'root' or name != 'root':
        raise Exception('linux adm name must be root')
    return _change_root_passwd(adm_passwd, passwd)


def remove(adm_name, adm_passwd, name):
    raise Exception('linux adm cannot removed')


if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    #print(remove('supra', 'supra', 'adm-123'))
    #print(new('root', 'supra', 'root', '123456'))
    #print(new('root', '123456', 'root', 'supra'))
    pass
