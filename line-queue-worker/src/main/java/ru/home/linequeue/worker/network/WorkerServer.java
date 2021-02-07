package ru.home.linequeue.worker.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import ru.home.linequeue.worker.queue.QueueKeeper;

/**
 * In fact this is not a server, but client that holds connection to master
 */
public class WorkerServer {

    private final String workerHost;
    private final int workerPort;
    private final String masterHost;
    private final int masterPort;
    private final QueueKeeper queueKeeper;

    public WorkerServer(String workerHost, int workerPort, String masterHost, int masterPort, int queueCapacity) {
        this.workerHost = workerHost;
        this.workerPort = workerPort;
        this.masterHost = masterHost;
        this.masterPort = masterPort;
        this.queueKeeper = new QueueKeeper(queueCapacity);
    }

    public void start() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(
                                    new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new WorkerMessageHandler(queueKeeper, workerHost, workerPort));
                        }
                    });

            b.connect(masterHost, masterPort).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
