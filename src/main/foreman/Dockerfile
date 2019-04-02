FROM ubuntu:18.04

ARG FOREMAN_RELEASE

LABEL org.label-schema.maintainer="Luke Simmons <luksi1>" \
      org.label-schema.name="Foreman" \
      org.label-schema.license="Apache-2.0" \
      org.label-schema.version=$FOREMAN_RELEASE \
      org.label-schema.vcs-url="https://github.com/luksi1/docker-foreman" \
      org.label-schema.dockerfile="/Dockerfile"

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# hadolint ignore=DL3008
RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    wget \
    tzdata \
    locales \
    nscd \
    gnupg \
    && rm -rf /var/lib/apt/lists/*

# Enable en_US.utf8 for Foreman
# See: http://projects.theforeman.org/issues/13496
RUN sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && \
    echo 'LANG="en_US.UTF-8"'>/etc/default/locale && \
    dpkg-reconfigure --frontend=noninteractive locales && \
    update-locale LANG=en_US.UTF-8

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
    puppet-agent \
    foreman-installer \
    foreman-postgresql \
    puppet-agent-oauth \
    wget \
    grub-common \
    grub-efi-amd64-bin \
    foreman-proxy \
    tftpd-hpa \
    syslinux-common \
    pxelinux \
    libapache2-mod-passenger \
    mime-support \
    foreman-cli \
    cron \
    ruby-foreman-setup \
    apache2 \
    netcat \
    && rm -rf /var/lib/apt/lists/*

# confd
ADD https://github.com/kelseyhightower/confd/releases/download/v0.10.0/confd-0.10.0-linux-amd64 /usr/local/bin/confd
RUN chmod +x /usr/local/bin/confd
COPY files/confd /etc/confd

# copy in apache2 modules
COPY files/modules/mods-available/* /etc/apache2/mods-available/
RUN a2enmod access_compat \
    && a2enmod alias \
    && a2enmod auth_basic \
    && a2enmod authn_core \
    && a2enmod authn_file \
    && a2enmod authz_core \
    && a2enmod authz_groupfile \
    && a2enmod authz_host \
    && a2enmod authz_user \
    && a2enmod autoindex \
    && a2enmod cgid \
    && a2enmod dav_fs \
    && a2enmod dav \
    && a2enmod deflate \
    && a2enmod dir \
    && a2enmod env \
    && a2enmod filter \
    && a2enmod headers \
    && a2enmod mime \
    && a2enmod negotiation \
    && a2enmod passenger \
    && a2enmod reqtimeout \
    && a2enmod setenvif \
    && a2enmod socache_shmcb \
    && a2enmod ssl \
    && a2enmod worker \
    && a2enmod zpassenger \
    && a2dismod mpm_event \
    && a2dismod status

COPY docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]

EXPOSE 443

# server certificates used for external access
VOLUME /certs/server.crt
VOLUME /certs/server.key
# the certificate authority chain for your server certificate
VOLUME /certs/server_cachain.pem
# the client certificate authority. this will be used to
# trust client certificates. When using a Puppet smart proxy
# this will be the ca.pem of your puppet installation
VOLUME /certs/client_ca.pem
# the certificate revocation list for the client certificate
# when using puppet, this will be your Puppet CRL.
VOLUME /certs/client_crl.pem
# the client certificates. The will be used for example to
# perform mutual authentication with a smart proxy
VOLUME /certs/client.crt
VOLUME /certs/client.key
