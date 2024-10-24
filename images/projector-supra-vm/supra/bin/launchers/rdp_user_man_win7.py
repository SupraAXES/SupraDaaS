#!/usr/bin/env python3
import logging
import winrm_ps


def _change_passwd(adm_name, adm_passwd, name, passwd):
    script = f'''
net user "{name}" "{passwd}"
'''
    winrm_ps.run(adm_name, adm_passwd, script)


def new(adm_name, adm_passwd, name, passwd):
    if adm_name == name:
        raise Exception('rdp user cannot be adm user')
    script = f'''
net user "{name}" "{passwd}" /add
net localgroup "Remote Desktop Users" {name} /add
net user "{name}" /passwordchg:no
wmic useraccount where name=`'{name}`' set passwordexpires=false
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
    #print(remove('supra', 'supra', 'supra'))
    #print(remove('supra', 'supra', 'rdp-user-noname'))
    #print(remove('supra', 'supra', 'test-123'))
    #print(new('supra', 'supra', 'test-123', '123456'))
    #print(new('supra', 'supra', 'test-123', '123457'))
    pass
