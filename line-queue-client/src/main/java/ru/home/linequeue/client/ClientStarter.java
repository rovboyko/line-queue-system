package ru.home.linequeue.client;

import ru.home.linequeue.client.network.CLIClient;
import ru.home.linequeue.config.Configuration;

public class ClientStarter {

    public static void main(String[] args) {
        var clientStarter = new ClientStarter();
        var clientConfig = Configuration.createFromArgs(args, new ClientConfig());
        clientStarter.startMasterClient(clientConfig);
    }

    private void startMasterClient(ClientConfig config) {
        CLIClient client = new CLIClient(
                config.getMasterHost(),
                config.getMasterPort());
        client.start();
    }
}
