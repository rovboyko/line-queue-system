package ru.home.linequeue.master.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Before;
import org.junit.Test;
import ru.home.linequeue.master.network.process.RandomWorkerChoosingStrategy;
import ru.home.linequeue.master.network.process.RequestBuffer;
import ru.home.linequeue.master.network.process.WorkerChoosingStrategy;
import ru.home.linequeue.messages.Message;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.Assert.assertEquals;
import static ru.home.linequeue.messages.Message.Type.GET;
import static ru.home.linequeue.messages.Message.Type.PUT;
import static ru.home.linequeue.messages.Message.Type.REGISTER;

public class MasterMessageHandlerTest {

    private final Map<String, ChannelHandlerContext> workersChannels = new ConcurrentHashMap<>();
    private final RequestBuffer requestBuffer = new RequestBuffer();
    private final ConcurrentNavigableMap<Long, String> linesToWorkers = new ConcurrentSkipListMap<>(Comparator.naturalOrder());
    private final WorkerChoosingStrategy workerChoosingStrategy = new RandomWorkerChoosingStrategy();

    EmbeddedChannel workerChannel;
    EmbeddedChannel clientChannel;

    @Before
    public void setUp() {
        MasterMessageHandler workerHandler = new MasterMessageHandler(new MasterServer(10042),
                workersChannels, requestBuffer, linesToWorkers, workerChoosingStrategy);
        MasterMessageHandler clientHandler = new MasterMessageHandler(new MasterServer(10042),
                workersChannels, requestBuffer, linesToWorkers, workerChoosingStrategy);
        workerChannel = new EmbeddedChannel(workerHandler);
        clientChannel = new EmbeddedChannel(clientHandler);
        workerChannel.writeInbound(new Message(REGISTER, "someworker", 0, 0));
    }

    @Test
    public void putTest() {
        Message msg = new Message(PUT, "abc", 0, 0);
        clientChannel.writeInbound(msg);
        assertEquals(msg, workerChannel.readOutbound());
    }

    @Test
    public void getNotEnoughTest() {
        Message msg = new Message(GET, "1", 0, 0);
        clientChannel.writeInbound(msg);
        // Returns NOT_ENOUGH message
        assertEquals(msg, clientChannel.readOutbound());
    }

    @Test
    public void getTest() {
        Message putMsg = new Message(PUT, "abc", 0, 0);
        Message getMsg = new Message(GET, "1", 0, 0);
        clientChannel.writeInbound(putMsg);
        assertEquals(putMsg, workerChannel.readOutbound());
        clientChannel.writeInbound(getMsg);
        getMsg.setReqId(2);
        assertEquals(getMsg, workerChannel.readOutbound());
    }
}