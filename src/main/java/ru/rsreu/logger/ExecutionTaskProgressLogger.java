package ru.rsreu.logger;

import java.util.Observable;
import java.util.Observer;

public class ExecutionTaskProgressLogger implements Observer {
    private final long totalIterationsNumber;
    private final long logsFrequency;
    private int prevLogsThreshold = 0;

    public ExecutionTaskProgressLogger(long totalIterationsNumber, int logsNumber) {
        this.totalIterationsNumber = totalIterationsNumber;
        if (logsNumber == 0) {
            logsFrequency = 0;
        } else  {
            logsFrequency = totalIterationsNumber / logsNumber;
        }
    }

    public void logNewProgress(long currentIterationsNumber) {
        if (logsFrequency != 0 && currentIterationsNumber / logsFrequency > prevLogsThreshold) {
            prevLogsThreshold++;
            System.out.printf("Calculation progress: %d%%%n", calculateCurrentPercent());
        }
    }

    private long calculateCurrentPercent() {
        return Math.round(prevLogsThreshold * logsFrequency / (double) totalIterationsNumber * 100);
    }

    @Override
    public void update(Observable o, Object arg) {
        logNewProgress((long) arg);
    }
}
