#!/bin/bash

set -ex

# update TLS public certs so that they are trusted in your environment
update-ca-trust

while ! nc -z "${DATABASE_HOSTNAME}" "${DATABASE_PORT}"; do
  sleep 1
done

bundle exec rake db:migrate
bundle exec rake db:seed
bundle exec rake permissions:reset username=admin password="${FOREMAN_ADMIN_PASSWORD}"

bundle exec bin/rails server -b 0.0.0.0