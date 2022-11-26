package ru.rsreu.exchange.queue;

import ru.rsreu.exchange.OrderProcessor;
import ru.rsreu.exchange.currency.CurrencyPair;
import ru.rsreu.exchange.dto.OrderRequestDto;
import ru.rsreu.exchange.order.Order;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class CurrencyPairOrdersRequestsListener implements Runnable {
    private static final int LISTENABLE_QUEUE_TIMEOUT_MIN = 5;
    private final CurrencyPair currencyPair;
    private final BlockingQueue<OrderRequestDto> currencyPairOrdersRequests;

    private final Map<CurrencyPair, List<Order>> orders;

    public CurrencyPairOrdersRequestsListener(
            CurrencyPair currencyPair,
            BlockingQueue<OrderRequestDto> currencyPairOrdersRequests,
            Map<CurrencyPair, List<Order>> orders
    ) {
        this.currencyPair = currencyPair;
        this.currencyPairOrdersRequests = currencyPairOrdersRequests;
        this.orders = orders;
    }

    @Override
    public void run() {
        try {
            listen();
        } catch (InterruptedException exception) {
            System.out.printf("%s pair listening interrupted\n", currencyPair);
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void listen() throws InterruptedException {
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            OrderRequestDto orderRequest = currencyPairOrdersRequests
                    .poll(LISTENABLE_QUEUE_TIMEOUT_MIN, TimeUnit.MINUTES);
            if (orderRequest == null) {
                continue;
            }
            Order order = orderRequest.getOrder();
            OrderProcessor orderProcessor = new OrderProcessor(order);
            orders.computeIfPresent(
                    currencyPair,
                    orderProcessor
            );
            orderRequest.getOperationCompletedLatch().countDown();
            orderRequest.setOrderRegistrationStatus(orderProcessor.getStatus());
        }
    }
}
