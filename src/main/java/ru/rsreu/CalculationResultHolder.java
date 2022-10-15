package ru.rsreu;

public class CalculationResultHolder {
    private double result;

    public synchronized void add(double partition) {
        result += partition;
    }

    public synchronized double getResult() {
        return result;
    }
}
