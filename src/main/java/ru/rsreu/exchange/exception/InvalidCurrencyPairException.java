package ru.rsreu.exchange.exception;

import ru.rsreu.exchange.currency.Currency;

public class InvalidCurrencyPairException extends RuntimeException {
    public InvalidCurrencyPairException(Currency first, Currency second) {
        super(String.format("Pait:%s:%s doesn't exist", first, second));
    }
}
