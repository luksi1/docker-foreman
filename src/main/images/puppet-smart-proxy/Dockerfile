FROM ubuntu:18.04

ARG FOREMAN_RELEASE

LABEL org.label-schema.maintainer="Luke Simmons <luksi1>" \
      org.label-schema.name="Puppet Smart-Proxy" \
      org.label-schema.license="Apache-2.0" \
      org.label-schema.version=$FOREMAN_RELEASE \
      org.label-schema.vcs-url="https://github.com/luksi1/docker-foreman" \
      org.label-schema.dockerfile="/Dockerfile"

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# hadolint ignore=DL3008
RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    tzdata \
    locales \
    wget \
    nscd \
    gnupg \
    netcat \
    && rm -rf /var/lib/apt/lists/*

# Enable en_US.utf8 for Foreman
# See: http://projects.theforeman.org/issues/13496
RUN sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && \
    echo 'LANG="en_US.UTF-8"'>/etc/default/locale && \
    dpkg-reconfigure --frontend=noninteractive locales && \
    update-locale LANG=en_US.UTF-8 && \
    # enable certs directory
    mkdir /certs

# Enable Puppet repo
# This is simply done here to speed up the foreman-installer
ADD https://apt.puppetlabs.com/puppetlabs-release-pc1-xenial.deb /tmp/puppetlabs.deb
# Enable Foreman repo
# hadolint ignore=DL3008
RUN dpkg -i /tmp/puppetlabs.deb && \
# Download the installer
# Install all the packages that will be needed to install Foreman
# This will speed up the foreman-installer, removing the need to install
# necessary packages.
    echo "deb http://deb.theforeman.org/ bionic ${FOREMAN_RELEASE}" > /etc/apt/sources.list.d/foreman.list && \
    echo "deb http://deb.theforeman.org/ plugins ${FOREMAN_RELEASE}" >> /etc/apt/sources.list.d/foreman.list && \
    wget -q https://deb.theforeman.org/pubkey.gpg -O- | apt-key add -  && \
    apt-get update && apt-get install -y --no-install-recommends \
    foreman-proxy puppet-agent \
    && rm -rf /var/lib/apt/lists/*

# confd
ADD https://github.com/kelseyhightower/confd/releases/download/v0.10.0/confd-0.10.0-linux-amd64 /usr/local/bin/confd
RUN chmod +x /usr/local/bin/confd
COPY files/confd /etc/confd

COPY docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]

EXPOSE 8443
VOLUME /certs/ca.pem
VOLUME /certs/puppet.crt
VOLUME /certs/private.key
