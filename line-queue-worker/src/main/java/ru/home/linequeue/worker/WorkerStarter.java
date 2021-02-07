package ru.home.linequeue.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.home.linequeue.config.Configuration;
import ru.home.linequeue.worker.network.WorkerServer;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class WorkerStarter {

    private static final Logger log = LoggerFactory.getLogger(WorkerStarter.class.getName());
    private static final String defaultWorkerHost = "unknown-worker";

    public static void main(String[] args) {
        WorkerStarter workerStarter = new WorkerStarter();
        WorkerConfig workerConfig = Configuration.createFromArgs(args, new WorkerConfig());
        workerStarter.startWorkerServer(workerConfig);
    }

    private void startWorkerServer(WorkerConfig config) {
        String workerHost;
        try {
            workerHost = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error(String.format("WORKER: unable to obtain inet address, using '%s' as default", defaultWorkerHost));
            workerHost = defaultWorkerHost;
        }
        WorkerServer server = new WorkerServer(
                workerHost,
                config.getWorkerPort(),
                config.getMasterHost(),
                config.getMasterPort(),
                config.getQueueCapacity()
        );
        server.start();
    }
}
