package ru.rsreu.exchange.generator;

import ru.rsreu.exchange.Client;
import ru.rsreu.exchange.currency.Currency;
import ru.rsreu.exchange.order.Order;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

public class DummyOrderStubGenerator {
    private final OrderStubGeneratorConfiguration configuration;

    public DummyOrderStubGenerator(OrderStubGeneratorConfiguration configuration) {
        this.configuration = configuration;
    }

    public Order generateRandomOrder(Client client) {
        Currency firstCurrency = configuration.availableCurrencies[getRandomIntInRange(0, configuration.availableCurrencies.length)];
        Currency secondCurrency;
        do {
            secondCurrency = configuration.availableCurrencies[getRandomIntInRange(0, configuration.availableCurrencies.length)];
        } while (secondCurrency == firstCurrency);
        BigDecimal buyingValue = BigDecimal.valueOf(getRandomIntInRange(configuration.availableMinValue, configuration.availableMaxValue + 1));
        BigDecimal rate;
        if (getRandomIntInRange(0, 2) == 0) {
            rate = BigDecimal.valueOf(configuration.availableIntegerRates[getRandomIntInRange(0, configuration.availableIntegerRates.length)]);
        } else {
            rate = BigDecimal.valueOf(configuration.availableDoubleRates[getRandomIntInRange(0, configuration.availableDoubleRates.length)]);
        }
        return new Order(client, firstCurrency, secondCurrency, buyingValue, rate);
    }

    private int getRandomIntInRange(int low, int high) {
        return ThreadLocalRandom.current().nextInt(low, high);
    }

}
