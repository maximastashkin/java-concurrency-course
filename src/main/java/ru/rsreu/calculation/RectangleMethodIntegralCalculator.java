package ru.rsreu.calculation;

import java.util.function.Function;

public class RectangleMethodIntegralCalculator {
    private final double epsilon;

    public RectangleMethodIntegralCalculator(double epsilon) {
        this.epsilon = epsilon;
    }

    public double calculate(double lowerBound, double upperBound, Function<Double, Double> function) {
        if (upperBound < lowerBound) {
            throw new IllegalArgumentException("Upper bound must be more than lower");
        }
        if (lowerBound == upperBound) {
            return 0;
        }
        return calculateRectangleMethodeIntegral(lowerBound, upperBound, function,
                getSegmentsNumberForBounds(lowerBound, upperBound));
    }

    private int getSegmentsNumberForBounds(
            double lowerBound,
            double upperBound
    ) {
        int segmentNumber = 1;
        while ((upperBound - lowerBound) / segmentNumber >= epsilon) {
            segmentNumber++;
        }
        return segmentNumber;
    }

    private double calculateRectangleMethodeIntegral(
            double lowerBound,
            double upperBound,
            Function<Double, Double> function,
            int segmentsNumber
    ) {
        double square = 0;
        double h = (upperBound - lowerBound) / segmentsNumber;
        double left = lowerBound;

        for (int i = 0; i < segmentsNumber; i++) {
            square += h * function.apply(left);
            left += h;
        }
        return square;
    }

}
