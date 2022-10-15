package ru.rsreu;

public class Counter {
    private int counter;

    public Counter(int startValue) {
        counter = startValue;
    }

    public void increment() {
        counter++;
    }

    public int getCurrentCount() {
        return counter;
    }
}