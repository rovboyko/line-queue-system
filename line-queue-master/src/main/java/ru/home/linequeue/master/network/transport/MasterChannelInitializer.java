package ru.home.linequeue.master.network.transport;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.home.linequeue.master.network.process.RequestBuffer;

import java.util.Map;

public class MasterChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static final Logger log = LoggerFactory.getLogger(MasterChannelInitializer.class.getName());

    private final Map<String, ChannelHandlerContext> workersChannels;
    private final RequestBuffer requestBuffer;

    public MasterChannelInitializer(Map<String, ChannelHandlerContext> workersChannels, RequestBuffer requestBuffer) {
        this.workersChannels = workersChannels;
        this.requestBuffer = requestBuffer;
    }


    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(
                new ObjectEncoder(),
                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                new MasterMessageHandler(workersChannels, requestBuffer));
    }

}

