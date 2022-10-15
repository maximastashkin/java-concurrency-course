package ru.rsreu.counter_holder;

import ru.rsreu.Counter;

public class LazyInitCounterHolder implements CounterHolder {
    private final int startCounterValue;
    private volatile Counter counter;

    public LazyInitCounterHolder(int startCounterValue) {
        this.startCounterValue = startCounterValue;
    }

    @Override
    public synchronized int incrementAndGet() {
        getCounter().increment();
        return getCounter().getCurrentCount();
    }

    @Override
    public synchronized int get() {
        return getCounter().getCurrentCount();
    }

    private Counter getCounter() {
        Counter localCounter = counter;
        if (counter == null) {
            synchronized (this) {
                localCounter = counter;
                if (counter == null) {
                    counter = localCounter = new Counter(startCounterValue);
                }
            }
        }
        return localCounter;
    }
}
