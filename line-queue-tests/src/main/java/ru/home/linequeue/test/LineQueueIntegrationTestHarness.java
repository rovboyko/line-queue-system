package ru.home.linequeue.test;

import ru.home.linequeue.client.ClientConfig;
import ru.home.linequeue.client.network.CLIClient;
import ru.home.linequeue.config.Configuration;
import ru.home.linequeue.master.MasterStarter;
import ru.home.linequeue.worker.WorkerStarter;

import java.io.Reader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class LineQueueIntegrationTestHarness {

    private final int numberOfMasters = 1;
    private int numberOfWorkers = 1;
    // todo: make it possible to be grater than 1
    private final int numberOfClients = 1;

    protected int workerStartingPort = 10043;
    ExecutorService executor;

    private final CountDownLatch clientCountDownLatch = new CountDownLatch(1);

    public void startCluster(Reader reader) throws InterruptedException {
        executor = Executors.newFixedThreadPool(20);
        startMaster();
        startWorkers();
        startClient(reader);
        clientCountDownLatch.await(10, TimeUnit.SECONDS);
    }

    protected void startMaster() throws InterruptedException {
        String[] args = {"--config", "master-config.properties"};
        executor.execute(() -> MasterStarter.main(args));
        // Yea, I know, this is ugly=))
        Thread.sleep(2000);
    }

    protected void startWorkers() throws InterruptedException {
        IntStream.range(workerStartingPort, workerStartingPort + numberOfWorkers)
                .forEach(port ->
                    executor.execute(() -> WorkerStarter.main(
                            new String[]{"--config", "worker-config.properties",
                                    "--worker.port", String.valueOf(port)})
                    )
                );
        // Yea, I know, this is ugly=))
        Thread.sleep(2000);
    }

    protected void startClient(Reader reader) {
        String[] args = {"--config", "client-config.properties"};
        ClientConfig config = Configuration.createFromArgs(args, new ClientConfig());
        executor.execute(() -> {
            CLIClient client = new CLIClient(
                config.getMasterHost(),
                config.getMasterPort(),
                reader);
            client.start();
        clientCountDownLatch.countDown();
        });
    }

    public void setNumberOfWorkers(int numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
    }
}