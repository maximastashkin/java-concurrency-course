package ru.rsreu.exchange.disruptor;

import ru.rsreu.exchange.base.ExchangeThreadsStressTest;

public class DisruptorExchangeImplThreadsStressTest extends ExchangeThreadsStressTest {
    protected DisruptorExchangeImplThreadsStressTest() {
        super(new DisruptorExchangeImpl());
    }
}
