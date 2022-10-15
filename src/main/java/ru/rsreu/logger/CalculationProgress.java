package ru.rsreu.logger;

import java.util.Observable;
import java.util.concurrent.locks.ReentrantLock;

public class CalculationProgress extends Observable {
    private final int hitsCount;
    private final ReentrantLock reentrantLock = new ReentrantLock();

    private long currentIterationsCount;

    public CalculationProgress(int hitsCount) {
        this.hitsCount = hitsCount;
    }

    public void addIterations(long iterationsCount) {
        reentrantLock.lock();
        try {
            currentIterationsCount += iterationsCount;
            setChanged();
            notifyObservers(currentIterationsCount);
        } finally {
            reentrantLock.unlock();
        }
    }

    public int getHitsCount() {
        return hitsCount;
    }
}
