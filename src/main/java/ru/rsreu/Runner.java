package ru.rsreu;

import ru.rsreu.calculation.RectangleMethodIntegralCalculator;

import static java.lang.Math.sqrt;

public class Runner {
    public static void main(String[] args) {
        RectangleMethodIntegralCalculator calculator = new RectangleMethodIntegralCalculator(1E-9);
        long timeBefore = System.currentTimeMillis();
        double square = calculator.calculate(0, 1, x -> sqrt(1 - x * x));
        long timeAfter = System.currentTimeMillis();
        System.out.println("square: " + square);
        System.out.printf("calculation time: %d ms", timeAfter - timeBefore);
    }
}
