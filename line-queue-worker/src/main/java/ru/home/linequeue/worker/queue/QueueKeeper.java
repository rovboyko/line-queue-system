package ru.home.linequeue.worker.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class QueueKeeper {

    // one second
    private static final long QUEUE_TIMEOUT = 1;

    private final ArrayBlockingQueue<QueueElement> queue;

    public QueueKeeper(int queueSize) {
        this.queue = new ArrayBlockingQueue<>(queueSize);
    }

    // returns true if element is put to queue or false if isn't
    public boolean offer(QueueElement elm) throws InterruptedException {
        return queue.offer(elm, QUEUE_TIMEOUT, TimeUnit.SECONDS);
    }

    // element from the head of queue
    public QueueElement poll() throws InterruptedException {
        return queue.poll(QUEUE_TIMEOUT, TimeUnit.SECONDS);
    }

}
