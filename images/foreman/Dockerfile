ARG FOREMAN_RELEASE=1.23
FROM quay.io/foreman/foreman:$FOREMAN_RELEASE

LABEL org.label-schema.name="foreman" \
      org.label-schema.version=$FOREMAN_RELEASE \
      org.label-schema.vcs-url="https://github.com/luksi1/docker-foreman"

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# confd
ADD https://github.com/kelseyhightower/confd/releases/download/v0.10.0/confd-0.10.0-linux-amd64 /usr/local/bin/confd
RUN chmod +x /usr/local/bin/confd
COPY files/confd /etc/confd

COPY docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh

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