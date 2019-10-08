#!/bin/bash

set -e

export FOREMAN_URL='foreman.dummy.test'
export PUPPET_URL='puppet.dummy.test'
export PUPPET_SMARTPROXY_URL='puppet-smart-proxy.dummy.test'

puppet_health_status() {
  sudo docker inspect --format '{{json .State.Health.Status }}' puppetserver | sed s/\"//g
}

sudo -E docker run -h "$PUPPET_URL" -v /etc/puppetlabs/puppet/ssl:/etc/puppetlabs/puppet/ssl puppet/puppetserver:latest ca setup
sudo -E docker network create -d bridge foreman
# Start the instance
sudo -E docker run --network foreman -d --name puppetserver -h puppet.dummy.test -v /etc/puppetlabs/puppet/ssl:/etc/puppetlabs/puppet/ssl puppet/puppetserver:latest

while true; do
  if [[ $(echo $(puppet_health_status) | egrep "healthy") ]]; then
    echo "Puppetserver's health is \"$(puppet_health_status)\""
    # echo "Sleeping one minute for Foreman to come up."
    break
  elif [[ $(echo $(puppet_health_status) | egrep -q "unhealthy") ]]; then
    echo "Puppetserver has not started correctly. Puppetserver's health is $(puppet_health_status)"
    exit 1
  fi
  sleep 5
  echo "Puppetserver's health is \"$(puppet_health_status)\""
done

# Generate a foreman certificate signed by puppet
sudo -E docker exec -t puppetserver puppetserver ca generate --certname "$FOREMAN_URL"
# Generate a smart proxy certificate signed by puppet
sudo -E docker exec -t puppetserver puppetserver ca generate --certname "$PUPPET_SMARTPROXY_URL"
# Generate a puppetdb certificate signed by puppet
# sudo -E docker exec -t puppet puppetserver ca generate --certname "puppetdb"
# cleanup images
sudo -E docker rm -f puppetserver
sudo -E docker network rm foreman
# Create public facing public certificate
openssl req -new -newkey rsa:4096 -days 1 -nodes -x509 -subj "/C=SE/L=Gothenburg/CN=${FOREMAN_URL}" -keyout /tmp/foreman.key -out /tmp/foreman.crt