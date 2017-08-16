FROM puppet/puppetserver

MAINTAINER luksi1

ARG FOREMAN_VERSION

COPY foreman.rb /opt/puppetlabs/puppet/lib/ruby/vendor_ruby/puppet/reports/

# Enable Foreman repo
RUN echo deb http://deb.theforeman.org/ xenial $FOREMAN_VERSION > /etc/apt/sources.list.d/foreman.list \
  && echo deb http://deb.theforeman.org/ plugins $FOREMAN_VERSION >> /etc/apt/sources.list.d/foreman.list \
  && wget -q https://deb.theforeman.org/pubkey.gpg -O- | apt-key add - \ 
  && apt-get update \
  && apt-get install -y foreman-proxy netcat nscd

# enable foreman-proxy 
RUN sed -i '/:enabled/c\:enabled: https' /etc/foreman-proxy/settings.d/puppet.yml 
# node.rb for facts and enc
COPY node.rb /etc/puppetlabs/puppet/
RUN chmod +x /etc/puppetlabs/puppet/node.rb

# copy in API file
COPY puppet_proxy_puppet_api.yml /tmp/

# foreman-proxy needs to belong to the puppet group to have access to
# our SSL certs
RUN usermod -G puppet foreman-proxy

# Foreman-proxy settings
COPY settings.yml /etc/foreman-proxy/
COPY foreman.yaml /etc/puppetlabs/puppet/foreman.yaml

# Add a few entries in auth.conf for
# environment_classes GET request
# and resource_type GET and POST requests
COPY auth.conf /tmp/

# Overwrite the original docker-entrypoint.sh
COPY docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh

# Allow reports to be sent on the puppetmaster to foreman
RUN sed -i '/reports=/c\reports=log,foreman,puppetdb' /etc/puppetlabs/puppet/puppet.conf

# SSL certs needs to be shared across instances
# The important thing to remember is that puppet needs UID 999
VOLUME /etc/puppetlabs/puppet/ssl/
VOLUME /etc/puppetlabs/code/

# Puppet
EXPOSE 8140

# Foreman Proxy
# You'll need to expose this when using foreman on a different docker host
# When used on the same host, containers can speak to one another
EXPOSE 8443
