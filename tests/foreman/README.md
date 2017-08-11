## Foreman REST Client Library

## Description
A REST client to handle Foreman's API. See Foreman's documentation at:

https://theforeman.org/api/1.8/

## Dependencies

### Python libraries
- requests
- json
- re

### Python version
- 3.4 (tested)

## Usage

```
from ForemanRestClient import ForemanRestClient
import getopt
import sys

username = "admin"
password = "abc123"
base_url = "https://foreman.domain.com"

foreman = ForemanRestClient.ForemanRestClient(username,password,base_url,domain='domain.com')
foreman.get_hosts()
```
