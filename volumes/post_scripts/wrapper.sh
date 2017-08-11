#!/bin/bash


if [[ -f .configured ]]; then
  echo "Already performed post-config"
  exit 0
fi

pip3 install requests
apt-get update && apt-get -y install netcat

while ! nc -z puppet 8140; do
  sleep 1
done

python configure.foreman.py

if [[ $? -eq 0 ]]; then
  touch .configured
fi
