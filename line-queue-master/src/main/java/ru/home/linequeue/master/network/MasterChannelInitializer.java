package ru.home.linequeue.master.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.home.linequeue.master.network.process.RandomWorkerChoosingStrategy;
import ru.home.linequeue.master.network.process.RequestBuffer;
import ru.home.linequeue.master.network.process.WorkerChoosingStrategy;

import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;

public class MasterChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static final Logger log = LoggerFactory.getLogger(MasterChannelInitializer.class.getName());

    private final Map<String, ChannelHandlerContext> workersChannels;
    private final RequestBuffer requestBuffer;
    private final ConcurrentNavigableMap<Long, String> linesToWorkers;
    private final WorkerChoosingStrategy workerChoosingStrategy = new RandomWorkerChoosingStrategy();

    public MasterChannelInitializer(Map<String, ChannelHandlerContext> workersChannels,
                                    RequestBuffer requestBuffer,
                                    ConcurrentNavigableMap<Long, String> linesToWorkers) {
        this.workersChannels = workersChannels;
        this.requestBuffer = requestBuffer;
        this.linesToWorkers = linesToWorkers;
    }


    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(
                new ObjectEncoder(),
                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                new MasterMessageHandler(workersChannels, requestBuffer, linesToWorkers, workerChoosingStrategy));
    }

}

