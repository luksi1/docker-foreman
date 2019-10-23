#!/bin/bash

set -e

# shellcheck disable=SC1090
source "${BASH_SOURCE%/*}/../functions/foreman.sh"

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
  if sudo docker logs foreman 2> /dev/null | egrep -q ' \| Finished'
  then
    echo "Foreman has started"
    break
  else
    echo "Waiting for Foreman to come up..."
    sleep 5
    continue
  fi
done

# grab the container ID for Foreman
FOREMAN_CONTAINER_ID=$(get_container_id_by_label "org.label-schema.name" "foreman")

# grab password from Foreman
ADMIN_PASSWORD=$(reset_foreman_admin_password "$FOREMAN_CONTAINER_ID")

# swap out admin password with jq
set_newman_password "foreman/provision.json" "$ADMIN_PASSWORD"
set_newman_password "foreman/cleanup.json" "$ADMIN_PASSWORD"
set_newman_password "foreman/post_agent_run.json" "$ADMIN_PASSWORD"


# perform tests
run_newman_test 'foreman/provision.json'
puppet_agent_run "my-agent.dummy.test" "docker-compose_puppet-foreman-network"
run_newman_test 'foreman/post_agent_run.json'
run_newman_test 'foreman/cleanup.json'