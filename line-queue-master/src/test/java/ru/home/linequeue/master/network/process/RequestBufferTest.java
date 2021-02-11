package ru.home.linequeue.master.network.process;

import io.netty.channel.ChannelHandlerContext;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class RequestBufferTest {

    private final int numThreads = 10;
    private final int elementsByThread = 100;
    private final ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
    private final RequestBuffer buffer = new RequestBuffer();
    private final List<ChannelHandlerContext> resultSet = new ArrayList<>(elementsByThread * numThreads);
    private final TestChannelHandlerContext testContext = new TestChannelHandlerContext();
    private final CountDownLatch countDownLatch = new CountDownLatch(numThreads);

    @Test
    public void testSyncAccess() throws InterruptedException {
        SyncTestRunner syncTestRunner = new SyncTestRunner();
        IntStream.range(0, numThreads)
                .forEach(i -> executorService.execute(syncTestRunner));
        countDownLatch.await(10, TimeUnit.SECONDS);
        IntStream.range(1, numThreads * elementsByThread + 1)
                .forEach(i -> {
                    ChannelHandlerContext channel = buffer.getChannel(i);
                    if (channel != null) {
                        resultSet.add(channel);
                    }
                });

        assertEquals(numThreads * elementsByThread, resultSet.size());
    }

    private class SyncTestRunner implements Runnable {
        @Override
        public void run() {
            IntStream.range(0, elementsByThread)
                    .forEach(i -> buffer.addRequest(testContext));
            countDownLatch.countDown();
        }
    }


}