package ru.rsreu;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class ParallelRunner {
    private static final ParallelExecutionTaskProcessor TASK_PROCESSOR =
            new ParallelExecutionTaskProcessor(8, new CalculationResultHolder(), 10);

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
        } catch (TimeoutException exception) {
            System.out.println("Timeout exception");
        }
    }
}