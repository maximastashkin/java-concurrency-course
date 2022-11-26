package ru.rsreu.exchange.queue;

import ru.rsreu.exchange.base.ExchangeOrdersLogicalTest;

public class BlockingQueueExchangeImplOrdersLogicalTest extends ExchangeOrdersLogicalTest {
    protected BlockingQueueExchangeImplOrdersLogicalTest() {
        super(new BlockingQueueExchangeImpl());
    }
}
