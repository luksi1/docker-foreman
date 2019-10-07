#!/bin/bash

set -e

while [[ true ]]; do
  if [[ $(sudo docker inspect --format '{{json .State.Health.Status }}' puppetserver | sed s/\"//g | egrep "healthy") ]]; then
    break
  elif [[ $(sudo docker inspect --format '{{json .State.Health.Status }}' puppetserver | sed s/\"//g | egrep "starting") ]]; then
    echo "Starting"
  elif [[ $(sudo docker inspect --format '{{json .State.Health.Status }}' puppetserver | sed s/\"//g | egrep "unhealthy") ]]; then
    echo "Puppet has not started correctly"
    exit 1
  fi
  sleep 1
  echo "Puppet is $(sudo docker inspect --format '{{json .State.Health.Status }}' puppetserver | sed s/\"//g)"
done

# grab the container ID for Foreman
FOREMAN_CONTAINER_ID=$(/usr/bin/sudo /usr/bin/docker ps -qaf label=org.label-schema.name=foreman)
# grab password from foreman
ADMIN_PASSWORD=$(/usr/bin/sudo /usr/bin/docker exec -t "$FOREMAN_CONTAINER_ID" foreman-rake permissions:reset | /bin/grep 'Reset to user:' | /usr/bin/awk '{print $NF}' | tr -d '\r')
# swap out admin password with jq
jq ".auth.basic[0].value = \"${ADMIN_PASSWORD}\"" tests/foreman.json > tests/foreman.json.bak && mv tests/foreman.json.bak tests/foreman.json
# perform tests
newman run tests/foreman.json --insecure

