package ru.home.linequeue.test;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

import java.io.Reader;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

public class OneWorkerOneClientIntegrationTest {

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    private static IntegrationTestHarness testHarness;

    @BeforeClass
    public static void setUp() throws InterruptedException {
        testHarness = new IntegrationTestHarness();
        testHarness.startCluster();
    }

    @Test
    public void simplePutAndGetTest() throws InterruptedException {
        Reader reader = new StringReader("put abc\nget 1\nquit\n");
        testHarness.startClient(reader);
        assertEquals("abc\n", systemOutRule.getLogWithNormalizedLineSeparator());
    }

    @Test
    public void put3AndGet1Test() throws InterruptedException {
        Reader reader = new StringReader("put abc\nput def\nput ghi\nget 3\nquit\n");
        testHarness.startClient(reader);
        testHarness.compareWithoutOrdering("abc\ndef\nghi\n", systemOutRule.getLogWithNormalizedLineSeparator());
    }

    @Test
    public void putsAndGetsTest() throws InterruptedException {
        Reader reader = new StringReader("put abc\nput def\nput ghi\nget 3\n" +
                "put jkl\nput mno\nput pqr\nget 3\n" +
                "quit\n");
        testHarness.startClient(reader);
        testHarness.compareWithoutOrdering("abc\ndef\nghi\njkl\nmno\npqr\n", systemOutRule.getLogWithNormalizedLineSeparator());
    }
}
