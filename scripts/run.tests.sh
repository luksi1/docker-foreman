#!/bin/bash

set -e

puppet_health_status() {
  sudo docker inspect --format '{{json .State.Health.Status }}' puppetserver | sed s/\"//g
}

while true
do
  if puppet_health_status | egrep -q "healthy"
  then
    echo "Puppetserver's health is \"$(puppet_health_status)\""
    break
  elif puppet_health_status | egrep -q "unhealthy"
  then
    echo "Puppetserver has not started correctly. Puppetserver's health is $(puppet_health_status)"
    exit 1
  fi
  sleep 5
  echo "Puppetserver's health is \"$(puppet_health_status)\""
done

while true
do
  if [[ ! $(nc -z foreman.dummy.test 443) ]]; then
    echo "Foreman has started"
    sleep 60
    break
  else
    echo "Waiting for Foreman to come up..."
    sleep 5
    continue
  fi
done

# grab the container ID for Foreman
FOREMAN_CONTAINER_ID=$(/usr/bin/sudo /usr/bin/docker ps -qaf label=org.label-schema.name=foreman)
# grab password from foreman
ADMIN_PASSWORD=$(/usr/bin/sudo /usr/bin/docker exec -t "$FOREMAN_CONTAINER_ID" foreman-rake permissions:reset | /bin/grep 'Reset to user:' | /usr/bin/awk '{print $NF}' | tr -d '\r')
# swap out admin password with jq
jq ".auth.basic[0].value = \"${ADMIN_PASSWORD}\"" tests/foreman.json > tests/foreman.json.bak && mv tests/foreman.json.bak tests/foreman.json
# perform tests
newman run tests/foreman.json --insecure

