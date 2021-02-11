package ru.home.linequeue.test;

import ru.home.linequeue.client.ClientConfig;
import ru.home.linequeue.client.network.CLIClient;
import ru.home.linequeue.config.Configuration;
import ru.home.linequeue.master.MasterStarter;
import ru.home.linequeue.worker.WorkerStarter;

import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.Assert.assertTrue;

public class IntegrationTestHarness {

    private int numberOfWorkers = 1;

    protected int workerStartingPort = 10043;
    ExecutorService executor;

    public void startCluster() throws InterruptedException {
        executor = Executors.newFixedThreadPool(20);
        startMaster();
        startWorkers();

    }

    private void startMaster() throws InterruptedException {
        String[] args = {"--config", "master-config.properties"};
        executor.execute(() -> MasterStarter.main(args));
        // Yea, I know, this is ugly=))
        Thread.sleep(2000);
    }

    private void startWorkers() throws InterruptedException {
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

    public void startClient(Reader reader) {
        CountDownLatch clientCountDownLatch = new CountDownLatch(1);
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
        try {
            clientCountDownLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setNumberOfWorkers(int numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
    }

    public void compareWithoutOrdering(String expStr, String actStr) {
        List<String> expected = Arrays.asList(expStr.split("\\n"));
        List<String> actual = Arrays.asList(actStr.split("\\n"));
        assertTrue(expected.size() == actual.size() && expected.containsAll(actual) && actual.containsAll(expected));
    }
}