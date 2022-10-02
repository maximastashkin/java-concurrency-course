package ru.rsreu;

import ru.rsreu.calculation.RectangleMethodIntegralCalculator;

import java.util.concurrent.Callable;
import java.util.function.Function;

public class IntegrationCalculationTask implements Callable<Double> {
    private final RectangleMethodIntegralCalculator calculator;
    private final Function<Double, Double> function;
    private final double lowerBound;
    private final double upperBound;

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
    public Double call() throws Exception {
        return calculator.calculate(function, lowerBound, upperBound);
    }

    @Override
    public String toString() {
        return "IntegrationCalculationTask{" +
                "lowerBound=" + lowerBound +
                ", upperBound=" + upperBound +
                '}';
    }

    public long getTotalIterationsNumber() {
        return calculator.getIntegrationSegmentNumber(lowerBound, upperBound);
    }
}