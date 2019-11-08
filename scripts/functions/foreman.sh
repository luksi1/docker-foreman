#!/bin/bash

# Make a Puppet run with a hostname and attach it to a network
puppet_agent_run() {
  if [[ "${1}x" == "x" || "${2}x" == "x" ]]
  then
    echo "You must indicate a hostname as your first argument and a network as your second argument"
    echo "Ex. ${0} foo.domain.com foreman-network"
    exit 1
  fi
  /usr/bin/sudo -E /usr/bin/docker run --network "$2" -h "$1" puppet/puppet-agent-ubuntu
}

set_newman_password() {
  if [[ "${1}x" == "x" || "${2}x" == "x" ]]
  then
    echo "You must indicate a Newman file as your first argument, and Foreman's admin password as the second argument"
    echo "Ex. ${0} foo.json ABCD1234"
    exit 1
  fi

  jq ".auth.basic[0].value = \"${2}\"" "tests/${1}" > "tests/${1}.bak" && mv "tests/${1}.bak" "tests/${1}"
}

reset_foreman_admin_password() {
  if [[ "${1}x" == "x" ]]
  then
    echo "You must indicate a container ID"
    echo "Ex. $0 kjdfljdfkjldf"
    exit 1
  fi

  /usr/bin/sudo /usr/bin/docker exec -t "$1" foreman-rake permissions:reset | /bin/grep 'Reset to user:' | /usr/bin/awk '{print $NF}' | tr -d '\r'
}

puppet_health_status() {
  sudo docker inspect --format '{{json .State.Health.Status }}' puppetserver | sed s/\"//g
}

run_newman_test() {
  if [[ "${1}x" == "x" ]]
  then
    echo "You must indicate a JSON script to run"
    echo "Ex. run_newman_test foo.json"
    exit 1
  fi
  newman run "tests/${1}" --insecure
}

get_container_id_by_label() {
  if [[ "${1}x" == "x" || "${2}x" == "x" ]]
  then
    echo "You must indicate a label as your first argument, and the value of that label as the second argument"
    echo "Ex. get_id_by_label my.label.name foo"
    exit 1
  fi

  /usr/bin/sudo /usr/bin/docker ps -qaf "label=${1}=${2}"
}