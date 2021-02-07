package ru.home.linequeue.master;

import ru.home.linequeue.config.Configuration;
import ru.home.linequeue.master.network.transport.MasterServer;

public class MasterStarter {

    public static void main(String[] args) {
        var masterStarter = new MasterStarter();
        var masterConfig = Configuration.createFromArgs(args, new MasterConfig());
        masterStarter.startMasterServer(masterConfig);
    }

    private void startMasterServer(MasterConfig config) {
        MasterServer server = new MasterServer(config.getMasterPort());
        server.start();
    }
}
