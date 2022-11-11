package ru.rsreu.exchange.exception;

import ru.rsreu.exchange.currency.Currency;

public class SuchPairNotExists extends RuntimeException {
    public SuchPairNotExists(Currency first, Currency second) {
        super(String.format("Pait:%s:%s doesn't exist", first, second));
    }
}
