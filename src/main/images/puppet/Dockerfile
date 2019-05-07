FROM puppet/puppetserver:6.2.1

LABEL org.label-schema.name="puppetserver" \
      org.label-schema.vcs-url="https://github.com/luksi1/docker-foreman"

COPY files/* /etc/puppetlabs/puppet/
ADD https://raw.githubusercontent.com/theforeman/puppet-foreman/master/lib/puppet/parser/functions/foreman.rb /opt/puppetlabs/puppet/lib/ruby/vendor_ruby/puppet/reports/foreman.rb

