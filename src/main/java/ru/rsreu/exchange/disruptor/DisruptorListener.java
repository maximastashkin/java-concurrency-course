package ru.rsreu.exchange.disruptor;

import com.lmax.disruptor.EventHandler;
import ru.rsreu.exchange.OrderProcessor;
import ru.rsreu.exchange.currency.CurrencyPair;
import ru.rsreu.exchange.disruptor.model.CreatingOrderRequest;
import ru.rsreu.exchange.dto.OrderRequestDto;
import ru.rsreu.exchange.order.Order;
import ru.rsreu.exchange.util.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;

import static ru.rsreu.exchange.util.CurrencyUtils.getCurrencyPairByTwoCurrencies;

public class DisruptorListener implements EventHandler<CreatingOrderRequest> {
    private final CurrencyPair currencyPair;

    private final List<Order> orders;

    public DisruptorListener(CurrencyPair currencyPair) {
        this.currencyPair = currencyPair;
        this.orders = new ArrayList<>();
    }

    public List<Order> getOrders() {
        return orders;
    }

    @Override
    public void onEvent(CreatingOrderRequest creatingOrderRequest, long l, boolean b) throws Exception {
        OrderRequestDto request = creatingOrderRequest.getOrderRequest();
        Order order = request.getOrder();
        if (getCurrencyPairByTwoCurrencies(order.getBuyingCurrency(), order.getSellingCurrency()) != currencyPair) {
            return;
        }
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        OrderProcessor orderProcessor = new OrderProcessor(order);
        orderProcessor.apply(currencyPair, orders);
        request.getOperationCompletedLatch().countDown();
        request.setOrderRegistrationStatus(orderProcessor.getStatus());
    }
}
