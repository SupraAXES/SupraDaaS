insert into ra_vm_template (id, name, os_type, disk_size, system_info, create_time, update_time) values ('ubuntu-2204-128GB', 'ubuntu-2204-128GB', 'ubuntu-2204', '128G', '{"userMgr":0,"adminName":"root","adminPasswd":"supra","users":[{"user":"supra","pass":"supra"}],"management":{"protocol":"rdp","port":"5985"},"access": [{"protocol": "ssh", "port": "22"}]}','2024-06-01 00:00:00', '2024-06-01 00:00:00');

insert into ra_vm_template (id, name, os_type, disk_size, system_info, create_time, update_time) values ('win10-2021-ltsc', 'win10-2021-ltsc', 'windows-10', '128G', '{"userMgr":1,"adminName":"supra","adminPasswd":"supra","users":[],"management":{"protocol":"rdp","port":"5985", "interval": 10},"access": [{"protocol": "rdp", "port": "3389"}]}','2024-06-01 00:00:00', '2024-06-01 00:00:00');

select * from ra_vm_template;
delete from ra_vm_templaqte;