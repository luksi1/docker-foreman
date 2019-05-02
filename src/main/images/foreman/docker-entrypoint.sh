#!/bin/bash

set -e

# IGNORED_ENVIRONMENTS_FILE="/usr/share/foreman/config/ignored_environments.yml"

# In some environments, the reverse lookups performed on the default gateway
# will flood the DNS logs with a host not found.
# This simply adds a host entry, so that foreman can perform the reverse lookup
# without hitting the nameserver
# echo $(ip route | awk '{print $3}' | head -1) router >> /etc/hosts

# if [[ ! -z $IMPORT_ONLY_ROLES_AND_PROFILES ]]; then
#   echo ':ignored:' >> $IGNORED_ENVIRONMENTS_FILE
#   echo "  - !ruby/regexp '/^(?!role|profile).*$/'" >> $IGNORED_ENVIRONMENTS_FILE
# fi

# add TLS public certs so that they are trusted in your environment
cp /certs/client_ca.pem /usr/local/share/ca-certificates/client_ca.crt
cp /certs/server_cachain.pem /usr/local/share/ca-certificates/server_cachain.crt
if [ -d /certs/trusts ]; then
  cp /certs/trusts/*crt /usr/local/share/ca-certificates/
fi
update-ca-certificates

# confd configuration
/usr/local/bin/confd -onetime -backend env

while ! nc -z "${DATABASE_HOST}" "${DATABASE_PORT}"; do
  sleep 1
done

foreman-rake db:migrate
foreman-rake db:seed
foreman-rake apipie:cache:index

apachectl -d /etc/apache2 -f /etc/apache2/apache2.conf -e debug -DFOREGROUND
