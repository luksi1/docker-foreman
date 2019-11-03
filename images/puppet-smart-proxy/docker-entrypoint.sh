#!/bin/sh

# add TLS public certs so that they are trusted in your environment
cp /certs/ca.pem /usr/local/share/ca-certificates/my.crt
if [ -d /certs/trusts ]; then
  cp /certs/trusts/*crt /usr/local/share/ca-certificates/
fi
update-ca-certificates
/usr/local/bin/confd -onetime -backend env

while ! nc -z foreman 3000; do
  sleep 1
done

ruby /usr/share/foreman-proxy/bin/smart-proxy
