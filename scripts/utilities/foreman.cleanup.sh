#!/bin/bash

# Description
# A utility script used to clean up volumes and certificates while testing. This is destructive! Be careful!

echo "Stopping all images on this host"
sudo docker ps -qa | sudo xargs docker rm -f

echo "Removing Foreman docker network"
sudo -E docker network rm foreman

echo "Removing all volumes"
sudo rm -rf volumes/*

echo "Removing all databases"
sudo rm -rf /databases/*

echo "Removing all certificates"
sudo rm -rf /tmp/foreman*
sudo rm -rf /etc/puppetlabs/puppet/ssl