#!/bin/bash

set -e

IGNORED_ENVIRONMENTS_FILE="/usr/share/foreman/config/ignored_environments.yml"
SSL_DIR="/etc/puppetlabs/puppet/ssl"
WEB_CERTS_DIR="$SSL_DIR/web_certs"

echo $(ip route | awk '{print $3}' | head -1) router >> /etc/hosts

if ! foreman-installer $FOREMAN_OPTS; then 
  foreman-rake db:migrate
  foreman-rake db:seed
  foreman-rake apipie:cache:index
fi

sed -i 's/START=no/START=yes/g' /etc/default/foreman 

if [[ ! -z $RESET_ADMIN_PASSWORD ]]; then
  foreman-rake permissions:reset > /tmp/accounts/admin
fi

if [[ ! -z $IMPORT_ONLY_ROLES_AND_PROFILES ]]; then
  echo ':ignored:' >> $IGNORED_ENVIRONMENTS_FILE
  echo "  - !ruby/regexp '/^(?!role|profile).*$/'" >> $IGNORED_ENVIRONMENTS_FILE
fi

# Add our own frontend cert
if [[ ! -z $FOREMAN_WEB_PUBLIC_CERT ]]; then
  sed -i "s|.*SSLCertificateFile.*|  SSLCertificateFile      \"/etc/puppetlabs/puppet/ssl/web_certs/${FOREMAN_WEB_PUBLIC_CERT}\"|" /etc/apache2/sites-enabled/05-foreman-ssl.conf
  sed -i "s|.*SSLCertificateKeyFile.*|  SSLCertificateKeyFile   \"/etc/puppetlabs/puppet/ssl/web_certs/${FOREMAN_WEB_PRIVATE_CERT}\"|" /etc/apache2/sites-enabled/05-foreman-ssl.conf
  sed -i "s|.*SSLCertificateChainFile.*|  SSLCertificateChainFile \"/etc/puppetlabs/puppet/ssl/web_certs/${FOREMAN_WEB_CA}\"|" /etc/apache2/sites-enabled/05-foreman-ssl.conf
fi

/etc/init.d/foreman start 
/etc/init.d/apache2 restart 

# Add variables to ignored_environments.yml
tail -f /var/log/foreman/production.log
