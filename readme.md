# Line Queue program

This program stores input messages inside distributed queue.

The system consists of several modules:
- [line-queue-master](line-queue-master) - module responsible for communication between clients and workers (may be only one per cluster)
- [line-queue-client](line-queue-client) - module implementing CLI interface (may be several instances)
- [line-queue-worker](line-queue-worker) - module responsible for physical queue storing (may be several instances)
- [line-queue-common](line-queue-common) - used for common classes storing
- [line-queue-tests](line-queue-tests) - here integration tests live
 
CLI clients receive messages from client via STDIN and produce output to STDOUT.
List of the acceptable commands:
- "PUT text" - captures an element and put it into distributed queue
- "GET n" - returns last n elements from distributed queue
- "SHUTDOWN" - shuts down all master and worker services
- "QUIT" - disconnects the CLI client from the cluster

project uses dependencies:
- Netty - the central communication bus
- Lombok - only for data classes
- log4j2 - for logging

## Build the project

### Prerequisites
- install jdk 8 or 11

### Build
- `./gradlew clean build customUberJar -x test`

## Cluster installation

### Prerequisites
- install docker
- install docker-compose
- build the project

### Run prepared docker cluster
- `cd docker && docker-compose up`

### Start cluster manually (without docker)
- `cd scripts && ./start-master.sh` - starts the master on 10042 port
- `cd scripts && ./start-worker.sh --worker.port 10043` - starts the worker on 10043 port which will try to connect to master on "localhost:10042"
- `cd scripts && ./start-client.sh` - starts the CLI client which will try to connect to master on "localhost:10042"


## Unrealized ideas
- Client - Master - Worker connection is synchronous, may be async
- Worker communicates with master within only one channel, it's better to hold connection pool to any worker on master side
- GET requests from master to workers consist only of one line, but might be grouped in some batches
- Now result (for GET n) is unsorted, but it might be sorted
- Now system only shards the queue, but for reliability it should replicate queue parts
- At the worker's shutdown moment it's necessary to snapshot the queue and restore in case of opening back 
- Cluster shutdown logic is too raw, need to be improved
- Netty handlers should be divided into parts inside each module, because now it's too complicated
- replace dependency `io.netty:netty-all` with only necessary netty modules the same about uber jars