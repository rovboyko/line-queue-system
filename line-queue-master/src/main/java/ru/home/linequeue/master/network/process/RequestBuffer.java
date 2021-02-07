package ru.home.linequeue.master.network.process;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

public class RequestBuffer {

    // here we could use ConcurrentHashMap and AtomicLong, but
    // nevertheless we have to synchronize class methods because the action on
    // two thread-safe implementations isn't thread-safe by default
    private final Map<Long, ChannelHandlerContext> clientRequestsToChannels = new HashMap<>();
    private long requestCounter = 0;

    public synchronized long addRequest(ChannelHandlerContext ctx) {
        clientRequestsToChannels.put(++requestCounter, ctx);
        return requestCounter;
    }

    public synchronized ChannelHandlerContext getChannel(long requestId) {
        return clientRequestsToChannels.remove(requestId);
    }

}
