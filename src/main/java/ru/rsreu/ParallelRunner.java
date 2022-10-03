package ru.rsreu;

import java.util.concurrent.ExecutionException;

public class ParallelRunner {
    private static final ParallelExecutionTaskProcessor TASK_PROCESSOR =
            new ParallelExecutionTaskProcessor(16, 100);

    public static void main(String[] args) {
        long before = System.currentTimeMillis();
        try {
            double result = TASK_PROCESSOR.executeIntegrationTaskParallel(
                    a -> a * Math.sin(a), 0, 10, 1E-8);
            System.out.printf("Time: %d ms%n", System.currentTimeMillis() - before);
            System.out.println("Square: " + result);
        } catch (InterruptedException exception) {
            System.out.println("Calculation was interrupted");
        } catch (ExecutionException exception) {
            System.out.println(exception.getCause().toString());
        }
    }
}