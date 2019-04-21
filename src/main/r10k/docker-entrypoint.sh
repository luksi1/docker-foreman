#!/bin/bash

# confd configuration
/usr/local/bin/confd -onetime -backend env

# pull all of our images
r10k deploy environment -pv 

# start webhook
puppet_webhook
