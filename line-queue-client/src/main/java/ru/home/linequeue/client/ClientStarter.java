package ru.home.linequeue.client;

import ru.home.linequeue.client.network.WorkerClient;
import ru.home.linequeue.config.Configuration;

public class ClientStarter {

    public static void main(String[] args) {
        var clientStarter = new ClientStarter();
        var clientConfig = Configuration.createFromArgs(args, new ClientConfig());
        clientStarter.startWorkerClient(clientConfig);
    }

    private void startWorkerClient(ClientConfig config) {
        WorkerClient client = new WorkerClient("localhost", 10042);
        client.start();
    }
}
