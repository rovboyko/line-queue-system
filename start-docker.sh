#!/bin/bash

docker-compose -f docker/docker-compose.yml up -d

./scripts/start-client.sh