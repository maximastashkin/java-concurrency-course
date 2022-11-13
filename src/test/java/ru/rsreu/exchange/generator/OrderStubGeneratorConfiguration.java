package ru.rsreu.exchange.generator;

import ru.rsreu.exchange.currency.Currency;

public class OrderStubGeneratorConfiguration {
    final Currency[] availableCurrencies = Currency.values();
    final int availableMinValue = 1;
    final int availableMaxValue = 20;
    final int[] availableIntegerRates = {3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    final double[] availableDoubleRates = {0.5, 1.0 / 3, 0.25, 0.2, 1.0 / 6, 1.0 / 7, 1.0 / 8, 1.0 / 23, 1.0 / 26};
}
