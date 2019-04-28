FROM ruby:2.3-jessie

ARG R10K_VERSION

ENV PUPPET_AGENT_VERSION="1.8.3" UBUNTU_CODENAME="xenial" PREFIX="false" CACHEDIR="/var/cache/r10k" BASEDIR="/etc/puppetlabs/code/environments"

# install confd
ADD https://github.com/kelseyhightower/confd/releases/download/v0.10.0/confd-0.10.0-linux-amd64 /usr/local/bin/confd
RUN chmod +x /usr/local/bin/confd

# copy in docker-entrypoint.sh
COPY docker-entrypoint.sh /

RUN apt-get update && \
    apt-get install --no-install-recommends -y wget ca-certificates lsb-release && \
    # install puppet agent from the puppetlabs repository
    wget https://apt.puppetlabs.com/puppetlabs-release-pc1-"$UBUNTU_CODENAME".deb && \
    dpkg -i puppetlabs-release-pc1-"$UBUNTU_CODENAME".deb && \
    rm puppetlabs-release-pc1-"$UBUNTU_CODENAME".deb && \
    apt-get update && \
    apt-get install --no-install-recommends -y puppet-agent="$PUPPET_AGENT_VERSION"-1"$UBUNTU_CODENAME" && \
    apt-get remove --purge -y wget && \
    apt-get autoremove -y && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    # install r10k and the puppet webhook
    gem install r10k:${R10K_VERSION} puppet_webhook json && \
    # docker-entrypoint.sh needs to be executable
    chmod +x /docker-entrypoint.sh && \
    # create r10k directory
    # the default directory for r10k configuration files is
    # under /etc/puppetlabs/r10k
    mkdir -p /etc/puppetlabs/r10k

# copy in template files for confd
COPY files/confd /etc/confd
ENTRYPOINT ["/docker-entrypoint.sh"]

# This gives us access to the Puppet code directory. This will need
# to be read-write on the r10k side, while read-only on the Puppet server.
VOLUME /etc/puppetlabs/code/

# r10k web hook port
EXPOSE 8088
