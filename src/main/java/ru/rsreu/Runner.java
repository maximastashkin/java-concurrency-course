package ru.rsreu;

import ru.rsreu.input.InputEntity;
import ru.rsreu.input.InputState;

import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;

public class Runner {
    private static final ExecutableThreadsProcessor threadsHolder = ExecutableThreadsProcessor.create();
    private static int currentTaskId = 0;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                InputEntity inputEntity = getInputEntity(scanner.nextLine());
                switch (inputEntity.getState()) {
                    case EXIT:
                        return;
                    case START: {
                        Optional<Double> epsilon = tryToConvertParam(inputEntity.getParam(), Double::parseDouble);
                        if (epsilon.isPresent()) {
                            threadsHolder.startNewTaskThread(currentTaskId, epsilon.get());
                            System.out.printf("Thread with task id = %d started%n", currentTaskId);
                            currentTaskId++;
                        } else {
                            System.out.println("Illegal epsilon parameter!");
                        }
                        break;
                    }
                    case AWAIT: {
                        int taskId = resolveTaskIdParameter(inputEntity.getParam());
                        try {
                            threadsHolder.getExecutableThreadOrThrow(taskId).join();
                        } catch (InterruptedException e) {
                            return;
                        }
                        break;
                    }
                    case STOP: {
                        int taskId = resolveTaskIdParameter(inputEntity.getParam());
                        threadsHolder.getExecutableThreadOrThrow(taskId).interrupt();
                        break;
                    }
                    default: {
                        System.out.println("Undefined input!");
                    }
                }
            } catch (RuntimeException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }

    private static int resolveTaskIdParameter(String param) {
        Optional<Integer> taskIdParameter = tryToConvertParam(param, Integer::parseInt);
        if (taskIdParameter.isPresent()) {
            return taskIdParameter.get();
        } else {
            throw new RuntimeException("Illegal task id parameter!");
        }
    }

    private static <T> Optional<T> tryToConvertParam(String param, Function<String, T> converter) {
        try {
            return Optional.of(converter.apply(param));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private static InputEntity getInputEntity(String input) {
        InputEntity.Builder builder = new InputEntity.Builder(input);
        if (input.equals("exit")) {
            return builder.state(InputState.EXIT).build();
        }
        if (input.startsWith("start")) {
            return builder.state(InputState.START).build();
        }
        if (input.startsWith("stop")) {
            return builder.state(InputState.STOP).build();
        }
        if (input.startsWith("await")) {
            return builder.state(InputState.AWAIT).build();
        }
        return builder.build();
    }
}
