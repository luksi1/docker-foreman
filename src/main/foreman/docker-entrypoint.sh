#!/bin/bash

set -e

# IGNORED_ENVIRONMENTS_FILE="/usr/share/foreman/config/ignored_environments.yml"
# SSL_DIR="/etc/puppetlabs/puppet/ssl"
# WEB_CERTS_DIR="$SSL_DIR/web_certs"

# In some environments, the reverse lookups performed on the default gateway
# will flood the DNS logs with a host not found.
# This simply adds a host entry, so that foreman can perform the reverse lookup
# without hitting the nameserver
# echo $(ip route | awk '{print $3}' | head -1) router >> /etc/hosts

# if ! foreman-installer $FOREMAN_OPTS; then 
#  foreman-rake db:migrate
#  foreman-rake db:seed
#  foreman-rake apipie:cache:index
# fi

# foreman-installer $FOREMAN_OPTS || :

# foreman-installer $FOREMAN_OPTS

# sed -i 's/START=no/START=yes/g' /etc/default/foreman 

# if [[ ! -z $RESET_ADMIN_PASSWORD ]]; then
#   foreman-rake permissions:reset > /tmp/accounts/admin
# fi

# if [[ ! -z $IMPORT_ONLY_ROLES_AND_PROFILES ]]; then
#   echo ':ignored:' >> $IGNORED_ENVIRONMENTS_FILE
#   echo "  - !ruby/regexp '/^(?!role|profile).*$/'" >> $IGNORED_ENVIRONMENTS_FILE
# fi

# conf configuration
/usr/local/bin/confd -onetime -backend env

foreman-rake db:migrate
foreman-rake db:seed
foreman-rake apipie:cache:index
# foreman-rake permission:reset PASSWORD=${ADMIN_PASSWORD}

apachectl -d /etc/apache2 -f /etc/apache2/apache2.conf -e debug -DFOREGROUND
