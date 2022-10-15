package ru.rsreu.counter_holder;

import ru.rsreu.Counter;

public class SimpleCounterHolder implements CounterHolder {
    private final Counter counter;

    public SimpleCounterHolder(int startCounterValue) {
        counter = new Counter(startCounterValue);
    }

    @Override
    public synchronized int incrementAndGet() {
        counter.increment();
        return counter.getCurrentCount();
    }

    @Override
    public synchronized int get() {
        return counter.getCurrentCount();
    }
}
