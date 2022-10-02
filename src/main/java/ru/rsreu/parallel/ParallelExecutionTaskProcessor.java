package ru.rsreu.parallel;

import ru.rsreu.calculation.RectangleMethodIntegralCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

public class ParallelExecutionTaskProcessor {
    private final ExecutorService executorService;
    private final int threadsCount;


    public ParallelExecutionTaskProcessor(int threadsCount) {
        this.threadsCount = threadsCount;
        executorService = Executors.newFixedThreadPool(threadsCount);
    }

    public double executeIntegrationTaskParallel(
            Function<Double, Double> function, double lowerBound, double upperBound, double epsilon) throws InterruptedException {
        RectangleMethodIntegralCalculator calculator = new RectangleMethodIntegralCalculator(epsilon, 10);

        double segmentsDelta = (upperBound - lowerBound) / threadsCount;
        double currentLowerBound = lowerBound;
        double currentUpperBound = currentLowerBound + segmentsDelta;

        List<IntegrationCalculationTask> tasks = new ArrayList<>();

        for (int i = 0; i < threadsCount; i++) {
            tasks.add(new IntegrationCalculationTask(calculator, function, currentLowerBound, currentUpperBound));
            currentLowerBound = currentUpperBound;
            currentUpperBound += segmentsDelta;
        }


        List<Future<Double>> futures = executorService.invokeAll(tasks);
        double result = 0;
        for (Future<Double> future : futures) {
            try {
                result += future.get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        executorService.shutdown();
        return result;
    }
}
