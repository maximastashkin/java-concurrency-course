package ru.rsreu;

import ru.rsreu.calculation.RectangleMethodIntegralCalculator;
import ru.rsreu.logger.CalculationProgress;
import ru.rsreu.logger.ExecutionTaskProgressLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

public class ParallelExecutionTaskProcessor {
    private final ExecutorService executorService;
    private final int threadsCount;
    private final CalculationProgress calculationProgress;
    private final Semaphore semaphore;
    private final CountDownLatch latch;

    public ParallelExecutionTaskProcessor(int threadsCount, int logsCount) {
        this.threadsCount = threadsCount;
        executorService = Executors.newFixedThreadPool(threadsCount);
        calculationProgress = new CalculationProgress(logsCount);
        semaphore = new Semaphore(threadsCount / 2, true);
        latch = new CountDownLatch(threadsCount);
    }

    private static double calculateResult(List<Future<Double>> tasksFutures)
            throws InterruptedException, ExecutionException, TimeoutException {
        double result = 0;
        for (Future<Double> future : tasksFutures) {
            result += future.get(10, TimeUnit.MINUTES);
        }
        return result;
    }

    public double executeIntegrationTaskParallel(
            Function<Double, Double> function,
            double lowerBound,
            double upperBound,
            double epsilon
    ) throws InterruptedException, ExecutionException, TimeoutException {
        double segmentsDelta = (upperBound - lowerBound) / threadsCount;
        List<IntegrationCalculationTask> tasks =
                formParallelSegmentsTasks(function, epsilon, segmentsDelta, lowerBound, segmentsDelta);
        if (tasks.size() == 0) {
            return 0;
        }

        long totalIterationsNumber = tasks.get(0).getTotalIterationsNumber() * tasks.size();
        calculationProgress.addObserver(
                new ExecutionTaskProgressLogger(totalIterationsNumber, calculationProgress.getHitsCount()));
        List<Future<Double>> tasksFutures = sendTasksToExecution(tasks);

        executorService.shutdown();
        if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
            executorService.shutdownNow();
        }
        return calculateResult(tasksFutures);
    }

    private List<Future<Double>> sendTasksToExecution(List<IntegrationCalculationTask> tasks) {
        List<Future<Double>> tasksFutures = new ArrayList<>();

        for (Callable<Double> task : tasks) {
            tasksFutures.add(executorService.submit(task));
        }
        return tasksFutures;
    }

    private List<IntegrationCalculationTask> formParallelSegmentsTasks(
            Function<Double, Double> function,
            double epsilon, double segmentsDelta,
            double lowerBound,
            double segmentDelta
    ) {
        List<IntegrationCalculationTask> tasks = new ArrayList<>();
        double upperBound = lowerBound + segmentDelta;
        for (int i = 0; i < threadsCount; i++) {
            RectangleMethodIntegralCalculator calculator =
                    new RectangleMethodIntegralCalculator(epsilon, calculationProgress);
            tasks.add(new IntegrationCalculationTask(calculator, function, lowerBound, upperBound, semaphore, latch));
            lowerBound = upperBound;
            upperBound += segmentsDelta;
        }
        return tasks;
    }
}
