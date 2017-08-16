#!/bin/bash

QUIET=$1

BOLD='\e[1m'
OKGREEN='\e[32m'
FAIL='\e[31m'
RESET='\e[0m'
SCRIPT=`realpath $0`
SCRIPTPATH=`dirname $SCRIPT`

function fail {
  echo -e "${BOLD}${1}: ${FAIL}FAIL${RESET}"
}

function ok { 
  echo -e "${BOLD}${1}: ${OKGREEN}OK${RESET}"
}

if [[ -z $QUIET ]]; then
  docker run -w $SCRIPTPATH -h $PUPPET_AGENT_HOSTNAME puppet/puppet-agent-ubuntu agent -t --server=${PUPPET_HOSTNAME}.${DOMAIN} --waitforcert 10 > /dev/null 2>&1
else 
  docker run -w $SCRIPTPATH -h $PUPPET_AGENT_HOSTNAME puppet/puppet-agent-ubuntu agent -t --server=${PUPPET_HOSTNAME}.${DOMAIN} --waitforcert 10
fi

if [[ $? -gt 0 ]]; then
  fail "Puppet Agent Run: "
else
  ok "Puppet Agent Run: "
fi
