# docker-foreman

A docker-compose stack for Foreman and Puppet using R10K for versioning.

## Description

Foreman is a tool for infrastructure provisioning. Puppet is a configuration management tool. PuppetDB is a backend, which provides an easy way to query how your infrastructure is provisioned as well as what is occuring in your infrastructure. R10K is simply a Ruby gem that allows you to pull Puppet modules directly into your configuration management stack. This docker-compose stack binds these components together in a seamless and easy way. All you need to do is input your infrastructure's parameters in an .env file.

PostgreSQL is used as a backend for PuppetDB and Foreman.

A post-configuration docker image is also used to import Puppet classes from your control repository and add Puppet as a smart proxy.

## Dependencies

#### Binaries
- docker-compose
- docker

#### Control repo
- a functional control repo. See the following urls if you are unsure about this:

  - https://docs.puppet.com/pe/latest/r10k.html
  - https://docs.puppet.com/pe/latest/cmgmt_control_repo.html
  - https://github.com/puppetlabs/control-repo
  
- add a webhook for each repository you want to trigger a pull when pushing to. Go to the settings for your repository you want to use as a trigger and add a webhook. Something like:
```
https://puppet:puppet@puppet-test.domain.com:8088/payload
```

## Setup

Go through the following to setup your environment:

1. Setup your .env file
2. Copy in SSL certificates and import your database if you are migrating from an existing instance
3. Run

#### .env

###### Mandatory

The location of your R10K control repo
```
R10K_REPO=https://github.com/myname/control-repo
```

The prefix in which your docker containers will have. 
```
IMAGE_PREFIX=foo
```

The tag of your images. Ex. test,prod,latest
```
IMAGE_TAG=latest
```

The domain of your infrastructure. This domain will be used for all of the docker containers.
```
DOMAIN=foo.com
```

The Puppet server's hostname. Do <b>NOT</b> include your domain!
```
PUPPET_HOSTNAME=puppet
```

The Foreman's hostname. Do <b>NOT</b> include your domain!
```
FOREMAN_HOSTNAME=foreman
```

PuppetDB's hostname. Do <b>NOT</b> include your domain!
```
PUPPETDB_HOSTNAME=puppetdb
```

Reset admin password and send output to /opt/foreman/volumes/foreman/accounts/admin.
You will need to use this if using the post configuration docker image. This image is "post" and will
run API calls to Foreman to import your Puppet classes and add Puppet as a smart proxy. If you are unsure about any of this, simply leave this as is. 

To turn this off, simply comment this out.
```
RESET_ADMIN_PASSWORD=true
```

###### Optional
If you want to use your own certificates for the Foreman GUI, specify them here
and place the certificates under foreman/certs.
```
FOREMAN_WEB_CA=foreman_ca.pem
FOREMAN_WEB_PUBLIC_CERT=foreman_public.crt
FOREMAN_WEB_PRIVATE_CERT=foreman_private.key
```

Specify how many max-active-instances of J-Ruby you want to start
```
MAX_ACTIVE_INSTANCES=1
```

Autosign.conf
```
AUTOSIGN=*.foo.com
```

Java heap
```
PUPPETSERVER_JAVA_ARGS=-Xms1024m -Xmx1024m
```

#### If you are migrating an existing instance

###### SSL

Copy SSL certificates to /opt/foreman/volumes/puppet/ssl/

###### PostgreSQL Database

1. Dump your PostgreSQL database and place the dump file in /opt/foreman/volumes/postgres/data/
2. Change the PostgreSQL docker image to match your PostgreSQL database that you are migrating from.
3. Start up your entire docker stack with: `docker-compose up`
4. Log in to your Postgres image with: `docker exec -it $(docker ps | grep postgres | awk '{print $1}') /bin/bash`
5. Perform your import. You dump file will be located under /var/postgres/data

#### If you are starting from a fresh install

- You will get some errors as the database is not yet seeded. This will be taken care of efter the Foreman installer has run. Nothing to worry about!
- You will need to create your puppet server certificates (CA, public, and private) and Foreman certificates and place it in /opt/foreman/volumes/puppet/ssl/. You can do this on a host with puppet installed or you can use docker:
```
docker run -v /opt/foreman/volumes/puppet/ssl:/etc/puppetlabs/puppet/ssl puppet/puppet-agent cert --generate YOUR_HOSTNAME
```

## Usage

#### Starting your configuration management stack

Once you have completed all the steps in the setup phase, simply run `docker-compose up` from the root directory.

#### Setting up your stack to start on boot

Edit your host's crontab accordingly
```
@reboot cd YOUR_ROOT_DIRECTORY && /usr/local/bin/docker-compose up -d
```

## Testing

#### Usage

Simply run `tests/run.test.sh`. This will start docker-compose, perform the following tests under "Coverage" and stop your instance.

#### Coverage

- Start/Stop docker-compose
- Puppet agent run
- Access Foreman API
- Import Puppet classes to Foreman
- Remove node from Foreman

## Notes

- If you would like to re-run the post-configuration steps, you'll need to remove the file `.configured` in `/opt/foreman/volumes/post_scripts/`
- Starting up this stack can take some time. Approx. 2 minutes.
- Startup time is partly due to the fact that R10K will perform a pull of all of it's modules. To speed up the time, there is a volume under `/opt/foreman/volumes/puppet/code` so that a fresh clone does not need to occur for each run.
- Postgres only listens locally.
- If you get a DH key pair error add the following to the bottom of your public foreman certificate: 
```
-----BEGIN DH PARAMETERS-----
MIGHAoGBAP//////////yQ/aoiFowjTExmKLgNwc0SkCTgiKZ8x0Agu+pjsTmyJR
Sgh5jjQE3e+VGbPNOkMbMCsKbfJfFDdP4TVtbVHCReSFtXZiXn7G9ExC6aY37WsL
/1y29Aa37e44a/taiZ+lrp8kEXxLH+ZJKGZR7OZTgf//////////AgEC
-----END DH PARAMETERS-----
```

## To Do

- Add MCollective
