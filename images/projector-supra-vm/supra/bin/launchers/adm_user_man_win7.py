#!/usr/bin/env python3
import logging
import winrm_ps


def _change_passwd(adm_name, adm_passwd, name, passwd):
    script = f'''
net user "{name}" "{passwd}"
'''
    try:
        winrm_ps.run(adm_name, adm_passwd, script)
    except:
        pass
    winrm_ps.run(name, passwd, '$null')


def new(adm_name, adm_passwd, name, passwd):
    if adm_name == name:
        return _change_passwd(adm_name, adm_passwd, name, passwd)
    script = f'''
net user "{name}" "{passwd}" /add
wmic useraccount where name=`'{name}`' set passwordexpires=false
net localgroup "Remote Desktop Users" {name} /add
net localgroup "Administrators" {name} /add
'''
    user_exist = 'The account already exists'
    try:
        ret = winrm_ps.run(adm_name, adm_passwd, script)
    except Exception as e:
        if user_exist in f'{str(e)}':
            logging.debug(f'{str(e)}')
            return _change_passwd(adm_name, adm_passwd, name, passwd)
        else:
            raise
    if user_exist in ret:
        return _change_passwd(adm_name, adm_passwd, name, passwd)


def remove(adm_name, adm_passwd, name):
    if adm_name == name:
        raise Exception('cannot remove itself')
    script = f'''
net user "{name}" /delete
'''
    user_not_found = f'The user name could not be found'
    try:
        winrm_ps.run(adm_name, adm_passwd, script)
    except Exception as e:
        if user_not_found in f'{str(e)}':
            logging.debug(f'{str(e)}')
        else:
            raise


if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    #print(remove('supra', 'supra', 'adm-123'))
    #print(new('supra', 'supra', 'adm-123', '123456'))
    #print(new('supra', 'supra', 'adm-123', '1234567'))
    #print(new('adm-123', '123', 'adm-123', '123456'))
    pass
