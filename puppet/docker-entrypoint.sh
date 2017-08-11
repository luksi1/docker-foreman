#!/bin/bash

PUPPET_PROXY_PUPPET_API="/etc/foreman-proxy/settings.d/puppet_proxy_puppet_api.yml"
SETTINGS_YML="/etc/foreman-proxy/settings.yml"
FOREMAN_YAML="/etc/puppetlabs/puppet/foreman.yaml"

# Puppet should own all SSL certs
chown -R puppet:puppet /etc/puppetlabs/puppet/ssl
chown -R puppet:puppet /opt/puppetlabs/server/data/puppetserver/

# foreman-proxy needs to have access to all the SSL directories
usermod -G puppet foreman-proxy

# add hosts entry for reverse lookup
getent hosts $FOREMAN_HOSTNAME >> /etc/hosts
getent hosts ${FOREMAN_HOSTNAME}.${DOMAIN} >> /etc/hosts
echo $(ip route | awk '{print $3}' | head -1) router >> /etc/hosts

# Foreman smart proxy API settings
cp /tmp/puppet_proxy_puppet_api.yml $PUPPET_PROXY_PUPPET_API

# Enable Puppet CA
sed -i "s|:enabled.*|:enabled: true|" /etc/foreman-proxy/settings.d/puppetca.yml

sed -i "s|:puppet_url.*|:puppet_url: https://${PUPPET_HOSTNAME}.${DOMAIN}:8140|" $PUPPET_PROXY_PUPPET_API
sed -i "s|:puppet_ssl_key.*|:puppet_ssl_key: /etc/puppetlabs/puppet/ssl/private_keys/${PUPPET_HOSTNAME}.${DOMAIN}.pem|" $PUPPET_PROXY_PUPPET_API
sed -i "s|:puppet_ssl_cert.*|:puppet_ssl_cert: /etc/puppetlabs/puppet/ssl/certs/${PUPPET_HOSTNAME}.${DOMAIN}.pem|" $PUPPET_PROXY_PUPPET_API

# Apply settings to settings.yaml
sed -i "s|:ssl_certificate.*|:ssl_certificate: /etc/puppetlabs/puppet/ssl/certs/${PUPPET_HOSTNAME}.${DOMAIN}.pem|" $SETTINGS_YML
sed -i "s|:ssl_private_key.*|:ssl_private_key: /etc/puppetlabs/puppet/ssl/private_keys/${PUPPET_HOSTNAME}.${DOMAIN}.pem|" $SETTINGS_YML
sed -i "s|:foreman_url.*|:foreman_url: https://${FOREMAN_HOSTNAME}|" $SETTINGS_YML
sed -i "s|FOREMAN_HOSTNAME_AND_DOMAIN|${FOREMAN_HOSTNAME}\.${DOMAIN}|" $SETTINGS_YML
sed -i "s|FOREMAN_HOSTNAME|${FOREMAN_HOSTNAME}|" $SETTINGS_YML

# Apply settings to foreman.yaml
sed -i "s|:ssl_cert.*|:ssl_cert: /etc/puppetlabs/puppet/ssl/certs/${PUPPET_HOSTNAME}.${DOMAIN}.pem|" $FOREMAN_YAML
sed -i "s|:ssl_key.*|:ssl_key: /etc/puppetlabs/puppet/ssl/private_keys/${PUPPET_HOSTNAME}.${DOMAIN}.pem|" $FOREMAN_YAML
sed -i "s|:url.*|:url: \"https://${FOREMAN_HOSTNAME}.${DOMAIN}\"|" $FOREMAN_YAML
if [[ ! -z $FOREMAN_WEB_CA ]]; then 
  sed -i "s|:ssl_ca.*|:ssl_ca: /etc/puppetlabs/puppet/ssl/web_certs/${FOREMAN_WEB_CA}|" $FOREMAN_YAML
fi

if [[ ! -z $MAX_ACTIVE_INSTANCES ]]; then
  sed -i "s|.*#max-active-instances:.*|    max-active-instances: ${MAX_ACTIVE_INSTANCES}|" /etc/puppetlabs/puppetserver/conf.d/puppetserver.conf
fi

# Apply autosign.conf
if [[ ! -z $AUTOSIGN ]]; then
  echo $AUTOSIGN > /etc/puppetlabs/puppet/autosign.conf
fi

# Start proxy
/etc/init.d/foreman-proxy start

# Add a few authorization lines for foreman
cp /tmp/auth.conf /etc/puppetlabs/puppetserver/conf.d/auth.conf 

# Add reporting for foreman
sed -i '/reports =/c\reports = log,foreman,puppetdb' /etc/puppetlabs/puppet/puppet.conf

# Add Foreman ENC
echo "external_nodes = /etc/puppetlabs/puppet/node.rb" >> /etc/puppetlabs/puppet/puppet.conf
echo "node_terminus = exec" >> /etc/puppetlabs/puppet/puppet.conf


if test -n "${PUPPETDB_SERVER_URLS}" ; then
  sed -i "s@^server_urls.*@server_urls = ${PUPPETDB_SERVER_URLS}@" /etc/puppetlabs/puppet/puppetdb.conf
fi

while ! nc -z foreman 443; do
  sleep 1
done

exec /opt/puppetlabs/bin/puppetserver "$@"

