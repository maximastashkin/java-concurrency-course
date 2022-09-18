package ru.rsreu;

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
        calculationResult = calculator.calculate(function, lowerBound, upperBound);
    }

    public double getCalculationResult() {
        return calculationResult;
    }
}
