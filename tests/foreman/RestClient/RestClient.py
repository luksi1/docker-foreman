#!/usr/bin/python3.4

import requests
import sys
import json

class RestClient:

  def __init__(self, username, password):
    self.username = username
    self.password = password
    self.headers  = {'Content-Type': 'application/json'}

  def get_data(self, url):
    try:
      r = requests.get(url, headers=self.headers, auth=(self.username, self.password), verify=False)
    except requests.exceptions.RequestException as e:
      raise
    if r.status_code == 404:
      r.raise_for_status()
    return r.text

  def delete_data(self, url):
    try:
      r = requests.delete(url, headers=self.headers, auth=(self.username, self.password), verify=False)
    except requests.exceptions.RequestException as e:
      raise
    if r.status_code == 404:
      r.raise_for_status()
    return r.text

  def put_data(self, url, payload):
    try:
      r = requests.put(url, data=json.dumps(payload), headers=self.headers, auth=(self.username, self.password), verify=False)
    except requests.exceptions.RequestException as e:
      raise
    if r.status_code == 404:
      r.raise_for_status()
    return r.text

  def post_data(self, url, payload):
    try:
      r = requests.post(url, data=json.dumps(payload), headers=self.headers, auth=(self.username, self.password), verify=False)
    except requests.exceptions.RequestException as e:
      raise
    if r.status_code == 404:
      r.raise_for_status()
    return r.text
