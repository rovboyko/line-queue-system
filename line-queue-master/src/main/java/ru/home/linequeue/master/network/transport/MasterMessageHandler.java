package ru.home.linequeue.master.network.transport;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.home.linequeue.master.network.process.RequestBuffer;
import ru.home.linequeue.messages.Message;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static ru.home.linequeue.messages.Message.Type.*;
import static ru.home.linequeue.utils.Checker.checkCondition;

/**
 * Handler uses for:
 * - receiving worker register messages and add their ChannelHandlerContext to stored map
 * -
 */
public class MasterMessageHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(MasterMessageHandler.class.getName());

    private final Map<String, ChannelHandlerContext> workersChannels;
    private final RequestBuffer requestBuffer;

    public MasterMessageHandler(Map<String, ChannelHandlerContext> workersChannels, RequestBuffer requestBuffer) {
        this.workersChannels = workersChannels;
        this.requestBuffer = requestBuffer;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.debug("MASTER: receive msg " + msg);
        checkCondition(msg instanceof Message, "MASTER: wrong message class - " + msg.getClass().getName());
        processMessage(ctx, (Message) msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void processMessage(ChannelHandlerContext ctx, Message in) {
        if (in.getType().equals(REGISTER)) {
            registerWorker(ctx, in);
        } else if (in.getType().equals(GET) || in.getType().equals(PUT)) {
            // this is message from client and it should be provided to worker
            provideToWorker(ctx, in);
        } else if (Arrays.asList(ACKNOWLEDGE, OVERSIZE, DATA, EMPTY, ERR).contains(in.getType())) {
            // this is message from worker and it should be provided back to client
            provideToClient(in);
        } else {
            log.warn("MASTER: message with unexpected type - " + in);
        }
    }

    private void registerWorker(ChannelHandlerContext ctx, Message in) {
        String workerName = in.getBody();
        log.info("MASTER: worker with name " + workerName + " was successfully registered");
        // todo: handle already existing worker names
        workersChannels.put(in.getBody(), ctx);
    }

    private void provideToWorker(ChannelHandlerContext clientCtx, Message in) {
        // todo: implement pattern for choosing workers
        Optional<Map.Entry<String, ChannelHandlerContext>> optAnyChannel = workersChannels.entrySet().stream().findAny();
        if (optAnyChannel.isPresent()) {
            ChannelHandlerContext anyWorkerChannel = optAnyChannel.get().getValue();
            long reqId = requestBuffer.addRequest(clientCtx);
            in.setReqId(reqId);
            anyWorkerChannel.writeAndFlush(in);
            // todo: implement map with reqId->worker for ordering purposes
            log.info("MASTER: message " + in + " was successfully sent to worker " + optAnyChannel.get().getKey());
        } else {
            clientCtx.writeAndFlush(new Message(ERR, "No registered workers", 0, 0));
        }
    }

    private void provideToClient(Message in) {
        ChannelHandlerContext clientChannel = requestBuffer.getChannel(in.getReqId());
        if (clientChannel != null) {
            clientChannel.writeAndFlush(in);
        } else {
            log.error("Can't find client channel for message " + in);
        }
    }
}