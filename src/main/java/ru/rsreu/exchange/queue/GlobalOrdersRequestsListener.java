package ru.rsreu.exchange.queue;

import ru.rsreu.exchange.OrderRegistrationStatus;
import ru.rsreu.exchange.currency.CurrencyPair;
import ru.rsreu.exchange.dto.OrderRequestDto;
import ru.rsreu.exchange.exception.InvalidCurrencyPairException;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static ru.rsreu.exchange.util.CurrencyUtils.getCurrencyPairByTwoCurrencies;

public class GlobalOrdersRequestsListener implements Runnable {
    private static final int LISTENABLE_QUEUE_TIMEOUT_MIN = 5;

    private final BlockingQueue<OrderRequestDto> orderRequests;

    private final Map<CurrencyPair, BlockingQueue<OrderRequestDto>> currencyPairsOrdersRequests;

    public GlobalOrdersRequestsListener(
            BlockingQueue<OrderRequestDto> orderRequests, Map<CurrencyPair,
            BlockingQueue<OrderRequestDto>> currencyPairsOrdersRequests
    ) {
        this.orderRequests = orderRequests;
        this.currencyPairsOrdersRequests = currencyPairsOrdersRequests;
    }

    @Override
    public void run() {
        try {
            listen();
        } catch (InterruptedException exception) {
            System.out.println("Exchange interrupted!");
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void listen() throws InterruptedException {
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            OrderRequestDto orderRequest = orderRequests.poll(LISTENABLE_QUEUE_TIMEOUT_MIN, TimeUnit.MINUTES);
            if (orderRequest == null) {
                continue;
            }
            try {
                currencyPairsOrdersRequests.get(
                        getCurrencyPairByTwoCurrencies(orderRequest.getOrder().getBuyingCurrency(),
                                orderRequest.getOrder().getSellingCurrency())
                ).add(orderRequest);
            } catch (InvalidCurrencyPairException exception) {
                orderRequest.setOrderRegistrationStatus(OrderRegistrationStatus.ERROR);
                orderRequest.getOperationCompletedLatch().countDown();
            }
        }
    }
}
