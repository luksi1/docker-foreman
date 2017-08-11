#!/usr/bin/python3

from ForemanRestClient import ForemanRestClient
from CLIColors import CLIColors
import subprocess
import getopt
import sys
import os
import re

puppet_hostname = os.environ['PUPPET_AGENT_HOSTNAME']
if len(sys.argv) > 1:
  print("You need to set the environment variable PUPPET_AGENT_HOSTNAME")
  sys.exit(1)

#ADMIN_PASSWORD_FILE='/tmp/accounts/admin'
ADMIN_PASSWORD_FILE='/opt/foreman/volumes/foreman/accounts/admin'

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

foreman = ForemanRestClient.ForemanRestClient(username,password,url)
colors = CLIColors.CLIColors()

def fail(comment):
  print(colors.BOLD + comment + ": " + colors.FAIL + "FAIL" + colors.ENDC)

def ok(comment):
  print(colors.BOLD + comment + ": " + colors.OKGREEN + "OK" + colors.ENDC)

def testAPI():
  test = "Access Foreman API"
  try:
    dashboard = foreman.get_dashboard()
  except:
    fail(test)
    raise
  if not isinstance(dashboard['active_hosts'], int):
    fail(test)
    print(dashboard)
  else:
    ok(test)

def testPuppetClassImport():
  test = "Import Puppet classes"
  try:
    import_puppet_classes = foreman.import_puppetclasses()
    ok(test)
  except Exception as e:
    print(e)
    fail(test)

def testPuppetNodeRemoval():
  test = "Remove our Puppet test host from Foreman"
  try:
    result = foreman.delete_host(puppet_hostname)
    ok(test)
  except:
    fail(test)
    raise
  
#testAPI()
#testPuppetClassImport()
#testPuppetNodeRemoval()
