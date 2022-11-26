package ru.rsreu.exchange.queue;

import ru.rsreu.exchange.base.ExchangeClientMoneyTest;

public class BlockingQueueExchangeImplClientMoneyTest extends ExchangeClientMoneyTest {
    protected BlockingQueueExchangeImplClientMoneyTest() {
        super(new BlockingQueueExchangeImpl());
    }
}
