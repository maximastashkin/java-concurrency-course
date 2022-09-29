package ru.rsreu.logger;

public class ProgressLogger {
    private final long totalIterationCount;
    private final long logFrequency;

    public ProgressLogger(long totalIterationCount, int totalLogsCount) {
        this.totalIterationCount = totalIterationCount;

        if (totalLogsCount != 0) {
            logFrequency = totalIterationCount / totalLogsCount;
        } else {
            logFrequency = -1;
        }
    }

    public void logProgress(long currentIteration) {
        if (logFrequency !=- 1 && currentIteration % logFrequency == 0) {
            System.out.printf("Thread [%s] progress: %d%%%n",
                    Thread.currentThread().getName(), calculateProgressPercent(currentIteration));
        }
    }

    private int calculateProgressPercent(long currentIteration) {
        return (int) Math.round((((double) currentIteration / totalIterationCount) * 100));
    }
}
