ARG FEDORA_RELEASE=30
ARG FOREMAN_RELEASE=1.23
FROM registry.fedoraproject.org/fedora-minimal:${FEDORA_RELEASE} as entrypoint
COPY --chown=1001 docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

FROM quay.io/foreman/foreman:${FOREMAN_RELEASE}-stable
LABEL org.label-schema.name="foreman" \
      org.label-schema.version=$FOREMAN_RELEASE \
      org.label-schema.vcs-url="https://github.com/luksi1/docker-foreman"
COPY --from=entrypoint --chown=1001 /docker-entrypoint.sh /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]

EXPOSE 3000

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