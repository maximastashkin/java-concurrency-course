package ru.rsreu.calculation;

import java.util.function.Function;

public class RectangleMethodIntegralCalculator {
    private static final int LOGS_COUNT = 15;

    private final double epsilon;

    public RectangleMethodIntegralCalculator(double epsilon) {
        this.epsilon = epsilon;
    }

    private static int calculateProgressPercent(long integrationSegmentsNumber, long iteration) {
        return (int) (((double) iteration / integrationSegmentsNumber) * 100);
    }

    public double calculate(Function<Double, Double> function, double lowerBound, double upperBound) {
        if (upperBound < lowerBound) {
            throw new IllegalArgumentException("Upper bound must be more than lower");
        }
        if (lowerBound == upperBound) {
            return 0;
        }
        return calculateRectangleMethodeIntegral(function, lowerBound, upperBound);
    }

    private double calculateRectangleMethodeIntegral(
            Function<Double, Double> function,
            double lowerBound,
            double upperBound
    ) {
        long integrationSegmentsNumber = getIntegrationSegmentNumber(lowerBound, upperBound);
        double integrationDelta = getIntegrationDelta(lowerBound, upperBound, integrationSegmentsNumber);
        long logsFrequency = getLogFrequency(integrationSegmentsNumber);

        double square = 0;
        double left = lowerBound;
        long iteration = 0;
        while (left + integrationDelta / 3 <= upperBound) {
            square += integrationDelta * function.apply(left);
            left += integrationDelta;
            if (iteration % logsFrequency == 0) {
                System.out.printf("Current calculation progress: %d%%\n",
                        calculateProgressPercent(integrationSegmentsNumber, iteration));
            }
            iteration++;
        }
        return square;
    }

    private long getIntegrationSegmentNumber(double lowerBound, double upperBound) {
        return (long) ((upperBound - lowerBound) / epsilon);
    }

    private double getIntegrationDelta(double lowerBound, double upperBound, long integrationSegmentsNumber) {
        return (upperBound - lowerBound) / integrationSegmentsNumber;
    }

    private long getLogFrequency(long integrationSegmentsNumber) {
        return integrationSegmentsNumber / LOGS_COUNT;
    }
}