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

public class MasterClient {

    private static final Logger log = LoggerFactory.getLogger(MasterClient.class.getName());

    private final String masterHost;
    private final int masterPort;

    public MasterClient(String masterHost, int masterPort) {
        this.masterHost = masterHost;
        this.masterPort = masterPort;
    }

    public void start() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientMasterChannelInitializer());

            // Start the connection attempt.
            Channel ch = b.connect(masterHost, masterPort).sync().channel();

            // Read commands from the stdin.
            ChannelFuture lastWriteFuture = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            boolean close = false;
            while(true) {
                String line = in.readLine();
                long currentTime = System.currentTimeMillis();
                if (line == null) {
                    break;
                }

                Message msg;
                if (line.equalsIgnoreCase("GET")) {
                    msg = new Message(GET, null, currentTime, 0);
                } else if (line.substring(0, 3).equalsIgnoreCase("PUT")) {
                    line = line.substring(4);
                    msg = new Message(PUT, line, currentTime, 0);
                } else if ("quit".equals(line.toLowerCase())) {
                    msg = new Message(STOP, null, currentTime, 0);
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
