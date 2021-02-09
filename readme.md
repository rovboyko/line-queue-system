#Line Queue program

## Build

## Installation

### Prerequisites
- install docker
- install docker-compose

### Run

## Unfinished ideas
- Client - Master - Worker connection is synchronous, may be async
- Worker communicates with master within only one channel, it's better to hold connection pool to any worker on master side
- GET requests from master to workers consist only of one line, but might be grouped in some batches