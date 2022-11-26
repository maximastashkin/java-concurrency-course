package ru.rsreu.exchange.queue;

import ru.rsreu.exchange.base.ExchangeThreadsStressTest;

public class BlockingQueueExchangeImplThreadsStressTest extends ExchangeThreadsStressTest {
    protected BlockingQueueExchangeImplThreadsStressTest() {
        super(new BlockingQueueExchangeImpl());
    }
}
