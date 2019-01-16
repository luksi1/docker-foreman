#!/bin/sh

# Trust CA certs mounted to /usr/local/share/ca-certificates/*
update-ca-certificates

dockerize \
  -template /templates/settings.yml:/etc/foreman-proxy/settings.yml \
  -template /templates/puppet.yml:/etc/foreman-proxy/settings.d/puppet.yml \
  -template /templates/puppet.yml:/etc/foreman-proxy/settings.d/puppet_proxy_puppet_api.yml

# foreman needs to have rights to the SSL directory
RUN usermod -G puppet foreman-proxy

if [ -z "$@" ]; then
  exec ruby /usr/share/foreman-proxy/bin/smart-proxy
fi

exec "$@"
