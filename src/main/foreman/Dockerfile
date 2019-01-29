FROM ubuntu:18.04

MAINTAINER luksi1

ARG FOREMAN_RELEASE

ENV DEBIAN_FRONTEND=noninteractive \
    GOSU_VERSION=1.9

# ENV HOSTNAME="dummy.hostname"
ENV FOREMAN_OPTS="--puppet-server=false --puppet-agent=false --foreman-proxy-puppet=false --foreman-proxy-puppetca=false --foreman-proxy-tftp=false --foreman-proxy-realm=false --foreman-proxy-dns=false --foreman-proxy-dhcp=false --foreman-proxy-bmc=false --puppet-puppetmaster=puppet --foreman-db-type=postgresql --foreman-db-manage=false --foreman-db-manage-rake=false --foreman-db-username=foreman --foreman-db-password=foreman --foreman-db-host=foreman-postgres --foreman-db-database=foreman --foreman-db-port=5433 --foreman-proxy-trusted-hosts=https://puppet.foobar.test --foreman-proxy-trusted-hosts=https://puppet --foreman-admin-username=admin --foreman-admin-password=admin --no-enable-foreman-proxy --no-enable-foreman-cli"

# Install gosuENV GOSU_VERSION 1.9
RUN apt-get update && apt-get install -y --no-install-recommends ca-certificates wget tzdata locales nscd gnupg

# Enable en_US.utf8 for Foreman
# See: http://projects.theforeman.org/issues/13496
RUN sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && \
    echo 'LANG="en_US.UTF-8"'>/etc/default/locale && \
    dpkg-reconfigure --frontend=noninteractive locales && \
    update-locale LANG=en_US.UTF-8

# Enable Puppet repo
# This is simply done here to speed up the foreman-installer
RUN wget https://apt.puppetlabs.com/puppetlabs-release-pc1-xenial.deb
RUN dpkg -i puppetlabs-release-pc1-xenial.deb

# Enable Foreman repo
RUN echo deb http://deb.theforeman.org/ bionic $FOREMAN_RELEASE > /etc/apt/sources.list.d/foreman.list && \
    echo deb http://deb.theforeman.org/ plugins $FOREMAN_RELEASE >> /etc/apt/sources.list.d/foreman.list && \
    wget -q https://deb.theforeman.org/pubkey.gpg -O- | apt-key add - 

# Download the installer
# Install all the packages that will be needed to install Foreman
# This will speed up the foreman-installer, removing the need to install
# necessary packages.
RUN apt-get update \
    && apt-get install -y puppet-agent foreman-installer foreman-postgresql puppet-agent-oauth wget grub-common grub-efi-amd64-bin foreman-proxy tftpd-hpa syslinux-common pxelinux libapache2-mod-passenger mime-support foreman-cli cron ruby-foreman-setup apache2 netcat

# confd
ADD https://github.com/kelseyhightower/confd/releases/download/v0.10.0/confd-0.10.0-linux-amd64 /usr/local/bin/confd
RUN chmod +x /usr/local/bin/confd
ADD files/confd /etc/confd

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

VOLUME /certs/public.crt
VOLUME /certs/private.key
VOLUME /certs/cachain.pem
VOLUME /certs/ca.pem
VOLUME /certs/crl.pem
