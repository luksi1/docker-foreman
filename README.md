[![Build Status](https://travis-ci.org/luksi1/docker-foreman.svg?branch=docker-maven-plugin)](https://travis-ci.org/luksi1/docker-foreman)
[![SonarCloud Bugs](https://sonarcloud.io/api/project_badges/measure?project=luksi1_docker-foreman&metric=bugs)](https://sonarcloud.io/component_measures/metric/reliability_rating/list?id=luksi1_docker-foreman)
[![SonarCloud Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=luksi1_docker-foreman&metric=vulnerabilities)](https://sonarcloud.io/component_measures/metric/security_rating/list?id=luksi1_docker-foreman)

# docker-foreman

A docker stack for Puppet, using Foreman as the external node classifier, R10K for version control, and PuppetDB. 

## Images

**luksi1/foreman**

[![](https://images.microbadger.com/badges/image/luksi1/foreman:1.20-1.svg)](https://microbadger.com/images/luksi1/foreman:1.20-1 "Get your own image badge on microbadger.com") [![](https://images.microbadger.com/badges/version/luksi1/foreman:1.20-1.svg)](https://microbadger.com/images/luksi1/foreman:1.20-1 "Get your own version badge on microbadger.com") [![](https://images.microbadger.com/badges/license/luksi1/foreman:1.20-1.svg)](https://microbadger.com/images/luksi1/foreman:1.20-1 "Get your own license badge on microbadger.com")

**luksi1/puppet-foreman**

[![](https://images.microbadger.com/badges/image/luksi1/puppetserver-foreman.svg)](https://microbadger.com/images/luksi1/puppetserver-foreman "Get your own image badge on microbadger.com") [![](https://images.microbadger.com/badges/version/luksi1/puppetserver-foreman:6.2.1-2.svg)](https://microbadger.com/images/luksi1/puppetserver-foreman:6.2.1-2 "Get your own version badge on microbadger.com") [![](https://images.microbadger.com/badges/commit/luksi1/puppetserver-foreman:6.2.1-2.svg)](https://microbadger.com/images/luksi1/puppetserver-foreman:6.2.1-2 "Get your own commit badge on microbadger.com") [![](https://images.microbadger.com/badges/license/luksi1/puppetserver-foreman:6.2.1-2.svg)](https://microbadger.com/images/luksi1/puppetserver-foreman:6.2.1-2 "Get your own license badge on microbadger.com")

**luksi1/r10k**

[![](https://images.microbadger.com/badges/image/luksi1/r10k:2.6.5-2.svg)](https://microbadger.com/images/luksi1/r10k:2.6.5-2 "Get your own image badge on microbadger.com")
[![](https://images.microbadger.com/badges/version/luksi1/r10k:2.6.5-2.svg)](https://microbadger.com/images/luksi1/r10k:2.6.5-2 "Get your own version badge on microbadger.com")

**luksi1/puppet-smart-proxy**

[![](https://images.microbadger.com/badges/image/luksi1/puppet-smart-proxy:1.20-2.svg)](https://microbadger.com/images/luksi1/puppet-smart-proxy:1.20-2 "Get your own image badge on microbadger.com")
[![](https://images.microbadger.com/badges/version/luksi1/puppet-smart-proxy:1.20-2.svg)](https://microbadger.com/images/luksi1/puppet-smart-proxy:1.20-2 "Get your own version badge on microbadger.com")

## Description

**Foreman** is a free open source project used to automate tasks, deploy applications, and manage a server's life cycle, either bare-metal, virtual, or in the cloud.

**Puppet** is a configuration management tool provided by PuppetLabs, allowing system administrators to use "Infrastructure as Code" to define a server's state.

**PuppetDB** is a backend, providing an easy way to query your infrastructure's operating systems, versions, network cards, and
other meta information.

**R10K** is a Ruby gem that allows you to pull Puppet modules directly into your configuration management stack from a version control system.

**PostgreSQL** is used as a backend for PuppetDB and Foreman.

This stack binds these components together in a seamless and easy way. All you need to do is input your infrastructure's parameters in an .env file, K8s cluster, or simply run an image on the side.

## Dependencies

#### Binaries
- docker
- docker-compose

#### Control repo
A functional control repo. See the following urls if you are unsure about this:
  - https://docs.puppet.com/pe/latest/r10k.html
  - https://docs.puppet.com/pe/latest/cmgmt_control_repo.html
  - https://github.com/puppetlabs/control-repo

Add a webhook for each repository you want to trigger a pull when pushing to. Go to the settings for your repository you want to use as a trigger and add a webhook. Something like:
```
https://puppet:puppet@puppet-test.domain.com:8088/payload
```

## Up and running with docker-compose

Use the following three commands to create a Puppet certificate (puppet.dummy.test) and Foreman certificate for your web frontend.

```
scripts/utilities/create.certificates.sh
cd examples/docker-compose
docker-compose up
```

## Configure hostnames, domains, and certificates for use with docker-compose

Go through the following to setup your environment:

1. Setup your .env file 
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

**Foreman's hostname (do not include the domain name)**
```
foreman_hostname=foreman
```

**Puppetserver's hostname (do not include the domain name)**
```
puppet_hostname=puppet
```

**R10K's hostname (do not include the domain name)**
```
r10k_hostname=r10k
```

**Puppet smartproxy's hostname (do not include the domain name)**
```
puppet_smartproxy_hostname: puppet-smart-proxy
```

**Path to Foreman's public certificate**
This is the public facing web server public certificate.
```
foreman_server_cert_file=../../volumes/certificates/certs/foreman.dummy.test.crt
```

**Path to Foreman's private certificate**
This is the public facing web server certificate's private key.
```
foreman_server_cert_key_file=../../volumes/certificates/private/foreman.dummy.test.key
```

**Path to Foreman's certificate authority chain**
```
foreman_server_cert_chain_file=../../volumes/certificates/certs/ca-chain.crt
```

### Certificate handling

One tip if you need to create certificates signed by your Puppet CA is simply to boot up your environment without puppet:

```
docker-compose up puppetserver
```

and then proceed to create your server certificates, for instance, this would create a puppet-smart-proxy:

```
docker run -it -v $(pwd)/volumes/puppet/ssl:/etc/puppetlabs/puppet/ssl --hostname puppet-smart-proxy.dummy.test puppet/puppet-agent
```

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

1. Dump your PostgreSQL database and place the dump file in /opt/foreman/volumes/postgres/data/
2. Change the PostgreSQL docker image to match your PostgreSQL database that you are migrating from.
3. Start up your entire docker stack with: `docker-compose up`
4. Log in to your Postgres image with: `docker exec -it $(docker ps | grep postgres | awk '{print $1}') /bin/bash`
5. Perform your import. You dump file will be located under /var/postgres/data

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

- Start/Stop docker-compose
- Puppet agent run
- Access Foreman API

## Notes

- Starting up this stack can take some time. Approx. 1 minute.
- Startup time is partly due to the fact that R10K will perform a pull of all of it's modules. To speed up the time, there is a volume under `/opt/foreman/volumes/puppet/code` so that a fresh clone does not need to occur for each run.
- Postgres only listens locally on the docker network by default. You will not be able access it remotely.
