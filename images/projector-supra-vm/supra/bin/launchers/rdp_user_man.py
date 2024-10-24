#!/usr/bin/env python3
import logging
import winrm_ps


def new(adm_name, adm_passwd, name, passwd):
    if adm_name == name:
        raise Exception('rdp user cannot be adm user')
    script = f'''
New-LocalUser -Name "{name}" -NoPassword
Set-LocalUser -Name "{name}" -Password (ConvertTo-SecureString "{passwd}" -AsPlainText -Force) -PasswordNeverExpires $true -UserMayChangePassword $false
Add-LocalGroupMember -Group "Remote Desktop Users" -Member "{name}"
'''
    user_exist = f'User {name} already exists'
    try:
        winrm_ps.run(adm_name, adm_passwd, script)
    except Exception as e:
        if user_exist in f'{str(e)}':
            logging.debug(f'{str(e)}')
        else:
            raise


def remove(adm_name, adm_passwd, name):
    if adm_name == name:
        raise Exception('cannot remove itself')
    script = f'''
Remove-LocalUser -Name "{name}"
Remove-Item -Path "C:\\Users\\{name}" -Recurse -Force
'''
    user_not_found = f'User {name} was not found'
    try:
        winrm_ps.run(adm_name, adm_passwd, script)
    except Exception as e:
        if user_not_found in f'{str(e)}':
            logging.debug(f'{str(e)}')
        else:
            raise


if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    #print(remove('supra', 'supra', 'test-123'))
    #print(new('supra', 'supra', 'test-123', '1234567'))
    #print(new('supra', 'supra', 'test-123', '12345'))
    pass
