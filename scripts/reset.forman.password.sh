#!/bin/bash

docker exec -t $(docker ps | grep '/foreman:' | awk '{print $1}') foreman-rake permissions:reset
