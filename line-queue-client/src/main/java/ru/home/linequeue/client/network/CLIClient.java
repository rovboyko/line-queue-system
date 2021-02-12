package ru.home.linequeue.client.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import ru.home.linequeue.client.console.ConsoleListener;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicInteger;

public class CLIClient {

    private final String masterHost;
    private final int masterPort;
    private final Reader reader;
    private final AtomicInteger unAnsweredMessages = new AtomicInteger();

    public CLIClient(String masterHost, int masterPort, Reader reader) {
        this.masterHost = masterHost;
        this.masterPort = masterPort;
        this.reader = reader;
    }

    public void start() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(
                                    new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new CLIClientMessageHandler(unAnsweredMessages));
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);;

            // Start the connection attempt.
            Channel ch = b.connect(masterHost, masterPort).sync().channel();

            // Start the console listener for communicating with console client
            // and provide its messages to master service
            ConsoleListener consoleListener = new ConsoleListener(reader, ch, unAnsweredMessages);
            consoleListener.start();

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }
    }

    public Reader getReader() {
        return reader;
    }
}
