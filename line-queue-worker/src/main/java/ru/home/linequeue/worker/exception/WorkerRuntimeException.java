package ru.home.linequeue.worker.exception;

import java.net.InetAddress;
import java.net.UnknownHostException;

//todo: use or drop
public class WorkerRuntimeException extends RuntimeException {

    private static final String HOSTNAME;

    static {
        String tempHostname;
        try {
            tempHostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            tempHostname = "unknown-worker";
        }
        HOSTNAME = tempHostname;
    }

    public WorkerRuntimeException(String message) {
        super(String.format("Worker %s encountered a problem: %s", HOSTNAME, message));
    }
}
