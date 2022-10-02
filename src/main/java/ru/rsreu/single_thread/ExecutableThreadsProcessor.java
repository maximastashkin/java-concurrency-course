package ru.rsreu.single_thread;

import ru.rsreu.calculation.RectangleMethodIntegralCalculator;

import java.util.HashMap;
import java.util.Map;

public class ExecutableThreadsProcessor {
    private static final int LOGS_QUANTITY = 5;
    private final Map<Integer, Thread> executableThreads = new HashMap<>();

    private ExecutableThreadsProcessor() {

    }

    public static ExecutableThreadsProcessor create() {
        return new ExecutableThreadsProcessor();
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
            if (!result.isAlive()) {
                throw new RuntimeException(
                        String.format("Thread [%s] is already dead%n", result.getName()));
            }
            return result;
        } else {
            throw new RuntimeException(String.format("Thread with given task(id = %d) didn't find", taskId));
        }
    }

}

