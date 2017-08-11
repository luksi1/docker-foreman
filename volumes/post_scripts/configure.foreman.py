#!/usr/bin/python

from ForemanRestClient import ForemanRestClient
import getopt
import sys
import os
import subprocess
import socket
import time
import re

ADMIN_PASSWORD_FILE='/tmp/accounts/admin'
#ADMIN_PASSWORD_FILE='/opt/foreman/volumes/foreman/accounts/admin'

puppet_hostname = os.environ['PUPPET_HOSTNAME']
foreman_hostname = os.environ['FOREMAN_HOSTNAME']
domain = os.environ['DOMAIN']

# Get admin password
line = subprocess.check_output("tail -1 " + ADMIN_PASSWORD_FILE, shell=True)
stripped = re.sub(r"\\n'", "", line.decode('utf-8'))
password_hash = re.findall("\w{9,}", stripped)
admin_password = password_hash[0]

# Authentication
username = 'admin'
password = admin_password
url = 'https://' + foreman_hostname + '.' + domain

# Foreman API instance
foreman = ForemanRestClient.ForemanRestClient(username,password,url)

def add_smart_proxy():
  add_smart_proxy = foreman.add_smart_proxy(puppet_hostname, 'https://' + puppet_hostname + '.' + domain + ':8443')

  if 'message' in add_smart_proxy and add_smart_proxy['message'] == 'ERF64-6496 [Foreman::MaintenanceException]: There are migrations pending in the system.':
    print('Unable to add smart proxy right now')
    return False
  elif 'created_at' in add_smart_proxy:
    print('Successfully added smart proxy!')
    print(add_smart_proxy)
    return True
  elif 'error' in add_smart_proxy and add_smart_proxy['error']['errors']['name'][0] == 'has already been taken':
    print('Smart proxy has already been added')
    return True
  else:
    print(add_smart_proxy)
    return True

while add_smart_proxy() is False:
  print("Waiting to add smart proxy...")
  time.sleep(10)

import_puppet_classes = foreman.import_puppetclasses()
if not 'message' in import_puppet_classes:
  print(import_puppet_classes)
