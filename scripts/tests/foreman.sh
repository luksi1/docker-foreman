#!/bin/bash

# shellcheck disable=SC1090
source "${BASH_SOURCE%/*}/../functions/foreman.sh"

timeout=$((SECONDS+120))
while [ $SECONDS -lt $timeout ]
do
  if puppet_health_status | egrep -q "healthy"
  then
    echo "Puppetserver's health is \"$(puppet_health_status)\""
    break
  fi
  sleep 5
  echo "Puppetserver's health is \"$(puppet_health_status)\""
done

while [ $SECONDS -lt $timeout ]
do
  if curl -s http://localhost:80/api/v2/status | egrep -q 'Unable to authenticate user'
  then
    break
  fi
done

# perform tests
run_newman_test 'foreman/provision.json'
puppet_agent_run "my-agent.dummy.test" "docker-compose_puppet-foreman-network"
run_newman_test 'foreman/post_agent_run.json'