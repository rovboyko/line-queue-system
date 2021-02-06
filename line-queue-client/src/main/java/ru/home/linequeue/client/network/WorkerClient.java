package ru.home.linequeue.client.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.home.linequeue.messages.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static ru.home.linequeue.messages.Message.Type.*;

public class WorkerClient {

    private static final Logger log = LoggerFactory.getLogger(WorkerClient.class.getName());

    private final String workerHost;
    private final int workerPort;

    public WorkerClient(String workerHost, int workerPort) {
        this.workerHost = workerHost;
        this.workerPort = workerPort;
    }

    public void start() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientWorkerChannelInitializer());

            // Start the connection attempt.
            Channel ch = b.connect(workerHost, workerPort).sync().channel();

            // Read commands from the stdin.
            ChannelFuture lastWriteFuture = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            boolean close = false;
            while(true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }

                Message msg;
                if (line.equalsIgnoreCase("GET")) {
                    msg = new Message(GET, null);
                } else if (line.substring(0, 3).equalsIgnoreCase("PUT")) {
                    line = line.substring(4);
                    msg = new Message(PUT, line);
                } else if ("quit".equals(line.toLowerCase())) {
                    msg = new Message(FINISHED, null);
                    close = true;
                } else {
                    log.info("Unknown input command: " + line);
                    continue;
                }

                // Sends the received line to the server.
                lastWriteFuture = ch.writeAndFlush(msg);

                if (close) {
                    lastWriteFuture.addListener(ChannelFutureListener.CLOSE);
                    break;
                }
            }

            // Wait until all messages are flushed before closing the channel.
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
