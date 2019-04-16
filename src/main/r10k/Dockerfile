FROM ruby:2.3.0

MAINTAINER Gareth Rushgrove "gareth@puppet.com"

ENV PUPPET_AGENT_VERSION="1.8.3" UBUNTU_CODENAME="xenial"

RUN apt-get update && \
    apt-get install --no-install-recommends -y wget ca-certificates lsb-release && \
    wget https://apt.puppetlabs.com/puppetlabs-release-pc1-"$UBUNTU_CODENAME".deb && \
    dpkg -i puppetlabs-release-pc1-"$UBUNTU_CODENAME".deb && \
    rm puppetlabs-release-pc1-"$UBUNTU_CODENAME".deb && \
    apt-get update && \
    apt-get install --no-install-recommends -y puppet-agent="$PUPPET_AGENT_VERSION"-1"$UBUNTU_CODENAME" && \
    apt-get remove --purge -y wget && \
    apt-get autoremove -y && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

ENV PATH=/opt/puppetlabs/server/bin:/opt/puppetlabs/puppet/bin:/opt/puppetlabs/bin:$PATH
# Install r10k web hook
RUN mkdir /modules
RUN git clone https://github.com/puppetlabs/puppetlabs-stdlib.git /modules/stdlib
RUN git clone https://github.com/puppetlabs/puppetlabs-ruby.git /modules/ruby
RUN git clone https://github.com/puppetlabs/puppetlabs-gcc.git /modules/gcc
RUN git clone https://github.com/puppetlabs/puppetlabs-inifile.git /modules/inifile
RUN git clone https://github.com/puppetlabs/puppetlabs-vcsrepo.git /modules/vscrepo
RUN git clone https://github.com/puppetlabs/puppetlabs-git.git /modules/git
RUN git clone https://github.com/voxpupuli/puppet-r10k.git /modules/r10k
COPY webhook.pp /
RUN mkdir /etc/mcollective
COPY server_private.pem /etc/mcollective/server_private.pem
COPY server_public.pem  /etc/mcollective/server_public.pem
COPY docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh
RUN ln -s /usr/local/bundle/bin/r10k /usr/local/bin/r10k

VOLUME /etc/puppetlabs/code/

ENTRYPOINT ["/docker-entrypoint.sh"]


# r10k web hook
EXPOSE 8088
