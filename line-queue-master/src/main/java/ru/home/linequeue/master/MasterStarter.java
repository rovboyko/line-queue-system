package ru.home.linequeue.master;

import ru.home.linequeue.config.Configuration;
import ru.home.linequeue.master.network.MasterServer;

public class MasterStarter {

    public static void main(String[] args) {
        MasterStarter masterStarter = new MasterStarter();
        MasterConfig masterConfig = Configuration.createFromArgs(args, new MasterConfig());
        masterStarter.startMasterServer(masterConfig);
    }

    private void startMasterServer(MasterConfig config) {
        MasterServer server = new MasterServer(config.getMasterPort());
        server.start();
    }
}
