package ru.rsreu.calculation;

import ru.rsreu.logger.ProgressLogger;

import java.util.function.Function;

public class RectangleMethodIntegralCalculator {
    private final double epsilon;
    private final int logsCount;

    public RectangleMethodIntegralCalculator(double epsilon) {
        this(epsilon, 0);
    }

    public RectangleMethodIntegralCalculator(double epsilon, int logsCount) {
        this.epsilon = epsilon;
        this.logsCount = logsCount;
    }

    public double calculate(Function<Double, Double> function, double lowerBound, double upperBound)
            throws InterruptedException {
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
    ) throws InterruptedException {
        long integrationSegmentsNumber = getIntegrationSegmentNumber(lowerBound, upperBound);
        double integrationDelta = getIntegrationDelta(lowerBound, upperBound, integrationSegmentsNumber);
        double square = 0;
        double left = lowerBound;
        long iteration = 0;
        ProgressLogger logger = new ProgressLogger(integrationSegmentsNumber, logsCount);
        while (iteration < integrationSegmentsNumber) {
            logger.logProgress(++iteration);
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            System.out.println(integrationDelta * function.apply(left));
            square += integrationDelta * function.apply(left);
            left += integrationDelta;
        }
        return square;
    }

    private long getIntegrationSegmentNumber(double lowerBound, double upperBound) {
        return (long) ((upperBound - lowerBound) / epsilon);
    }

    private double getIntegrationDelta(double lowerBound, double upperBound, long integrationSegmentsNumber) {
        return (upperBound - lowerBound) / integrationSegmentsNumber;
    }
}