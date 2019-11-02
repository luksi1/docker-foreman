ARG FEDORA_RELEASE=30

FROM registry.fedoraproject.org/fedora-minimal:${FEDORA_RELEASE} as gosu
ADD https://github.com/tianon/gosu/releases/download/1.2/gosu-amd64 /usr/local/bin/gosu
RUN chmod +x /usr/local/bin/gosu

FROM registry.fedoraproject.org/fedora-minimal:${FEDORA_RELEASE} as confd
ARG CONFD_VERSION="0.10.0"
ADD https://github.com/kelseyhightower/confd/releases/download/v${CONFD_VERSION}/confd-${CONFD_VERSION}-linux-amd64 /usr/local/bin/confd
RUN chmod +x /usr/local/bin/confd
COPY files/confd /etc/confd

FROM registry.fedoraproject.org/fedora-minimal:${FEDORA_RELEASE} as entrypoint
COPY --chown=1001 docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x "${HOME}/docker-entrypoint.sh"

FROM quay.io/foreman/foreman:${FOREMAN_RELEASE}-stable
ARG FOREMAN_RELEASE=1.23
LABEL org.label-schema.name="foreman" \
      org.label-schema.version=$FOREMAN_RELEASE \
      org.label-schema.vcs-url="https://github.com/luksi1/docker-foreman" \
      org.lobal-schema.confd-version=$CONFD_VERSION

SHELL ["/bin/bash", "-o", "pipefail", "-c"]
COPY --from=entrypoint /docker-entrypoint.sh /${HOME}/docker-entrypoint.sh
COPY --from=confd /usr/local/bin/confd /usr/local/bin/confd
COPY --from=gosu /usr/local/bin/gosu /usr/local/bin/gosu
ENTRYPOINT ["${HOME}/docker-entrypoint.sh"]

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