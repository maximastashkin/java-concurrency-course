package ru.rsreu.logger;

import java.util.Observable;

public class CalculationProgress extends Observable {
    private final int hitsCount;
    private long currentIterationsCount;

    public CalculationProgress(int hitsCount) {
        this.hitsCount = hitsCount;
    }

    public synchronized void addIterations(long iterationsCount) {
        currentIterationsCount += iterationsCount;
        setChanged();
        notifyObservers(currentIterationsCount);
    }

    public int getHitsCount() {
        return hitsCount;
    }
}
