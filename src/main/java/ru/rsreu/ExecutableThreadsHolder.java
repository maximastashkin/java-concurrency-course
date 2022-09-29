package ru.rsreu;

import ru.rsreu.calculation.RectangleMethodIntegralCalculator;

import java.util.HashMap;
import java.util.Map;

public class ExecutableThreadsHolder {
    private static final int LOGS_QUANTITY = 5;
    private final Map<Integer, Thread> executableThreads = new HashMap<>();

    private ExecutableThreadsHolder() {

    }

    public static ExecutableThreadsHolder create() {
        return new ExecutableThreadsHolder();
    }


    private static Thread formThreadWithTask(int taskId, double epsilon) {
        Thread thread = new Thread(
                new IntegrationCalculationTask(new RectangleMethodIntegralCalculator(epsilon, LOGS_QUANTITY),
                        (x) -> Math.sqrt(1 - x * x), 0, 1)
        );
        thread.setDaemon(true);
        thread.setName(String.format("thread(task_id=%s)", taskId));
        return thread;
    }

    public void startNewTaskThread(int taskId, double epsilon) {
        Thread thread = formThreadWithTask(taskId, epsilon);
        executableThreads.put(taskId, thread);
        thread.start();
    }

    public Thread getExecutableThreadOrThrow(int taskId) {
        Thread result = executableThreads.get(taskId);
        if (result != null) {
            return result;
        } else {
            throw new RuntimeException(String.format("Thread with given task(id = %d) didn't find", taskId));
        }
    }

}

