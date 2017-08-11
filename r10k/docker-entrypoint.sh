#!/bin/bash

sed -i "s|.*remote.*|      'remote'  => '$R10K_REPO',|" /webhook.pp
puppet apply /webhook.pp --modulepath=/modules
tail -f /var/log/webhook/access.log
