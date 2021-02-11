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

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class MasterChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static final Logger log = LoggerFactory.getLogger(MasterChannelInitializer.class.getName());

    private final MasterServer masterServer;

    // we should hold single instance of every working collection
    // (workersChannels, requestBuffer, linesToWorkers) for all handlers
    private final Map<String, ChannelHandlerContext> workersChannels = new ConcurrentHashMap<>();
    private final RequestBuffer requestBuffer = new RequestBuffer();
    private final ConcurrentNavigableMap<Long, String> linesToWorkers = new ConcurrentSkipListMap<>(Comparator.naturalOrder());

    private final WorkerChoosingStrategy workerChoosingStrategy = new RandomWorkerChoosingStrategy();

    public MasterChannelInitializer(MasterServer masterServer) {
        this.masterServer = masterServer;
    }


    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(
                new ObjectEncoder(),
                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                new MasterMessageHandler(masterServer, workersChannels, requestBuffer, linesToWorkers, workerChoosingStrategy));
    }

}

