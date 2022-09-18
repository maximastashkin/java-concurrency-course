package ru.rsreu;

import ru.rsreu.calculation.RectangleMethodIntegralCalculator;

public class Runner {
    public static void main(String[] args) {
        RectangleMethodIntegralCalculator calculator = new RectangleMethodIntegralCalculator(1E-9);
        long timeBefore = System.currentTimeMillis();
        double square = calculator.calculate(x -> Math.sqrt(1 - x * x), 0, 1);
        long timeAfter = System.currentTimeMillis();
        System.out.println("square: " + square + " real square: " + Math.PI / 4);
        System.out.printf("calculation time: %d ms", timeAfter - timeBefore);
    }
}
