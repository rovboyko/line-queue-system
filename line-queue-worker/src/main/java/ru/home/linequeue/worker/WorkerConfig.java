package ru.home.linequeue.worker;

import ru.home.linequeue.config.Configuration;

public class WorkerConfig extends Configuration {

    //Server properties
    public static String WORKER_PORT = "worker.port";

    //Worker properties
    public static String QUEUE_CAPACITY = "worker.queue.capacity";

    //DEFAULT VALUES
    private static final int DEFAULT_WORKER_PORT = 10042;
    private static final int DEFAULT_QUEUE_CAPACITY = 100 * 1000;

    static {
        argProperties.add("--"+WORKER_PORT);
        argProperties.add("--"+QUEUE_CAPACITY);
    }

    public int getWorkerPort() {
        return Integer.parseInt(properties.getProperty(WORKER_PORT, String.valueOf(DEFAULT_WORKER_PORT)));
    }

    public int getQueueCapacity() {
        return Integer.parseInt(properties.getProperty(QUEUE_CAPACITY, String.valueOf(DEFAULT_QUEUE_CAPACITY)));
    }
}
