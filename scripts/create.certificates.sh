#!/bin/bash

export FOREMAN_URL='foreman.dummy.test'
export PUPPET_URL='puppet.dummy.test'

sudo -E docker run -h "$PUPPET_URL" -v /etc/puppetlabs/puppet/ssl:/etc/puppetlabs/puppet/ssl puppet/puppetserver:latest ca setup
sudo -E docker network create -d bridge foreman
# Start the instance
sudo -E docker run --network foreman -d --name puppet -h puppet.dummy.test -v /etc/puppetlabs/puppet/ssl:/etc/puppetlabs/puppet/ssl puppet/puppetserver:latest
sleep 15
sudo -E docker exec -t puppet puppetserver ca generate --certname foreman.dummy.test
# cleanup images
sudo -E docker rm -f puppet
sudo -E docker network rm foreman
# Create public facing public certificate
openssl req -new -newkey rsa:4096 -days 1 -nodes -x509 -subj "/C=SE/L=Gothenburg/CN=${FOREMAN_URL}" -keyout /tmp/foreman.key -out /tmp/foreman.crt