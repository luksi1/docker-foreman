FROM puppet/puppetserver:6.3.1

LABEL org.label-schema.name="puppetserver" \
      org.label-schema.vcs-url="https://github.com/luksi1/docker-foreman"

# Set the default Foreman web CA certificate
# But allow it to be overidden
ENV FOREMAN_WEB_CA_CERTIFICATE="/etc/puppetlabs/puppet/ssl/certs/ca.pem"
ENV AUTOSIGN="true"

# foreman.rb
ADD https://raw.githubusercontent.com/theforeman/puppet-foreman/master/files/foreman-report_v2.rb  /opt/puppetlabs/puppet/lib/ruby/vendor_ruby/puppet/reports/foreman.rb

# confd
ADD https://github.com/kelseyhightower/confd/releases/download/v0.10.0/confd-0.10.0-linux-amd64 /usr/local/bin/confd
COPY files/confd /etc/confd
COPY files/99-run.confd.sh /docker-entrypoint.d/
RUN chmod +x /docker-entrypoint.d/99-run.confd.sh \
    && chmod +x /usr/local/bin/confd \
    && mkdir -p /etc/puppetlabs/foreman/ssl/ \
    && mkdir -p /etc/puppetlabs/puppet/keys \
    && chmod 0666 /opt/puppetlabs/puppet/lib/ruby/vendor_ruby/puppet/reports/foreman.rb
