#!/bin/bash

DOCKER_DIR='/opt/foreman'
SCRIPT=`realpath $0`
SCRIPTPATH=`dirname $SCRIPT`
ENV="${SCRIPTPATH}/../.env"

export `grep DOMAIN ${ENV}`
export `grep PUPPET_HOSTNAME ${ENV}`
export `grep FOREMAN_HOSTNAME ${ENV}`
export PUPPET_AGENT_HOSTNAME="foo.${DOMAIN}"
export SSL_DIR='/opt/foreman/volumes/puppet/ssl'

if [[ -f "${SSL_DIR}/ca/signed/${PUPPET_AGENT_HOSTNAME}.pem" ]]; then
  find ${SSL_DIR}/ca/signed -name ${PUPPET_AGENT_HOSTNAME}.pem | xargs rm 
fi

cd $DOCKER_DIR

function stop_composer {
  docker-compose stop
}

function run_composer {
  docker-compose up -d
}

function import_puppet_classes {
  ${SCRIPTPATH}/foreman/test.puppet.class.import.py
}

function puppet_agent_run {
  ${SCRIPTPATH}/puppet/test.puppet.sh 
}

function foreman_api {
  ${SCRIPTPATH}/foreman/test.foreman.api.py
}

function delete_node {
  ${SCRIPTPATH}/foreman/test.delete.node.py
}

run_composer

while ! nc -z $PUPPET_HOSTNAME 8443; do
  sleep 1
done

puppet_agent_run
delete_node
foreman_api
import_puppet_classes
stop_composer

unset DOMAIN
unset PUPPET_HOSTNAME
unset FOREMAN_HOSTNAME
