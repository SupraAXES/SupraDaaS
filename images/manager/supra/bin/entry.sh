#!/bin/bash

# nginx settings
if [ ! -d /supra/logs ]; then
    mkdir -p /supra/logs
fi

# create self-signed certificate if the key file and cert file do not exist
if [ ! -d /supra/conf/cert ]; then
    mkdir -p /supra/conf/cert
fi

if [ ! -f /supra/conf/cert/tls.key ] || [ ! -f /supra/conf/cert/tls.crt ]; then
    echo "create self-signed certificate..."
    openssl req -x509 -newkey rsa:4096 -keyout /supra/conf/cert/tls.key -out /supra/conf/cert/tls.crt -days 365 -nodes -subj "/C=US/ST=Delaware/O=SupraAXES/CN=www.supraaxes.com"
fi

# conf locations in /supra/conf/nginx.d
if [ ! -d /supra/conf/nginx.d ]; then
    mkdir -p /supra/conf/nginx.d
fi
nginx -c /supra/conf/nginx.conf &


# run app
exec app.sh
