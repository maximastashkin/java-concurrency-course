package ru.rsreu.parallel;

public class ParallelRunner {
    private static final ParallelExecutionTaskProcessor TASK_PROCESSOR = new ParallelExecutionTaskProcessor(5);

    public static void main(String[] args) throws InterruptedException {
        System.out.println(
                TASK_PROCESSOR.executeIntegrationTaskParallel(a -> Math.sqrt(1 - a * a), 0, 1, 1E-10));
    }
}
