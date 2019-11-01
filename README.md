[![Build Status](https://travis-ci.org/luksi1/docker-foreman.svg?branch=master)](https://travis-ci.org/luksi1/docker-foreman)
[![SonarCloud Bugs](https://sonarcloud.io/api/project_badges/measure?project=luksi1_docker-foreman&metric=bugs)](https://sonarcloud.io/component_measures/metric/reliability_rating/list?id=luksi1_docker-foreman)
[![SonarCloud Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=luksi1_docker-foreman&metric=vulnerabilities)](https://sonarcloud.io/component_measures/metric/security_rating/list?id=luksi1_docker-foreman)

# docker-foreman

A configuration management stack using Puppet as the configuration management tool, Foreman as the external node classifier (ENC), R10K for version control, and PuppetDB.

This stack aims to provide a reasonable workflow and easy onboarding for using Infrastructure as Code with Puppet. To accomplish
this, we'll swipe Puppet's images from https://github.com/puppetlabs/puppet-in-docker and simply configure Foreman as an ENC so
that we can visualize and easily configure our servers. Included is two PostreSQL hosts, used as backends for Foreman and PuppetDB. R10K is also provided so that we can utilize version control for our modules.

## Docker images

**luksi1/foreman**

[![](https://images.microbadger.com/badges/image/luksi1/foreman:1.23.svg)](https://microbadger.com/images/luksi1/foreman:1.23 "Get your own image badge on microbadger.com") [![](https://images.microbadger.com/badges/version/luksi1/foreman:1.23.svg)](https://microbadger.com/images/luksi1/foreman:1.23 "Get your own version badge on microbadger.com") [![](https://images.microbadger.com/badges/license/luksi1/foreman:1.23.svg)](https://microbadger.com/images/luksi1/foreman:1.23 "Get your own license badge on microbadger.com")

**luksi1/puppet-foreman**   

[![](https://images.microbadger.com/badges/image/luksi1/puppetserver-foreman.svg)](https://microbadger.com/images/luksi1/puppetserver-foreman "Get your own image badge on microbadger.com") [![](https://images.microbadger.com/badges/version/luksi1/puppetserver-foreman:6.7.svg)](https://microbadger.com/images/luksi1/puppetserver-foreman:6.7 "Get your own version badge on microbadger.com") [![](https://images.microbadger.com/badges/commit/luksi1/puppetserver-foreman:6.2.1-2.svg)](https://microbadger.com/images/luksi1/puppetserver-foreman:6.2.1-2 "Get your own commit badge on microbadger.com") [![](https://images.microbadger.com/badges/license/luksi1/puppetserver-foreman:6.2.1-2.svg)](https://microbadger.com/images/luksi1/puppetserver-foreman:6.2.1-2 "Get your own license badge on microbadger.com")

**luksi1/r10k**

[![](https://images.microbadger.com/badges/image/luksi1/r10k:2.6.5.svg)](https://microbadger.com/images/luksi1/r10k:2.6.5 "Get your own image badge on microbadger.com")
[![](https://images.microbadger.com/badges/version/luksi1/r10k:2.6.5.svg)](https://microbadger.com/images/luksi1/r10k:2.6.5 "Get your own version badge on microbadger.com")

**luksi1/puppet-smart-proxy**

[![](https://images.microbadger.com/badges/image/luksi1/puppet-smart-proxy:1.20-2.svg)](https://microbadger.com/images/luksi1/puppet-smart-proxy:1.20-2 "Get your own image badge on microbadger.com")
[![](https://images.microbadger.com/badges/version/luksi1/puppet-smart-proxy:1.20-2.svg)](https://microbadger.com/images/luksi1/puppet-smart-proxy:1.20-2 "Get your own version badge on microbadger.com")'

## Dependencies

#### Binaries
- docker
- docker-compose

#### Control repo
A functional control repo. See the following urls if you are unsure about this:
  - https://docs.puppet.com/pe/latest/r10k.html
  - https://docs.puppet.com/pe/latest/cmgmt_control_repo.html
  - https://github.com/puppetlabs/control-repo

If you want, you can add a webhook for each repository you want to trigger. R10K exposes a Sinatra API over port 8088 that you can trigger. Go to the settings for your repository you want to use as a trigger and add a webhook. Something like:

```
https://puppet:puppet@puppet.domain.com:8088/payload
```

## Up and running with docker-compose

Use the following three commands to create a Puppet certificate (puppet.dummy.test) and Foreman certificate for your web frontend.
Be sure to add puppet.dummy.test and foreman.dummy.test to `/etc/hosts` so the certificates match. Once the stack has booted
up, you should be able to surf to foreman.dummy.test.

```
scripts/utilities/create.certificates.sh
cd examples/docker-compose
docker-compose up
```

## Configure hostnames, domains, and certificates for use with docker-compose

Go through the following to setup your environment:

1. Setup your .env file. You can se an example at examples/docker-compose/.env
2. Copy in SSL certificates and import your database if you are migrating from an existing instance
3. Run

### .env

#### Mandatory

**Your domain**
```
domain=domain.com
```

**The location of your R10K control repo**
```
control_repo=https://github.com/myname/control-repo
```

**Foreman's hostname. Do not include the domain name. This will be taken from "domain_name"**
```
foreman_hostname=foreman
```

**Puppetserver's hostname. Do not include the domain name. This will be taken from "domain_name"**
```
puppet_hostname=puppet
```

**R10K's hostname. Do not include the domain name. This will be taken from "domain_name"**
```
r10k_hostname=r10k
```

**Puppet smartproxy's hostname. Do not include the domain name. This will be taken from "domain_name"**
```
puppet_smartproxy_hostname: puppet-smart-proxy
```

**Path to Foreman's public certificate**
This is Foreman's public facing web server certificate. This is the public server certificate you would see when
surfing into the Foreman GUI.
```
foreman_server_cert_file=../../volumes/certificates/certs/foreman.dummy.test.crt
```

**Path to Foreman's private certificate**
This is Foreman's public facing web server certificate private key. This is the key for the server certificate you would see when
surfing into the Foreman GUI.
```
foreman_server_cert_key_file=../../volumes/certificates/private/foreman.dummy.test.key
```

**Path to Foreman's certificate authority chain**
This is the certificate authority for Foreman's public facing certificate.
```
foreman_server_cert_chain_file=../../volumes/certificates/certs/ca-chain.crt
```

### Certificate handling

SSL is hard. To ease the frustration of creating certificates, you can simply update the environment variables in 
`scripts/utilities/create.certificates.sh` and run it. This will get you up and running with an appropriate Puppet certificate.
And then you can either simply use the self-signed Foreman certificate or change it out if you need a certificate with a real
certificate authority at a later date.

#### Optional

**Java heap for Puppetserver**
```
PUPPETSERVER_JAVA_ARGS=-Xms1024m -Xmx1024m
```

**Autosigning certificates (default true)**
```
AUTOSIGN="false"
```

### If you are migrating an existing instance

#### SSL

Copy SSL certificates to /opt/docker-foreman/volumes/puppet/ssl/

#### PostgreSQL Database

You'll need to copy your Postgres instance to /databases/foreman-postgres or import it into Postgres using conventional
docker commands. Something like `cp my.dump.sql /databases/foreman-postgres` and `docker run -it -v /databases/foreman-postgres:/var/lib/postgresql/data/ postgres bash` and then import your database from /var/lib/postgresql/data.

## Standard Operating Procedures

### Starting your foreman stack

```
cd examples/docker-compose/
docker-compose up -d
```

#### Setting up your stack to start on boot

Edit your host's crontab accordingly
```
@reboot cd YOUR_ROOT_DIRECTORY && /usr/local/bin/docker-compose up -d
```

## Testing

#### Coverage

- Syntax checking with ShellCheck and Hadolint
- Start/Stop docker-compose
- Puppet agent run
- Access Foreman API

## Notes

- Starting up this stack can take some time. Approx. 1 minute.
- Startup time is partly due to the fact that R10K will perform a pull of all of it's modules. To speed up the time, there is a volume under `/opt/foreman/volumes/puppet/code` so that a fresh clone does not need to occur for each run.
- Postgres only listens locally on the docker network by default. You will not be able access it remotely.
