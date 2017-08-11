#!/usr/bin/python3.4

import requests
import json
import sys
import re
import ast
from RestClient import RestClient

class ForemanRestClient(RestClient.RestClient):

  def __init__(self, username, password, base_url, **kvargs):
    super().__init__(username, password)
    self.api = kvargs.get("api", 'v2')
    self.domain = kvargs.get("domain", None)
    
    if self.api is 'v2':
      self.base_url = base_url + "/api/v2/"
    else:
      self.base_url = base_url + "/api/"
      
  def __get_results(self, path):
    json_data = super().get_data(self.base_url + path)
    results_json = json.loads(json_data)
    results = results_json['results']
    return results
  
  def __get_puppetclass_ids(self):
      
    results = self.__get_results("puppetclasses")
    ids = []
    for r in results['role']:
      ids.append(r['id'])
    return ids

  def __get_puppetclass_id_by_name(self, puppetclass):

     prog = re.compile(re.escape(puppetclass.lower()))
     results = self.__get_results("puppetclasses")
     hostgroup_id = None
     ids = {}
     for r in results['profile']:
         name = r['name']
         if prog.search(name):
             ids[name] = r['id']

     if len(ids.keys()) >= 2:
       return ids
     elif len(ids.keys()) == 1:
       return ids[0]
     else:
       raise ValueError('%s not found' % (puppetclass))

  def __get_hostgroup_id_by_name(self, hostgroup_name):
    prog = re.compile(re.escape(hostgroup_name))
    results = self.__get_results("hostgroups")
    hostgroup_id = None
    ids = []
    for r in results:
        if prog.search(r['title']):
            ids.append(r['id'])

    if len(ids) >= 2:
       return ids
    elif len(ids) == 1:
       return ids[0]
    else:
       raise ValueError('%s not found' % (hostname))
        
  def __get_host_id_by_name(self, hostname):
    prog = re.compile(re.escape(hostname))
    results = self.__get_results("hosts")
    
    ids = []
    for r in results:
        if prog.search(r['name']):
            ids.append(r['id'])

    if len(ids) >= 2:
       return ids
    elif len(ids) == 1:
        return ids[0]
    else:
        raise ValueError('%s not found' %s (hostname))

  def __get_environment_id_by_name(self, environment):
    prog = re.compile(re.escape(environment))
    ids = []
    for r in self.__get_results("environments"):
      name = r['name']
      if prog.search(name):
         ids.append(r['id'])
    
    if len(ids) > 1:
       raise ValueError('%s returned more than one element. Try refining your search' % (environment))

    return ids[0]

  def __get_smart_proxy_id_by_name(self, name):
    prog = re.compile(re.escape(name))
    ids = []
    for r in self.__get_results("smart_proxies"):
      name = r['name']
      if prog.search(r['name']):
        ids.append(r['id'])
    if len(ids) > 1:
       raise ValueError('%s returned more than one element. Try refining your search' % (name))

    return ids[0]

  '''The Foreman API returns values with 'true', 'false', and 'null'. These need to be replaced by their Python equivalent value'''
  def __clean_api_string(self, string):
    if type(string) == str:
      string = string.replace('null', 'None')
      string = string.replace('true', 'True')
      string = string.replace('false', 'False')
    return string

  def import_puppetclasses(self):
    puppet_id = self.__get_smart_proxy_id_by_name("puppet")
    url = self.base_url + "smart_proxies/" + str(puppet_id) + "/import_puppetclasses"
    payload = {'smart_proxy': {}}
    text = super().post_data(url, payload)
    return text

  def add_smart_proxy(self, name, proxy_url):
    url = self.base_url + "smart_proxies"
    payload = {'smart_proxy': {'name': name, 'url': proxy_url }}
    text = super().post_data(url, payload)
    cleaned = self.__clean_api_string(text)
    return eval(cleaned)

  def get_smart_proxy(self, name):
    path = "smart_proxies/" + str(self.__get_smart_proxy_id_by_name(name))
    json_data = super().get_data(self.base_url + path)
    return(json.loads(json_data))

  def get_dashboard(self):
    path = "dashboard"
    json_data = super().get_data(self.base_url + path)
    results_json = json.loads(json_data)
    return results_json

  def get_hosts(self):
    results = self.__get_results("hosts")
    return results

  def delete_host(self, hostname):
    host_id = self.__get_host_id_by_name(hostname)
    url = self.base_url + "hosts" + "/" + str(host_id)
    result = super().delete_data(url)
    # See clean_api_string for why this is needed
    cleaned = self.__clean_api_string(result)
    return eval(cleaned)

  def get_hostnames(self):
    hosts = []
    for r in self.__get_results("hosts"):
      hosts.append(r['name'])
    return hosts

  def bulk_delete_puppet_classes(self):
    for i in __get_puppet_class_ids():
      super().delete_data(self.base_url + "puppetclasses" + "/" + str(i))
