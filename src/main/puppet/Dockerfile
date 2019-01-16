FROM puppet/puppetserver:latest

COPY files/* /etc/puppetlabs/puppet/
ADD https://raw.githubusercontent.com/theforeman/puppet-foreman/master/lib/puppet/parser/functions/foreman.rb /opt/puppetlabs/puppet/lib/ruby/vendor_ruby/puppet/reports/foreman.rb


