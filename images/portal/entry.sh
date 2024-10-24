#!/bin/bash -e

check_cert() {
  if [ ! -f /opt/supra/cert/tls.crt ]; then
    echo "Certificate not found, generating a new one"
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout /opt/supra/cert/tls.key -out /opt/supra/cert/tls.crt -subj "/C=US/ST=Delaware/O=SupraAXES/CN=www.supraaxes.com"
  fi
}

launch_proxy() {

  check_cert 

  echo "Launching nginx"
  /usr/local/nginx/sbin/nginx &
}

# check if /opt/supra/data/tmp/ directory exists, create it if not
if [ ! -d "/opt/supra/data/tmp/" ]; then
  mkdir -p /opt/supra/data/tmp/
fi

# check if /opt/supra/data/files/ directory exists, create it if not
if [ ! -d "/opt/supra/data/files/" ]; then
  mkdir -p /opt/supra/data/files/
fi

# check if /opt/supra/logs/ directory exists, create it if not
if [ ! -d "/opt/supra/logs/" ]; then
  mkdir -p /opt/supra/logs/
fi


launch_proxy

# Wait for any process to exit
wait -n


# Exit with status of process that exited first
exit $?