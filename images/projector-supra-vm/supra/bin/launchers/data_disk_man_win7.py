#!/usr/bin/env python3
import logging
import winrm_ps


def init_and_format_all_raw(adm_name, adm_passwd):
    script ='''
# Get all raw disks
$rawDisks = Get-WmiObject -Class Win32_DiskDrive | Where-Object { $_.Partitions -eq 0 }

# For each raw disk
foreach ($disk in $rawDisks) {
    $diskNumber = $disk.Index
    if ("$diskNumber" -eq '') {
        continue
    }
    # diskLetter grows from D
    $diskLetter = [char]([int][char]'C' + $diskNumber)

    # DiskPart commands
    $commands = @"
    select disk $diskNumber
    online disk
    attributes disk clear readonly
    clean
    convert mbr
    create partition primary
    select partition 1
    active
    format fs=ntfs quick
    assign letter $diskLetter
"@

    # Run DiskPart with the commands
    $commands | diskpart
}
'''
    return winrm_ps.run(adm_name, adm_passwd, script)


def try_init_and_format(adm_name, adm_passwd, data_disk_idx):
    disk_idx = data_disk_idx + 1
    script = f'''
# Get all raw disks
$rawDisks = Get-WmiObject -Class Win32_DiskDrive | Where-Object {{ $_.Partitions -eq 0 }}

# For each raw disk
foreach ($disk in $rawDisks) {{
    $diskNumber = $disk.Index
    if ("$diskNumber" -eq '') {{
        continue
    }}
    if ($diskNumber -ne {disk_idx}) {{
        continue
    }}
    $diskLetter = [char]([int][char]'C' + $diskNumber)

    # DiskPart commands
    $commands = @"
    select disk $diskNumber
    online disk
    attributes disk clear readonly
    clean
    convert mbr
    create partition primary
    select partition 1
    active
    format fs=ntfs quick
    assign letter $diskLetter
"@

    # Run DiskPart with the commands
    $commands | diskpart
}}
'''
    return winrm_ps.run(adm_name, adm_passwd, script)


if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    #print(try_init_and_format('supra', 'supra', 4))
    #print(try_init_and_format('supra', 'supra', 0))
    #print(init_and_format_all_raw('supra', 'supra'))
    pass
