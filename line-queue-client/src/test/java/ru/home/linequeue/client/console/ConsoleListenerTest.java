package ru.home.linequeue.client.console;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;
import ru.home.linequeue.messages.Message;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static ru.home.linequeue.messages.Message.Type.*;

public class ConsoleListenerTest {

    private EmbeddedChannel channel = new EmbeddedChannel();

    @Test
    public void lowerGetTest() throws IOException, InterruptedException {
        Reader reader = new StringReader("get 1\nquit\n");
        ConsoleListener consoleListener = new ConsoleListener(reader, channel);
        consoleListener.start();
        Message expectedGetMsg = new Message(GET, "1", System.currentTimeMillis(), 0);
        Message actualGetMsg = channel.readOutbound();
        assertEquals(expectedGetMsg, actualGetMsg);

        Message expectedQuitMsg = new Message(QUIT, null, System.currentTimeMillis(), 0);
        Message actualQuitMsg = channel.readOutbound();
        assertEquals(expectedQuitMsg, actualQuitMsg);
    }

    @Test
    public void upperGetTest() throws IOException, InterruptedException {
        Reader reader = new StringReader("GET 1\nquit\n");
        ConsoleListener consoleListener = new ConsoleListener(reader, channel);
        consoleListener.start();
        Message expectedGetMsg = new Message(GET, "1", System.currentTimeMillis(), 0);
        Message actualGetMsg = channel.readOutbound();
        assertEquals(expectedGetMsg, actualGetMsg);

        Message expectedQuitMsg = new Message(QUIT, null, System.currentTimeMillis(), 0);
        Message actualQuitMsg = channel.readOutbound();
        assertEquals(expectedQuitMsg, actualQuitMsg);
    }

    @Test
    public void ExtraWhitespaceGetTest() throws IOException, InterruptedException {
        Reader reader = new StringReader("Get    1\nquit\n");
        ConsoleListener consoleListener = new ConsoleListener(reader, channel);
        consoleListener.start();
        Message expectedGetMsg = new Message(GET, "1", System.currentTimeMillis(), 0);
        Message actualGetMsg = channel.readOutbound();
        assertEquals(expectedGetMsg, actualGetMsg);

        Message expectedQuitMsg = new Message(QUIT, null, System.currentTimeMillis(), 0);
        Message actualQuitMsg = channel.readOutbound();
        assertEquals(expectedQuitMsg, actualQuitMsg);
    }

    @Test(expected = NumberFormatException.class)
    public void invalidNumberGetTest() throws IOException, InterruptedException {
        Reader reader = new StringReader("GET 1ds4\nquit\n");
        ConsoleListener consoleListener = new ConsoleListener(reader, channel);
        consoleListener.start();
    }

    @Test
    public void lowerPutTest() throws IOException, InterruptedException {
        Reader reader = new StringReader("put abc\nquit\n");
        ConsoleListener consoleListener = new ConsoleListener(reader, channel);
        consoleListener.start();
        Message expectedPutMsg = new Message(PUT, "abc", System.currentTimeMillis(), 0);
        Message actualPutMsg = channel.readOutbound();
        assertEquals(expectedPutMsg, actualPutMsg);

        Message expectedQuitMsg = new Message(QUIT, null, System.currentTimeMillis(), 0);
        Message actualQuitMsg = channel.readOutbound();
        assertEquals(expectedQuitMsg, actualQuitMsg);
    }

    @Test
    public void upperPutTest() throws IOException, InterruptedException {
        Reader reader = new StringReader("PUT abc def\nquit\n");
        ConsoleListener consoleListener = new ConsoleListener(reader, channel);
        consoleListener.start();
        Message expectedPutMsg = new Message(PUT, "abc def", System.currentTimeMillis(), 0);
        Message actualPutMsg = channel.readOutbound();
        assertEquals(expectedPutMsg, actualPutMsg);

        Message expectedQuitMsg = new Message(QUIT, null, System.currentTimeMillis(), 0);
        Message actualQuitMsg = channel.readOutbound();
        assertEquals(expectedQuitMsg, actualQuitMsg);
    }

    @Test
    public void ExtraWhitespacePutTest() throws IOException, InterruptedException {
        Reader reader = new StringReader("PUt    abc\nquit\n");
        ConsoleListener consoleListener = new ConsoleListener(reader, channel);
        consoleListener.start();
        Message expectedPutMsg = new Message(PUT, "abc", System.currentTimeMillis(), 0);
        Message actualPutMsg = channel.readOutbound();
        assertEquals(expectedPutMsg, actualPutMsg);

        Message expectedQuitMsg = new Message(QUIT, null, System.currentTimeMillis(), 0);
        Message actualQuitMsg = channel.readOutbound();
        assertEquals(expectedQuitMsg, actualQuitMsg);
    }

    @Test
    public void caseQuitTest() throws IOException, InterruptedException {
        Reader reader = new StringReader("QuiT\n");
        ConsoleListener consoleListener = new ConsoleListener(reader, channel);
        consoleListener.start();

        Message expectedQuitMsg = new Message(QUIT, null, System.currentTimeMillis(), 0);
        Message actualQuitMsg = channel.readOutbound();
        assertEquals(expectedQuitMsg, actualQuitMsg);
    }

    @Test
    public void caseShutDownTest() throws IOException, InterruptedException {
        Reader reader = new StringReader("shUTdoWN\nquit\n");
        ConsoleListener consoleListener = new ConsoleListener(reader, channel);
        consoleListener.start();
        Message expectedShtMsg = new Message(SHUTDOWN, null, System.currentTimeMillis(), 0);
        Message actualShtMsg = channel.readOutbound();
        assertEquals(expectedShtMsg, actualShtMsg);

        Message expectedQuitMsg = new Message(QUIT, null, System.currentTimeMillis(), 0);
        Message actualQuitMsg = channel.readOutbound();
        assertEquals(expectedQuitMsg, actualQuitMsg);
    }
}