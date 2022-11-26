package ru.rsreu.exchange.simple;

import ru.rsreu.exchange.base.ExchangeClientMoneyTest;

public class SimpleExchangeImplClientMoneyTest extends ExchangeClientMoneyTest {
    public SimpleExchangeImplClientMoneyTest() {
        super(new SimpleExchangeImpl());
    }
}
