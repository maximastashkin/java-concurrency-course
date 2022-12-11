package ru.rsreu.exchange.disruptor;

import ru.rsreu.exchange.base.ExchangeClientMoneyTest;

public class DisruptorExchangeImplClientMoneyTest extends ExchangeClientMoneyTest {
    protected DisruptorExchangeImplClientMoneyTest() {
        super(new DisruptorExchangeImpl());
    }
}
