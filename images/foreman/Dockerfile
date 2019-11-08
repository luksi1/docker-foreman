ARG FEDORA_RELEASE=30
ARG FOREMAN_RELEASE=1.23
FROM registry.fedoraproject.org/fedora-minimal:${FEDORA_RELEASE} as entrypoint
COPY --chown=1001 docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

FROM registry.fedoraproject.org/fedora-minimal:${FEDORA_RELEASE} as healthcheck
COPY --chown=1001 healthcheck.sh /
RUN chmod +x /healthcheck.sh

FROM quay.io/foreman/foreman:${FOREMAN_RELEASE}-stable
LABEL org.label-schema.name="foreman" \
      org.label-schema.version=$FOREMAN_RELEASE \
      org.label-schema.vcs-url="https://github.com/luksi1/docker-foreman"
COPY --from=entrypoint --chown=1001 /docker-entrypoint.sh /docker-entrypoint.sh
COPY --from=healthcheck --chown=1001 /healthcheck.sh /healthcheck.sh
HEALTHCHECK --interval=15s --timeout=15s --retries=12 --start-period=1m CMD ["/healthcheck.sh"]
ENTRYPOINT ["/docker-entrypoint.sh"]

EXPOSE 3000