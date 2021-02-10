#Line Queue program

This program stores input messages inside distributed queue.
It consists of:
- One master service
- Many worker services
- Many CLI clients
 
CLI clients receive messages from client via STDIN and produce output to STDOUT.
List of the acceptable commands:
- "PUT text" - captures an element and put it into distributed queue
- "GET n" - returns last n elements from distributed queue
- "SHUTDOWN" - shuts down all master and worker services
- "QUIT" - disconnects the CLI client from the cluster

## Build the project

### Prerequisites
- install jdk 8 or 11

### Build
- `./gradlew build`

## Cluster installation

### Prerequisites
- install docker
- install docker-compose
- build the project

### Run prepared docker cluster
- `cd docker && docker-compose up`

### Start cluster manually (without docker)
- `cd scripts && ./start-master.sh "10042"` - starts the master on 10042 port
- `cd scripts && ./start-worker.sh "localhost:10042" "10043"` - starts the worker on 10043 port which will try to connect to master on "localhost:10042"
- `cd scripts && ./start-client.sh "localhost:10042"` - starts the CLI client which will try to connect to master on "localhost:10042"


## Unfinished ideas
- Client - Master - Worker connection is synchronous, may be async
- Worker communicates with master within only one channel, it's better to hold connection pool to any worker on master side
- GET requests from master to workers consist only of one line, but might be grouped in some batches
- Now result (for GET n) is unsorted, but it might be sorted