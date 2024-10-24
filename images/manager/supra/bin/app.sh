#!/bin/bash
check_backup() {
    . restic-env.sh

    # check if backup folder exists
    if [ ! -d "$RESTIC_REPOSITORY" ]; then
        echo "Backup folder does not exist. Creating..."
        mkdir -p $RESTIC_REPOSITORY
        chown -R supra:supra $RESTIC_REPOSITORY
    fi

    # check if backup config exists
    if [ ! -f "/supra/conf/app/vm_backup_schedule.json" ]; then
        echo "Backup config does not exist. Creating..."
        cp /supra/conf/vm_backup_schedule_default.json /supra/conf/app/vm_backup_schedule.json
    fi
}

mkdir -p /vm-storage/template
chown -R supra:supra /vm-storage/template
mkdir -p /vm-storage/machine
chown -R supra:supra /vm-storage/machine

check_backup
rsyslogd
app-vm-runner.py &
runuser -u supra -- app.py &
runuser -u supra -- app-vm-backup-schedule.py &
wait -n
