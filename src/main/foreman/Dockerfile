FROM centos:7

MAINTAINER luksi1

ARG FOREMAN_RELEASE

# ENV DEBIAN_FRONTEND=noninteractive \
#     TZ=Europe/Stockholm \
#     # Gosu version. Gosu is used to run applications using a specific user.
#     GOSU_VERSION=1.9

# ENV HOSTNAME="dummy.hostname"
ENV FOREMAN_OPTS="--puppet-server=false --puppet-agent=false --foreman-proxy-puppet=false --foreman-proxy-puppetca=false --foreman-proxy-tftp=false --foreman-proxy-realm=false --foreman-proxy-dns=false --foreman-proxy-dhcp=false --foreman-proxy-bmc=false --puppet-puppetmaster=puppet --foreman-db-type=postgresql --foreman-db-manage=false --foreman-db-manage-rake=false --foreman-db-username=foreman --foreman-db-password=foreman --foreman-db-host=foreman-postgres --foreman-db-database=foreman --foreman-db-port=5433 --foreman-proxy-trusted-hosts=https://puppet.foobar.test --foreman-proxy-trusted-hosts=https://puppet --foreman-admin-username=admin --foreman-admin-password=admin --no-enable-foreman-proxy --no-enable-foreman-cli"

# Install gosuENV GOSU_VERSION 1.9
# RUN set -x \
#    && apt-get update && apt-get install -y --no-install-recommends ca-certificates wget tzdata locales nscd \
#    && wget -O /usr/local/bin/gosu "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$dpkgArch" \
#    && wget -O /usr/local/bin/gosu.asc "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$dpkgArch.asc" \
#    && export GNUPGHOME="$(mktemp -d)" \
#    && gpg --keyserver ha.pool.sks-keyservers.net --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4 \
#    && gpg --batch --verify /usr/local/bin/gosu.asc /usr/local/bin/gosu \
#    && rm -r "$GNUPGHOME" /usr/local/bin/gosu.asc \
#    && chmod +x /usr/local/bin/gosu \
#    && gosu nobody true

# RUN apt-get update && apt-get install -y --no-install-recommends ca-certificates wget tzdata locales gnupg

# Enable en_US.utf8 for Foreman
# See: http://projects.theforeman.org/issues/13496
# RUN sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && \
#     echo 'LANG="en_US.UTF-8"'>/etc/default/locale && \
#     dpkg-reconfigure --frontend=noninteractive locales && \
#     update-locale LANG=en_US.UTF-8

# Enable Puppet repo
# This is simply done here to speed up the foreman-installer
# RUN wget https://apt.puppetlabs.com/puppetlabs-release-pc1-xenial.deb
# RUN dpkg -i puppetlabs-release-pc1-xenial.deb
# RUN rpm -ivh https://yum.puppetlabs.com/puppetlabs-release-el-7.noarch.rpm
# RUN yum localinstall -y https://yum.puppetlabs.com/puppetlabs-release-el-7.noarch.rpm
RUN yum localinstall -y https://yum.puppet.com/puppet5/puppet5-release-el-7.noarch.rpm

# Puppet needs UID/GID 999 as this is used as standard
# in puppet-in-docker containers
# RUN groupadd -g 999 puppet
# RUN useradd -ms /bin/bash -g puppet -u 999 puppet

# Foreman Puppet run is built for RedHat. Add /var/www directory for use with Ubuntu
# RUN mkdir /var/www

# Install Foreman repository
RUN yum localinstall -y https://yum.theforeman.org/releases/$FOREMAN_RELEASE/el7/x86_64/foreman-release.rpm
RUN yum install -y centos-release-scl-rh
RUN yum install -y epel-release
RUN yum install -y foreman-release-scl
# Enable Foreman repo
# RUN echo deb http://deb.theforeman.org/ bionic $FOREMAN_RELEASE > /etc/apt/sources.list.d/foreman.list && \
#     echo deb http://deb.theforeman.org/ plugins $FOREMAN_RELEASE >> /etc/apt/sources.list.d/foreman.list && \
#     wget -q https://deb.theforeman.org/pubkey.gpg -O- | apt-key add - 

# RUN yum install -y puppet-agent foreman-installer foreman-postgresql puppet-agent-oauth foreman-proxy foreman-cli apache2
RUN yum install -y foreman foreman-selinux foreman-rake foreman-postgresql mailcap httpd puppet-agent mod_ssl mod_passenger tfm-rubygem-passenger-native foreman-installer
# RUN echo $(/opt/puppetlabs/bin/facter fqdn) > /etc/hostname && hostname -f && /opt/puppetlabs/bin/facter fqdn && foreman-installer $FOREMAN_OPTS || true

# confd
ADD https://github.com/kelseyhightower/confd/releases/download/v0.10.0/confd-0.10.0-linux-amd64 /usr/local/bin/confd
RUN chmod +x /usr/local/bin/confd
ADD files/confd /etc/confd

# create mandatory directories
RUN mkdir /certs
RUN mkdir /etc/httpd/conf.d/05-foreman.d
RUN mkdir /etc/httpd/conf.d/05-foreman-ssl.d

# copy in httpd modules
COPY files/modules/* /etc/httpd/conf.modules.d/

COPY docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]

EXPOSE 443

VOLUME /certs/public.crt
VOLUME /certs/private.key
VOLUME /certs/cachain.pem
VOLUME /certs/ca.pem
VOLUME /certs/crl.pem
