package ru.home.linequeue.master.network.process;

import org.junit.Test;
import ru.home.linequeue.master.network.process.strategy.RandomWorkerChoosingStrategy;
import ru.home.linequeue.master.network.process.strategy.WorkerChoosingStrategy;

import java.util.Arrays;

import static org.junit.Assert.*;

public class RandomWorkerChoosingStrategyTest {

    @Test
    public void outOfBoundsTest() {
        WorkerChoosingStrategy strategy = new RandomWorkerChoosingStrategy();
        strategy.addWorker("w1");
        assertEquals("w1", strategy.getWorker().orElse("err"));
        strategy.addWorker("w2");
        assertTrue(Arrays.asList("w1", "w2").contains(strategy.getWorker().orElse("err")));
        strategy.removeWorker("w1");
        assertEquals("w2", strategy.getWorker().orElse("err"));
    }
}