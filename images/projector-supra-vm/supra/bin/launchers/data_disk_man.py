#!/usr/bin/env python3
import logging
import winrm_ps


def init_and_format_all_raw(adm_name, adm_passwd):
    script ='''
Get-Disk | Where-Object PartitionStyle -Eq "RAW" | Initialize-Disk -PassThru | New-Partition -AssignDriveLetter -UseMaximumSize | Format-Volume
'''
    return winrm_ps.run(adm_name, adm_passwd, script)


def try_init_and_format(adm_name, adm_passwd, data_disk_idx):
    disk_idx = data_disk_idx + 1
    script = f'''
$disk = Get-Disk -Number {disk_idx}
if ($disk.PartitionStyle -eq "RAW") {{
    Write-Output "Disk is not initialized. initialized it."
    $disk = Initialize-Disk -InputObject $disk -PassThru
}}

$partitions = Get-Partition -Disk $disk | Where-Object -FilterScript {{$_.Type -Eq "Basic"}}
if (-not $partitions) {{
    Write-Output 'No Partitions. new-partition it.'
    $partition = New-Partition -InputObject $disk -AssignDriveLetter -UseMaximumSize
}}
else {{
    $partition = $partitions[0]
}}

$volume = Get-Volume -Partition $partition
if ($volume.FileSystem -eq '') {{
    Write-Output 'No FileSystem. format it.'
    Format-Volume -Partition $partition -FileSystem NTFS -Confirm:$false
}}
#$volume | ConvertTo-Json
'''
    return winrm_ps.run(adm_name, adm_passwd, script)


if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    #print(try_init_and_format('supra', 'supra', 0))
    #print(try_init_and_format('supra', 'supra', 1))
    #print(init_and_format_all_raw('supra', 'supra'))
    pass
