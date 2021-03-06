package ru.home.linequeue.master.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.home.linequeue.master.network.process.RequestBuffer;
import ru.home.linequeue.master.network.process.strategy.WorkerChoosingStrategy;
import ru.home.linequeue.messages.Message;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentNavigableMap;

import static ru.home.linequeue.messages.Message.Type.ACKNOWLEDGE;
import static ru.home.linequeue.messages.Message.Type.DATA;
import static ru.home.linequeue.messages.Message.Type.EMPTY;
import static ru.home.linequeue.messages.Message.Type.ERR;
import static ru.home.linequeue.messages.Message.Type.GET;
import static ru.home.linequeue.messages.Message.Type.NOT_ENOUGH;
import static ru.home.linequeue.messages.Message.Type.OVERSIZE;
import static ru.home.linequeue.messages.Message.Type.PUT;
import static ru.home.linequeue.messages.Message.Type.QUIT;
import static ru.home.linequeue.messages.Message.Type.REGISTER;
import static ru.home.linequeue.messages.Message.Type.SHUTDOWN;
import static ru.home.linequeue.utils.Checker.checkCondition;

/**
 * Handler uses for:
 * - receiving worker register messages and add their ChannelHandlerContext to stored map
 * - receiving PUT and GET from clients and forwards them to workers
 * - receiving DATA or other technical messages from workers and provides them back to clients
 */
public class MasterMessageHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(MasterMessageHandler.class.getName());

    private final MasterServer masterServer;

    private final Map<String, ChannelHandlerContext> workersChannels;
    private final RequestBuffer requestBuffer;
    private final ConcurrentNavigableMap<Long, String> linesToWorkers;
    private final WorkerChoosingStrategy workerChoosingStrategy;


    public MasterMessageHandler(MasterServer masterServer, Map<String, ChannelHandlerContext> workersChannels,
                                RequestBuffer requestBuffer,
                                ConcurrentNavigableMap<Long, String> linesToWorkers,
                                WorkerChoosingStrategy workerChoosingStrategy) {
        this.masterServer = masterServer;
        this.workersChannels = workersChannels;
        this.requestBuffer = requestBuffer;
        this.linesToWorkers = linesToWorkers;
        this.workerChoosingStrategy = workerChoosingStrategy;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.debug("MASTER: receive msg " + msg);
        checkCondition(msg instanceof Message, "wrong message class - " + msg.getClass().getName());
        processMessage(ctx, (Message) msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void processMessage(ChannelHandlerContext ctx, Message in) {
        if (in.getType().equals(REGISTER)) {
            // this is message from worker and it should be processed here
            registerWorker(ctx, in);
        } else if (in.getType().equals(GET)) {
            // this is message from client and it should be provided to worker
            processGet(ctx, in);
        } else if (in.getType().equals(PUT)) {
            // this is message from client and it should be provided to worker
            processPut(ctx, in);
        } else if (in.getType().equals(SHUTDOWN)) {
            // this is message from client and it should initiate workers shutdown
            processShutdown(ctx, in);
        } else if (in.getType().equals(QUIT)) {
            // this is message from client and it should be processed here
            processQuit(in);
        } else if (Arrays.asList(ACKNOWLEDGE, OVERSIZE, DATA, EMPTY, ERR).contains(in.getType())) {
            // this is message from worker and it should be provided back to client
            provideToClient(in);
        } else {
            log.warn("message with unexpected type - " + in);
        }
    }

    private void registerWorker(ChannelHandlerContext ctx, Message in) {
        String workerName = in.getBody();
        log.info("worker with name " + workerName + " was successfully registered");
        // todo: handle already existing worker names
        workersChannels.put(workerName, ctx);
        workerChoosingStrategy.addWorker(workerName);
    }

    private void processPut(ChannelHandlerContext clientCtx, Message in) {
        Optional<String> optWorkerName = workerChoosingStrategy.getWorker();

        if (optWorkerName.isPresent()) {
            String workerName = optWorkerName.get();
            ChannelHandlerContext workerChannel = workersChannels.get(workerName);
            long reqId = requestBuffer.addRequest(clientCtx);
            in.setReqId(reqId);

            // Seems like here we need synchronization, but we don't because it's
            // enough for us to have this action ordering: send msg and then put id to map
            workerChannel.writeAndFlush(in);
            linesToWorkers.put(reqId, workerName);

            log.debug("message " + in + " was successfully sent to worker " + workerName);
        } else {
            clientCtx.writeAndFlush(new Message(ERR, "No registered workers", 0, 0));
        }
    }

    private void processGet(ChannelHandlerContext clientCtx, Message in) {

        int linesNumber = Integer.parseInt(in.getBody());

        if (linesToWorkers.size() < linesNumber) {
            in.setType(NOT_ENOUGH);
            clientCtx.writeAndFlush(in);
            return;
        }

        for (int i = 0; i < linesNumber; i++){
            Map.Entry<Long, String> reqIdToWorker = linesToWorkers.pollFirstEntry();
            String workerName = reqIdToWorker.getValue();

            log.debug("dequeue reqIdToWorker: reqId = " + reqIdToWorker.getKey() + " , worker = " + workerName);

            ChannelHandlerContext workerChannel = workersChannels.get(workerName);
            long reqId = requestBuffer.addRequest(clientCtx);
            Message getMsg = new Message(GET, "1", in.getTs(), reqId);
            workerChannel.writeAndFlush(getMsg);

            log.debug("message " + in + " was successfully sent to worker " + workerName);
        }
    }

    private void processShutdown(ChannelHandlerContext ctx, Message in) {
        log.debug("message " + in + " was successfully received");
        workersChannels.values().forEach(workerCh -> workerCh.writeAndFlush(in));
        workersChannels.clear();
        ctx.channel().parent().close();
    }

    private void processQuit(Message in) {
        log.debug("message " + in + " was successfully received");
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