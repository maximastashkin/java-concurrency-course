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
    private final CalculationResultHolder calculationResultHolder;
    private final CalculationProgress calculationProgress;

    public ParallelExecutionTaskProcessor(
            int threadsCount,
            CalculationResultHolder calculationResultHolder,
            int logsCount
    ) {
        this.threadsCount = threadsCount;
        executorService = Executors.newFixedThreadPool(threadsCount);
        this.calculationResultHolder = calculationResultHolder;
        calculationProgress = new CalculationProgress(logsCount);
    }

    private static void awaitAllTasks(List<Future<Double>> tasksFutures) throws InterruptedException, ExecutionException {
        for (Future<?> future : tasksFutures) {
            future.get();
        }
    }

    public double executeIntegrationTaskParallel(
            Function<Double, Double> function,
            double lowerBound,
            double upperBound,
            double epsilon
    ) throws InterruptedException, ExecutionException, TimeoutException {
        List<IntegrationCalculationTask> tasks =
                formParallelSegmentsTasks(function, epsilon, lowerBound, upperBound);
        if (tasks.size() == 0) {
            return 0;
        }

        subscribeLogObservers(tasks);
        List<Future<Double>> tasksFutures = sendTasksToExecution(tasks);

        executorService.shutdown();
        if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
            executorService.shutdownNow();
        }

        awaitAllTasks(tasksFutures);
        return calculationResultHolder.getResult();
    }

    private void subscribeLogObservers(List<IntegrationCalculationTask> tasks) {
        long totalIterationsNumber = tasks.get(0).getTotalIterationsNumber() * tasks.size();
        calculationProgress.addObserver(
                new ExecutionTaskProgressLogger(totalIterationsNumber, calculationProgress.getHitsCount()));
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
            double epsilon,
            double lowerBound,
            double upperBound
    ) {
        double segmentsDelta = (upperBound - lowerBound) / threadsCount;
        List<IntegrationCalculationTask> tasks = new ArrayList<>();
        double currentUpper = lowerBound + segmentsDelta;
        for (int i = 0; i < threadsCount; i++) {
            RectangleMethodIntegralCalculator calculator =
                    new RectangleMethodIntegralCalculator(calculationResultHolder, calculationProgress, epsilon);
            tasks.add(new IntegrationCalculationTask(calculator, function, lowerBound, currentUpper));
            lowerBound = currentUpper;
            currentUpper += segmentsDelta;
        }
        return tasks;
    }
}
