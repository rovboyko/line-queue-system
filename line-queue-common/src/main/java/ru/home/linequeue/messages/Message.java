package ru.home.linequeue.messages;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Message implements Serializable {

    private static final long serialVersionUID = 6313680935421286924L;

    private Type type;
    private String body;
    private long ts;
    private long reqId;

    public enum Type {
        GET, // used for get request
        PUT, // used for put request
        STOP, // used for disconnecting client from master
        REGISTER, // used for registering worker server in master
        SHUTDOWN, // used for shutdown worker server from master and master server from client
        ACKNOWLEDGE, // used as response for successfully enqueued line
        OVERSIZE, // used for indication worker's oversized queue
        EMPTY, // used for indication worker's empty queue
        ERR, // used when unhandled exception occurs
        DATA // used as header for dequeued line
    }
}
