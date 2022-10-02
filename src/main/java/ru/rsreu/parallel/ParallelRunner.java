package ru.rsreu.parallel;

public class ParallelRunner {
    private static final ParallelExecutionTaskProcessor TASK_PROCESSOR = new ParallelExecutionTaskProcessor(10);

    public static void main(String[] args) throws InterruptedException {
        long before = System.currentTimeMillis();
        double result = TASK_PROCESSOR.executeIntegrationTaskParallel(a -> a * Math.sin(a), 0, 1, 3E-10);
        System.out.println("Time: " + (System.currentTimeMillis() - before) / 1000.0);
        System.out.println("Square: " + result);
    }
}
