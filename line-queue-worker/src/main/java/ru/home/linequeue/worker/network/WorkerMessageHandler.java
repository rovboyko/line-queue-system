package ru.home.linequeue.worker.network;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.home.linequeue.messages.Message;
import ru.home.linequeue.utils.Checker;
import ru.home.linequeue.worker.queue.QueueElement;
import ru.home.linequeue.worker.queue.QueueKeeper;

import static io.netty.channel.ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE;
import static ru.home.linequeue.messages.Message.Type.*;
import static ru.home.linequeue.utils.Checker.checkCondition;

public class WorkerMessageHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(WorkerMessageHandler.class.getName());

    private final QueueKeeper queueKeeper;
    private final String workerHost;
    private final int workerPort;

    public WorkerMessageHandler(QueueKeeper queueKeeper, String workerHost, int workerPort) {
        this.queueKeeper = queueKeeper;
        this.workerHost = workerHost;
        this.workerPort = workerPort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Message registerMsg = new Message(REGISTER, workerHost + ":" + workerPort, System.currentTimeMillis(), 0);
        log.info("WORKER: send register message " + registerMsg);
        ChannelFuture future = ctx.writeAndFlush(registerMsg);
        future.addListener(FIRE_EXCEPTION_ON_FAILURE);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("WORKER: receive msg " + msg);
        checkCondition(msg instanceof Message, "WORKER: wrong message class - " + msg.getClass().getName());
        processMessage(ctx, (Message) msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void processMessage(ChannelHandlerContext ctx, Message in) {
        if (in.getType().equals(GET)) {
            getFromQueue(ctx, in);
        } else if (in.getType().equals(PUT)) {
            putToQueue(ctx, in);
        } else if (in.getType().equals(SHUTDOWN)) {
            ctx.channel().close();
            log.info("closing the connection - " + in);
        } else {
            log.warn("message with unexpected type - " + in);
        }
    }

    private void putToQueue(ChannelHandlerContext ctx, Message in) {
        Checker.checkCondition(in.getReqId() > 0, "put request should contain reqId");
        try {
            if (queueKeeper.offer(new QueueElement(in.getBody(), in.getTs()))) {
                ctx.write(new Message(ACKNOWLEDGE, null, 0, in.getReqId()));
            } else {
                ctx.write(new Message(OVERSIZE, null, 0, in.getReqId()));
            }
        } catch (InterruptedException e) {
            ctx.write(new Message(ERR, null, 0, in.getReqId()));
        }
    }

    private void getFromQueue(ChannelHandlerContext ctx, Message in) {
        Checker.checkCondition(in.getReqId() > 0, "request for data should contain reqId");
        try {
            QueueElement elm = queueKeeper.poll();
            if (elm != null) {
                ctx.write(new Message(DATA, elm.getBody(), elm.getTs(), in.getReqId()));
            } else {
                ctx.write(new Message(EMPTY, null, 0, in.getReqId()));
            }
        } catch (InterruptedException e) {
            ctx.write(new Message(ERR, null, 0, in.getReqId()));
        }
    }
}