package ru.rsreu.exchange.simple;

import ru.rsreu.exchange.base.ExchangeThreadsStressTest;

public class SimpleExchangeImplThreadsStressTest extends ExchangeThreadsStressTest {
    public SimpleExchangeImplThreadsStressTest() {
        super(new SimpleExchangeImpl());
    }
}
