package ru.home.linequeue.client;

import ru.home.linequeue.client.network.CLIClient;
import ru.home.linequeue.config.Configuration;

import java.io.InputStreamReader;

public class ClientStarter {

    public static void main(String[] args) {
        ClientStarter clientStarter = new ClientStarter();
        ClientConfig clientConfig = Configuration.createFromArgs(args, new ClientConfig());
        clientStarter.startMasterClient(clientConfig);
    }

    private void startMasterClient(ClientConfig config) {
        CLIClient client = new CLIClient(
                config.getMasterHost(),
                config.getMasterPort(),
                new InputStreamReader(System.in));
        client.start();
    }
}
