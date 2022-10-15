package ru.rsreu.calculation;

import ru.rsreu.CalculationResultHolder;
import ru.rsreu.logger.CalculationProgress;

import java.util.function.Function;

public class RectangleMethodIntegralCalculator {
    private final CalculationResultHolder resultHolder;
    private final CalculationProgress progress;
    private final double epsilon;

    public RectangleMethodIntegralCalculator(CalculationResultHolder resultHolder, double epsilon) {
        this(resultHolder, new CalculationProgress(0), epsilon);
    }

    public RectangleMethodIntegralCalculator(
            CalculationResultHolder resultHolder, CalculationProgress progress, double epsilon) {
        this.resultHolder = resultHolder;
        this.epsilon = epsilon;
        this.progress = progress;
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
        long progressUpdatingFrequency = calculateProgressUpdatingFrequency(integrationSegmentsNumber);
        long iteration = 0;
        long nextProgressPartition = 0;
        while (iteration < integrationSegmentsNumber) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            double currentSquare = integrationDelta * function.apply(left);
            square += currentSquare;
            resultHolder.add(currentSquare);
            left += integrationDelta;
            iteration++;
            if (progressUpdatingFrequency != 0 && nextProgressPartition == progressUpdatingFrequency) {
                progress.addIterations(nextProgressPartition);
                nextProgressPartition = 0;
            }
            nextProgressPartition++;
        }
        progress.addIterations(nextProgressPartition);
        return square;
    }

    private long calculateProgressUpdatingFrequency(long integrationSegmentsNumber) {
        long progressUpdatingFrequency;
        if (progress.getHitsCount() != 0) {
            progressUpdatingFrequency = integrationSegmentsNumber / progress.getHitsCount();
        } else {
            progressUpdatingFrequency = 0;
        }
        return progressUpdatingFrequency;
    }

    public long getIntegrationSegmentNumber(double lowerBound, double upperBound) {
        return (long) ((upperBound - lowerBound) / epsilon);
    }

    private double getIntegrationDelta(double lowerBound, double upperBound, long integrationSegmentsNumber) {
        return (upperBound - lowerBound) / integrationSegmentsNumber;
    }
}