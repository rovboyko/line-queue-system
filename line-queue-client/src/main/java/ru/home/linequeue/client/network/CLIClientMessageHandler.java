package ru.home.linequeue.client.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.home.linequeue.messages.Message;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static ru.home.linequeue.messages.Message.Type.ACKNOWLEDGE;
import static ru.home.linequeue.messages.Message.Type.DATA;
import static ru.home.linequeue.messages.Message.Type.EMPTY;
import static ru.home.linequeue.messages.Message.Type.ERR;
import static ru.home.linequeue.messages.Message.Type.NOT_ENOUGH;
import static ru.home.linequeue.messages.Message.Type.OVERSIZE;

public class CLIClientMessageHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(CLIClientMessageHandler.class.getName());

    private final AtomicInteger unAnsweredMessages;

    public CLIClientMessageHandler(AtomicInteger unAnsweredMessages) {
        this.unAnsweredMessages = unAnsweredMessages;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("CLIENT: receive msg " + msg);
        processMessage((Message) msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void processMessage(Message msg) {
        if (msg.getType().equals(ACKNOWLEDGE)) {
            // simply do nothing
            unAnsweredMessages.decrementAndGet();
        } else if (msg.getType().equals(DATA)) {
            System.out.println(msg.getBody());
            unAnsweredMessages.decrementAndGet();
        } else if (msg.getType().equals(NOT_ENOUGH)) {
            System.out.println(ERR);
            int linesCnt = Integer.parseInt(msg.getBody());
            log.info(String.format("queue doesn't contain %s lines", linesCnt));
            IntStream.range(0, linesCnt).forEach(n -> unAnsweredMessages.decrementAndGet());
        } else if (Arrays.asList(OVERSIZE, EMPTY, ERR).contains(msg.getType())) {
            // something bad happened
            System.out.println(ERR);
            log.error("MASTER: receives fatal response - " + msg);
            unAnsweredMessages.decrementAndGet();
        } else {
            log.warn("MASTER: message with unexpected type - " + msg);
        }
    }
}