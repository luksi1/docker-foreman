ARG FOREMAN_RELEASE=1.23
FROM quay.io/foreman/foreman:${FOREMAN_RELEASE}-stable

ARG CONFD_VERSION=0.10.0

LABEL org.label-schema.name="foreman" \
      org.label-schema.version=$FOREMAN_RELEASE \
      org.label-schema.vcs-url="https://github.com/luksi1/docker-foreman" \
      org.lobal-schema.confd-version=$CONFD_VERSION

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

USER root

# Setup gosu for easier command execution
RUN gpg --keyserver pool.sks-keyservers.net --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4 \
    && curl -o /usr/local/bin/gosu -SL "https://github.com/tianon/gosu/releases/download/1.2/gosu-amd64" \
    && curl -o /usr/local/bin/gosu.asc -SL "https://github.com/tianon/gosu/releases/download/1.2/gosu-amd64.asc" \
    && gpg --verify /usr/local/bin/gosu.asc \
    && rm /usr/local/bin/gosu.asc \
    && rm -r /root/.gnupg/ \
    && chmod +x /usr/local/bin/gosu

# confd
ADD https://github.com/kelseyhightower/confd/releases/download/v${CONFD_VERSION}/confd-${CONFD_VERSION}-linux-amd64 /usr/local/bin/confd
RUN gosu root chmod +x /usr/local/bin/confd
COPY files/confd /etc/confd

COPY docker-entrypoint.sh /
RUN gosu root chmod +x /docker-entrypoint.sh

USER 1001

ENTRYPOINT ["/docker-entrypoint.sh"]

EXPOSE 443
EXPOSE 8443

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