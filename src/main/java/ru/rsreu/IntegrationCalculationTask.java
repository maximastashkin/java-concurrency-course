package ru.rsreu;

import ru.rsreu.calculation.RectangleMethodIntegralCalculator;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class IntegrationCalculationTask implements Callable<Double> {
    private final RectangleMethodIntegralCalculator calculator;
    private final Function<Double, Double> function;
    private final double lowerBound;
    private final double upperBound;
    private final Semaphore semaphore;
    private final CountDownLatch countDownLatch;

    public IntegrationCalculationTask(
            RectangleMethodIntegralCalculator calculator,
            Function<Double, Double> function,
            double lowerBound,
            double upperBound, Semaphore semaphore, CountDownLatch countDownLatch) {
        this.calculator = calculator;
        this.function = function;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.semaphore = semaphore;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public Double call() throws Exception {
        semaphore.acquire();
        double result = calculator.calculate(function, lowerBound, upperBound);
        semaphore.release();
        countDownLatch.countDown();
        long start = System.currentTimeMillis();
        logTimeAfterCompleting(start);
        return result;
    }

    private void logTimeAfterCompleting(long start) throws InterruptedException {
        if (countDownLatch.await(10, TimeUnit.MINUTES)) {
            System.out.printf(
                    "Time from end task [%s]: %d%n",
                    Thread.currentThread().getName(),
                    System.currentTimeMillis() - start
            );
        }
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