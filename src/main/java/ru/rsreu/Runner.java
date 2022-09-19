package ru.rsreu;

import ru.rsreu.calculation.RectangleMethodIntegralCalculator;

public class Runner {
    public static void main(String[] args) throws InterruptedException {
        RectangleMethodIntegralCalculator calculator = new RectangleMethodIntegralCalculator(1E-9);
        IntegrationCalculationTask task = new IntegrationCalculationTask(
                calculator, (x) -> Math.sqrt(1 - x * x), 0, 1);
        Thread calculationThread = new Thread(task);
        calculationThread.start();
        long timeBefore = System.currentTimeMillis();
        calculationThread.join();
        long timeAfter = System.currentTimeMillis();
        System.out.println("square: " + task.getCalculationResult());
        System.out.printf("calculation time: %d ms", timeAfter - timeBefore);
    }
}
