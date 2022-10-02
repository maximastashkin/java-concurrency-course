package ru.rsreu.single_thread;

import ru.rsreu.calculation.RectangleMethodIntegralCalculator;

import java.util.function.Function;

public class IntegrationCalculationTask implements Runnable {
    private final RectangleMethodIntegralCalculator calculator;
    private final Function<Double, Double> function;
    private final double lowerBound;
    private final double upperBound;
    private double calculationResult;


    public IntegrationCalculationTask(
            RectangleMethodIntegralCalculator calculator,
            Function<Double, Double> function,
            double lowerBound,
            double upperBound) {
        this.calculator = calculator;
        this.function = function;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public void run() {
        try {
            calculationResult = calculator.calculate(function, lowerBound, upperBound);
        } catch (InterruptedException exception) {
            System.out.printf("Thread [%s] was interrupted%n", Thread.currentThread().getName());
        }

    }

    public double getCalculationResult() {
        return calculationResult;
    }
}
