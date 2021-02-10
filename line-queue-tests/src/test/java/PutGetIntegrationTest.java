import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import ru.home.linequeue.test.LineQueueIntegrationTestHarness;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PutGetIntegrationTest {

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    private LineQueueIntegrationTestHarness testHarness;

    @Before
    public void setUp() {
        testHarness = new LineQueueIntegrationTestHarness();
    }

    @Test
    public void simplePutAndGetTest() throws InterruptedException {
        Reader reader = new StringReader("put abc\nget 1\nquit\n");
        testHarness.startCluster(reader);
        assertEquals("abc\n", systemOutRule.getLogWithNormalizedLineSeparator());
    }

    @Test
    public void put3AndGet1Test() throws InterruptedException {
        Reader reader = new StringReader("put abc\nput def\nput ghi\nget 3\nquit\n");
        testHarness.startCluster(reader);
        compareWithoutOrdering("abc\ndef\nghi\n", systemOutRule.getLogWithNormalizedLineSeparator());
    }

    @Test
    public void putsAndGetsTest() throws InterruptedException {
        Reader reader = new StringReader("put abc\nput def\nput ghi\nget 3\n" +
                "put jkl\nput mno\nput pqr\nget 3\n" +
                "quit\n");
        testHarness.startCluster(reader);
        compareWithoutOrdering("abc\ndef\nghi\njkl\nmno\npqr\n", systemOutRule.getLogWithNormalizedLineSeparator());
    }

    @Test
    public void simplePutAndGetFor3WorkersTest() throws InterruptedException {
        testHarness.setNumberOfWorkers(3);
        Reader reader = new StringReader("put abc\nget 1\nquit\n");
        testHarness.startCluster(reader);
        assertEquals("abc\n", systemOutRule.getLogWithNormalizedLineSeparator());
    }

    @Test
    public void put3AndGet1For3WorkersTest() throws InterruptedException {
        testHarness.setNumberOfWorkers(3);
        Reader reader = new StringReader("put abc\nput def\nput ghi\nget 3\nquit\n");
        testHarness.startCluster(reader);
        compareWithoutOrdering("abc\ndef\nghi\n", systemOutRule.getLogWithNormalizedLineSeparator());
    }

    @Test
    public void putsAndGetsFor3WorkersTest() throws InterruptedException {
        testHarness.setNumberOfWorkers(3);
        Reader reader = new StringReader("put abc\nput def\nput ghi\nget 3\n" +
                "put jkl\nput mno\nput pqr\nget 3\n" +
                "quit\n");
        testHarness.startCluster(reader);
        compareWithoutOrdering("abc\ndef\nghi\njkl\nmno\npqr\n", systemOutRule.getLogWithNormalizedLineSeparator());
    }

    private void compareWithoutOrdering(String expStr, String actStr) {
        List<String> expected = Arrays.asList(expStr.split("\\n"));
        List<String> actual = Arrays.asList(actStr.split("\\n"));
        assertTrue(expected.size() == actual.size() && expected.containsAll(actual) && actual.containsAll(expected));
    }

}
