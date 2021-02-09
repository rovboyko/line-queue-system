package ru.home.linequeue.master.network.process;

import java.util.Optional;
import java.util.Random;

public class RandomWorkerChoosingStrategy extends WorkerChoosingStrategy{

    final Random r = new Random();
    int bound = 0;

    @Override
    protected Optional<String> getWorkerInternal() {
        if (bound > 0) {
            return Optional.of(workers.get(r.nextInt(bound)));
        } else {
            return Optional.empty();
        }
    }

    @Override
    protected void refreshStrategy() {
        bound = workers.size();
    }
}
