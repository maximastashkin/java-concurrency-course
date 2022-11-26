package ru.rsreu.exchange.simple;

import ru.rsreu.exchange.base.ExchangeOrdersLogicalTest;

public class SimpleExchangeImplOrdersLogicalTest extends ExchangeOrdersLogicalTest {
    protected SimpleExchangeImplOrdersLogicalTest() {
        super(new SimpleExchangeImpl());
    }
}
