#!/bin/bash

curl -s http://localhost:3000/api/v2/status | grep -q 'Unable to authenticate user'