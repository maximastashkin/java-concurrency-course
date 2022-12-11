package ru.rsreu.exchange.disruptor;

import ru.rsreu.exchange.base.ExchangeOrdersLogicalTest;

public class DisruptorExchangeImplOrdersLogicalTest extends ExchangeOrdersLogicalTest {
    protected DisruptorExchangeImplOrdersLogicalTest() {
        super(new DisruptorExchangeImpl());
    }
}
