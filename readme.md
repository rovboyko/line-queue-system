# Line Queue program

This program stores input messages inside distributed queue.

## System architecture
System completely relies on Netty as a central communication tool.
Main modules:

- CLI Client 
receives messages from user and sends them to master. 
Master can serve many clients. Each CLI client holds netty client and communicate with master through it.

- Master 
responsible for providing requests from clients to workers. 
It uses ConcurrentSkipListMap for storing binding from requests to clients and ConcurrentHashMap as a workers list.

- Worker 
stores the queue inside ArrayBlockingQueue. It processes messages one by one. Every single input message produces single output one.
The queue capacity is controlled by the `worker.queue.capacity` config property.

## User interface description
CLI clients receive messages from user via STDIN and produce output to STDOUT.
List of the acceptable commands:
- `PUT line` - captures an element and put it into distributed queue
- `GET n` - returns last n elements from distributed queue
- `SHUTDOWN` - shuts down all master and worker services
- `QUIT` - disconnects the CLI client from the cluster

## Project structure
The system consists of several modules:
- [line-queue-master](line-queue-master) - module responsible for communication between clients and workers (may be only one per cluster)
- [line-queue-client](line-queue-client) - module implementing CLI interface (may be several instances)
- [line-queue-worker](line-queue-worker) - module responsible for physical queue storing (may be several instances)
- [line-queue-common](line-queue-common) - used for common classes storing
- [line-queue-tests](line-queue-tests) - here integration tests live

Project uses dependencies:
- Netty - the central communication bus
- Lombok - only for data classes
- log4j2 - for logging

## Build the project

### Pre-build
- install jdk 8 or higher

### Build
- `./build.sh`

### Test
- `./gradlew test` - takes approximately 10-15 sec

## Cluster installation

### Pre-installation
- install docker
- install docker-compose
- build the project

### Run prepared docker cluster
`./start-docker.sh` - starts the cluster of 1 master and 3 workers inside docker, it exposes 10042 port to the host network. 
Also it starts the CLI client to interact with user.

### Start cluster on the host machine (without docker)
`./start-docker.sh` - this script:
- starts the master on 10042 port as daemon process
- starts the first worker on 10043 port as daemon process
- starts the second worker on 10044 port as daemon process
- starts the third worker on 10045 port as daemon process
- starts the CLI client to interact with user

## Unrealized ideas
- GET requests from master to workers consist only of one line, but might be grouped in some batches
- STDOUT result for GET n is unsorted, but it might be sorted
- Functionality for workers unregistering is not implemented yet
- System only shards the queue, but for reliability it should replicate queue parts
- At the worker's shutdown moment it's necessary to snapshot the queue and restore in case of opening back 
- Cluster shutdown logic is too raw, need to be improved. It doesn't work when more than one clients connected to cluster
- Netty handlers should be divided into parts inside each module, because now it looks too complicated
- replace dependency `io.netty:netty-all` with only necessary netty modules and try to reduce uber jars size