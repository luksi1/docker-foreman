#!/bin/bash

set -e

# grab the container ID for Foreman
FOREMAN_CONTAINER_ID=$(/usr/bin/sudo /usr/bin/docker ps -qaf label=org.label-schema.name=foreman)
# grab password from foreman
ADMIN_PASSWORD=$(/usr/bin/sudo /usr/bin/docker exec -t "$FOREMAN_CONTAINER_ID" foreman-rake permissions:reset | /bin/grep 'Reset to user:' | /usr/bin/awk '{print $NF}' | tr -d '\r')
# swap out admin password with jq
/usr/bin/jq ".auth.basic[0].value = \"${ADMIN_PASSWORD}\"" tests/foreman.json > tests/foreman.json.bak && mv tests/foreman.json.bak tests/foreman.json
# perform tests
/usr/bin/newman run tests/foreman.json --insecure

