package ru.rsreu.exchange.disruptor;

import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import ru.rsreu.exchange.AbstractExchange;
import ru.rsreu.exchange.OrderRegistrationStatus;
import ru.rsreu.exchange.currency.CurrencyPair;
import ru.rsreu.exchange.disruptor.model.CreatingOrderRequest;
import ru.rsreu.exchange.dto.OrderRequestDto;
import ru.rsreu.exchange.exception.NotEnoughMoneyException;
import ru.rsreu.exchange.order.Order;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static ru.rsreu.exchange.util.CurrencyUtils.getCurrenciesCartesianProduct;
import static ru.rsreu.exchange.util.CurrencyUtils.getCurrencyPairByTwoCurrencies;
import static ru.rsreu.exchange.util.OrderUtils.takeMoneyFromAccountByOrder;
import static ru.rsreu.exchange.util.OrderUtils.validateOrder;

public class DisruptorExchangeImpl extends AbstractExchange {
    private static final long ORDER_PROCESSING_TIMEOUT_MIN = 2;
    private static final int BUFFER_SIZE = 16384 * 4;

    private final Disruptor<CreatingOrderRequest> disruptor;
    private final Map<CurrencyPair, DisruptorListener> pairListeners = new HashMap<>();


    public DisruptorExchangeImpl() {
        disruptor = new Disruptor<>(CreatingOrderRequest.FACTORY, BUFFER_SIZE, DaemonThreadFactory.INSTANCE);
        for (CurrencyPair currencyPair : getCurrenciesCartesianProduct()) {
            DisruptorListener listener = new DisruptorListener(currencyPair);
            pairListeners.put(currencyPair, listener);
            disruptor.handleEventsWith(listener);
        }
        disruptor.start();
    }

    @Override
    public OrderRegistrationStatus registerNewOrder(Order order) throws NotEnoughMoneyException {
        validateOrder(order);
        takeMoneyFromAccountByOrder(order);
        OrderRequestDto request = new OrderRequestDto(order);
        disruptor.getRingBuffer().publishEvent((event, sequence) -> event.setOrderRequest(request));
        try {
            if (request.getOperationCompletedLatch().await(ORDER_PROCESSING_TIMEOUT_MIN, TimeUnit.MINUTES)) {
                return request.getOrderRegistrationStatus();
            } else {
                throw new InterruptedException();
            }
        } catch (InterruptedException exception) {
            System.out.println("Exchange interrupted");
            disruptor.shutdown();
        }
        return OrderRegistrationStatus.ERROR;
    }

    @Override
    public void declineOrder(Order order) {
        order.getClient().putMoney(order.getSellingCurrency(), order.getBuyingValue().multiply(order.getRate()));
        pairListeners
                .get(getCurrencyPairByTwoCurrencies(order.getBuyingCurrency(), order.getSellingCurrency()))
                .getOrders()
                .remove(order);
    }

    @Override
    public List<Order> getAllOpenedOrders() {
        return Collections.unmodifiableList(
                pairListeners
                        .values()
                        .stream()
                        .flatMap(it -> it.getOrders().stream())
                        .collect(Collectors.toList())
        );
    }
}
