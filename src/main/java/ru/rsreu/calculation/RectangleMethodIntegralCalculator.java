package ru.rsreu.calculation;

import java.util.function.Function;

public class RectangleMethodIntegralCalculator {
    private final double epsilon;

    public RectangleMethodIntegralCalculator(double epsilon) {
        this.epsilon = epsilon;
    }

    public double calculate(Function<Double, Double> function, double lowerBound, double upperBound) {
        if (upperBound < lowerBound) {
            throw new IllegalArgumentException("Upper bound must be more than lower");
        }
        if (lowerBound == upperBound) {
            return 0;
        }
        long integrationSegmentsNumber = getIntegrationSegmentNumber(lowerBound, upperBound);
        return calculateRectangleMethodeIntegral(function, lowerBound, upperBound,
                getIntegrationDelta(lowerBound, upperBound, integrationSegmentsNumber));
    }

    private long getIntegrationSegmentNumber(double lowerBound, double upperBound) {
        return (long) ((upperBound - lowerBound) / epsilon);
    }

    private double getIntegrationDelta(double lowerBound, double upperBound, long integrationSegmentsNumber) {
        return (upperBound - lowerBound) / integrationSegmentsNumber;
    }

    private double calculateRectangleMethodeIntegral(
            Function<Double, Double> function,
            double lowerBound,
            double upperBound,
            double integrationDelta
    ) {
        double square = 0;
        double left = lowerBound;

        while (left + integrationDelta / 3 <= upperBound) {
            square += integrationDelta * function.apply(left);
            left += integrationDelta;
        }
        return square;
    }
}