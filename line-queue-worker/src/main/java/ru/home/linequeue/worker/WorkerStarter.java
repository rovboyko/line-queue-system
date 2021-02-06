package ru.home.linequeue.worker;

import ru.home.linequeue.config.Configuration;
import ru.home.linequeue.worker.network.WorkerServer;

public class WorkerStarter {

    public static void main(String[] args) {
        var workerStarter = new WorkerStarter();
        var workerConfig = Configuration.createFromArgs(args, new WorkerConfig());
        workerStarter.startWorkerServer(workerConfig);
    }

    private void startWorkerServer(WorkerConfig config) {
        WorkerServer server = new WorkerServer(config.getWorkerPort());
        server.start();
    }
}
