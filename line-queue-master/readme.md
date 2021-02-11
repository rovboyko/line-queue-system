# Line Queue Master

Responsible for communication between clients and workers.
May serve many clients and provide their messages to many workers.
It also routes worker's responses back to clients.

The queue is distributed between workers according to `WorkerChoosingStrategy` provided to master. 