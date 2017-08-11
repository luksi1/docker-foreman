FROM ubuntu:16.04

MAINTAINER luksi1

ENV DEBIAN_FRONTEND=noninteractive \
    TZ=Europe/Stockholm \
    # Gosu version. Gosu is used to run applications using a specific user.
    GOSU_VERSION=1.9

# Install gosuENV GOSU_VERSION 1.9
RUN set -x \
    && apt-get update && apt-get install -y --no-install-recommends ca-certificates wget tzdata locales nscd \
    && dpkgArch="$(dpkg --print-architecture | awk -F- '{ print $NF }')" \
    && wget -O /usr/local/bin/gosu "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$dpkgArch" \
    && wget -O /usr/local/bin/gosu.asc "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$dpkgArch.asc" \
    && export GNUPGHOME="$(mktemp -d)" \
    && gpg --keyserver ha.pool.sks-keyservers.net --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4 \
    && gpg --batch --verify /usr/local/bin/gosu.asc /usr/local/bin/gosu \
    && rm -r "$GNUPGHOME" /usr/local/bin/gosu.asc \
    && chmod +x /usr/local/bin/gosu \
    && gosu nobody true

# Enable en_US.utf8 for Foreman
# See: http://projects.theforeman.org/issues/13496
RUN echo "Europe/Stockholm" > /etc/timezone && \
    dpkg-reconfigure -f noninteractive tzdata && \
    sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && \
    echo 'LANG="en_US.UTF-8"'>/etc/default/locale && \
    dpkg-reconfigure --frontend=noninteractive locales && \
    update-locale LANG=en_US.UTF-8

# Enable Puppet repo
# This is simply done here to speed up the foreman-installer
RUN wget https://apt.puppetlabs.com/puppetlabs-release-pc1-xenial.deb
RUN dpkg -i puppetlabs-release-pc1-xenial.deb

# Puppet needs UID/GID 999 as this is used as standard
# in puppet-in-docker containers
RUN groupadd -g 999 puppet
RUN useradd -ms /bin/bash -g puppet -u 999 puppet

# Enable Foreman repo
RUN echo "deb http://deb.theforeman.org/ xenial 1.14" > /etc/apt/sources.list.d/foreman.list && \
    echo "deb http://deb.theforeman.org/ plugins 1.14" >> /etc/apt/sources.list.d/foreman.list && \
    wget -q https://deb.theforeman.org/pubkey.gpg -O- | apt-key add - 

# Download the installer
# Install all the packages that will be needed to install Foreman
# This will speed up the foreman-installer, removing the need to install
# necessary packages.
RUN apt-get update \
  && apt-get install -y puppet-agent foreman-installer foreman-postgresql puppet-agent-oauth wget grub-common grub-efi-amd64-bin foreman-proxy tftpd-hpa syslinux-common pxelinux libapache2-mod-passenger mime-support foreman-cli cron ruby-foreman-setup apache2 netcat

COPY docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh

VOLUME /etc/puppetlabs/puppet/ssl/

CMD /docker-entrypoint.sh

#Foreman
EXPOSE 443
