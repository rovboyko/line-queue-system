package ru.home.linequeue.master.network.process.strategy;

import java.util.*;

public abstract class WorkerChoosingStrategy {

    protected final List<String> workers = new ArrayList<>();

    protected abstract Optional<String> getWorkerInternal();

    protected abstract void refreshStrategy();

    public void addWorker(String worker) {
        workers.add(worker);
        refreshStrategy();
    }

    public void removeWorker(String worker) {
        workers.remove(worker);
        refreshStrategy();
    }

    public Optional<String> getWorker() {
        return getWorkerInternal();
    }
}
