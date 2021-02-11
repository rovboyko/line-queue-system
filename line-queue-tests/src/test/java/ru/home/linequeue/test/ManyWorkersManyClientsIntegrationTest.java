package ru.home.linequeue.test;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ManyWorkersManyClientsIntegrationTest {

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    private static IntegrationTestHarness testHarness;

    @BeforeClass
    public static void setUp() throws InterruptedException {
        testHarness = new IntegrationTestHarness();
        testHarness.setNumberOfWorkers(3);
        testHarness.startCluster();
    }

    @Test
    public void simplePutAndGetTest() throws InterruptedException, ExecutionException {
        Reader reader1 = new StringReader("put abc\nget 1\nquit\n");
        Reader reader2 = new StringReader("put def\nget 1\nquit\n");
        CompletableFuture<Void> clientFuture1 = CompletableFuture.runAsync(() -> testHarness.startClient(reader1));
        CompletableFuture<Void> clientFuture2 = CompletableFuture.runAsync(() -> testHarness.startClient(reader2));
        clientFuture1
                .thenCombine(clientFuture2, (unused1, unused2) -> "done")
                .get();
        testHarness.compareWithoutOrdering("abc\ndef\n", systemOutRule.getLogWithNormalizedLineSeparator());
    }

    @Test
    public void put3AndGet1Test() throws InterruptedException, ExecutionException {
        Reader reader1 = new StringReader("put abc\nput def\nput ghi\nget 3\nquit\n");
        Reader reader2 = new StringReader("put abc\nput def\nput ghi\nget 3\nquit\n");
        Reader reader3 = new StringReader("put abc\nput def\nput ghi\nget 3\nquit\n");
        CompletableFuture<Void> clientFuture1 = CompletableFuture.runAsync(() -> testHarness.startClient(reader1));
        CompletableFuture<Void> clientFuture2 = CompletableFuture.runAsync(() -> testHarness.startClient(reader2));
        CompletableFuture<Void> clientFuture3 = CompletableFuture.runAsync(() -> testHarness.startClient(reader3));
        clientFuture1
                .thenCombine(clientFuture2, (unused1, unused2) -> "done")
                .thenCombine(clientFuture3, (unused1, unused2) -> "done")
                .get();

        String expected = "abc\ndef\nghi\nabc\ndef\nghi\nabc\ndef\nghi\n";
        testHarness.compareWithoutOrdering(expected, systemOutRule.getLogWithNormalizedLineSeparator());
    }

    @Test
    public void putsAndGetsTest() throws InterruptedException, ExecutionException {
        Reader reader1 = new StringReader("put abc1\nput def1\nput ghi1\nget 3\n" +
                "put jkl1\nput mno1\nput pqr1\nget 3\n" +
                "quit\n");
        Reader reader2 = new StringReader("put abc2\nput def2\nput ghi2\nget 3\n" +
                "put jkl2\nput mno2\nput pqr2\nget 3\n" +
                "quit\n");
        Reader reader3 = new StringReader("put abc3\nput def3\nput ghi3\nget 3\n" +
                "put jkl3\nput mno3\nput pqr3\nget 3\n" +
                "quit\n");
        Reader reader4 = new StringReader("put abc4\nput def4\nput ghi4\nget 3\n" +
                "put jkl4\nput mno4\nput pqr4\nget 3\n" +
                "quit\n");
        CompletableFuture<Void> clientFuture1 = CompletableFuture.runAsync(() -> testHarness.startClient(reader1));
        CompletableFuture<Void> clientFuture2 = CompletableFuture.runAsync(() -> testHarness.startClient(reader2));
        CompletableFuture<Void> clientFuture3 = CompletableFuture.runAsync(() -> testHarness.startClient(reader3));
        CompletableFuture<Void> clientFuture4 = CompletableFuture.runAsync(() -> testHarness.startClient(reader4));
        clientFuture1
                .thenCombine(clientFuture2, (unused1, unused2) -> "done")
                .thenCombine(clientFuture3, (unused1, unused2) -> "done")
                .thenCombine(clientFuture4, (unused1, unused2) -> "done")
                .get();
        String expected = "abc1\ndef1\nghi1\njkl1\nmno1\npqr1\n" +
                "abc2\ndef2\nghi2\njkl2\nmno2\npqr2\n" +
                "abc3\ndef3\nghi3\njkl3\nmno3\npqr3\n" +
                "abc4\ndef4\nghi4\njkl4\nmno4\npqr4\n";
        testHarness.compareWithoutOrdering(expected, systemOutRule.getLogWithNormalizedLineSeparator());
    }
}
