package ru.home.linequeue.client.console;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.home.linequeue.messages.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static ru.home.linequeue.messages.Message.Type.GET;
import static ru.home.linequeue.messages.Message.Type.PUT;
import static ru.home.linequeue.messages.Message.Type.QUIT;
import static ru.home.linequeue.messages.Message.Type.SHUTDOWN;

public class ConsoleListener {
    private final Reader reader;
    private final Channel channel;
    private final AtomicInteger unAnsweredMessages;

    private static final Logger log = LoggerFactory.getLogger(ConsoleListener.class.getName());

    public ConsoleListener(Reader reader, Channel channel, AtomicInteger unAnsweredMessages) {
        this.reader = reader;
        this.channel = channel;
        this.unAnsweredMessages = unAnsweredMessages;
    }

    public void start() throws IOException, InterruptedException {
        // Read commands from the stdin.
        ChannelFuture lastWriteFuture = null;
        BufferedReader in = new BufferedReader(reader);
        boolean close = false;
        while(true) {
            String line = in.readLine();
            if (line == null) {
                break;
            }

            String command = line.trim();

            if ("GET".equalsIgnoreCase(command.substring(0, 3))) {
                lastWriteFuture = processGet(command);
            } else if ("PUT".equalsIgnoreCase(command.substring(0, 3))) {
                lastWriteFuture = processPut(command);
            } else if ("QUIT".equalsIgnoreCase(command)) {
                lastWriteFuture = processQuit();
                close = true;
            } else if ("SHUTDOWN".equalsIgnoreCase(command)) {
                lastWriteFuture = processShutdown();
                close = true;
            } else {
                log.info("Unknown input command: " + command);
                continue;
            }

            if (close) {
                // just waiting for all GET and PUT messages to be acknowledged
                while (unAnsweredMessages.get() > 0) {
                    //noinspection BusyWait
                    Thread.sleep(100);
                }
                lastWriteFuture.addListener(ChannelFutureListener.CLOSE);
                break;
            }
        }

        // Wait until all messages are flushed before closing the channel.
        if (lastWriteFuture != null) {
            lastWriteFuture.sync();
        }
    }

    // should be able to process different types of get commands:
    //    - get 7
    //    - GET 1
    //    - GEt    8
    private ChannelFuture processGet(String command) {
        // trying to obtain the number of lines for getting from our queue
        int linesCnt;
        try {
            linesCnt = Integer.parseInt(command.substring(3).trim());
        } catch (NumberFormatException e) {
            System.out.println("ERR");
            log.error("Can't get number from GET command: " + command);
            return null;
        }
        Message msg = new Message(GET, String.valueOf(linesCnt), System.currentTimeMillis(), 0);
        IntStream.range(0, linesCnt).forEach(n -> unAnsweredMessages.incrementAndGet());
        return channel.writeAndFlush(msg);
    }

    // should be able to process different types of put commands:
    //    - put abc
    //    - PUT abc def
    //    - Put    abc
    private ChannelFuture processPut(String command) {
        // trying to obtain to be put into queue
        String line = command.substring(3).trim();
        Message msg = new Message(PUT, line, System.currentTimeMillis(), 0);
        unAnsweredMessages.incrementAndGet();
        return channel.writeAndFlush(msg);
    }

    private ChannelFuture processQuit() {
        Message msg = new Message(QUIT, null, System.currentTimeMillis(), 0);
        return channel.writeAndFlush(msg);
    }

    private ChannelFuture processShutdown() {
        Message msg = new Message(SHUTDOWN, null, System.currentTimeMillis(), 0);
        return channel.writeAndFlush(msg);
    }
}
