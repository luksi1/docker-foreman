FROM ubuntu:16.04

ARG FOREMAN_RELEASE=1.20
ARG FOREMAN_PACKAGE_VERSION=1.20.0-1

ENV DOCKERIZE_VERSION=v0.3.0 \
    FOREMAN_URL=foreman \
    TRUSTED_HOSTS=localhost,host1,host2 \

# Install dockerize
RUN apt-get update && \
    apt-get install -y ca-certificates wget && \
    wget -q https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz && \
    tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz && \
    rm dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Puppet needs UID/GID 999 as this is used as standard
# in puppet-in-docker containers
RUN groupadd -g 999 puppet
RUN useradd -ms /bin/bash -g puppet -u 999 puppet

# Enable Puppet repo
# This is simply done here to speed up the foreman-installer
RUN wget https://apt.puppetlabs.com/puppetlabs-release-pc1-xenial.deb
RUN dpkg -i puppetlabs-release-pc1-xenial.deb

# Install Foreman proxy
RUN wget -q https://deb.theforeman.org/pubkey.gpg -O- | apt-key add - && \
    echo "deb http://deb.theforeman.org/ xenial $FOREMAN_RELEASE" > /etc/apt/sources.list.d/foreman.list && \
    echo "deb http://deb.theforeman.org/ plugins $FOREMAN_RELEASE" >> /etc/apt/sources.list.d/foreman.list && \
    apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y -q \
      foreman-proxy=$FOREMAN_PACKAGE_VERSION \
      nscd \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Enable en_US.utf8 for Foreman
# See: http://projects.theforeman.org/issues/13496
RUN sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && \
    echo 'LANG="en_US.UTF-8"'>/etc/default/locale && \
    dpkg-reconfigure --frontend=noninteractive locales && \
    update-locale LANG=en_US.UTF-8

COPY docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh
COPY templates /
COPY Dockerfile /

EXPOSE 443
VOLUME /etc/puppetlabs/puppet/ssl/

ENTRYPOINT ["/docker-entrypoint.sh"]
