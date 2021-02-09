package ru.home.linequeue.client.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.home.linequeue.client.console.ConsoleListener;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class CLIClient {

    private static final Logger log = LoggerFactory.getLogger(CLIClient.class.getName());

    private final String masterHost;
    private final int masterPort;

    public CLIClient(String masterHost, int masterPort) {
        this.masterHost = masterHost;
        this.masterPort = masterPort;
    }

    public void start() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new CLIClientChannelInitializer());

            // Start the connection attempt.
            Channel ch = b.connect(masterHost, masterPort).sync().channel();
            // Create the console reader
            Reader reader = new InputStreamReader(System.in);

            // Start the console listener for communicating with console client
            // and provide its messages to master service
            ConsoleListener consoleListener = new ConsoleListener(reader, ch);
            consoleListener.start();

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
