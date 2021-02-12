#!/bin/bash

./scripts/start-master.sh &
sleep 5
./scripts/start-worker.sh --worker.port 10043 &
./scripts/start-worker.sh --worker.port 10044 &
./scripts/start-worker.sh --worker.port 10045 &
./scripts/start-client.sh
