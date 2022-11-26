package ru.rsreu.exchange.queue;

import ru.rsreu.exchange.AbstractExchange;
import ru.rsreu.exchange.OrderRegistrationStatus;
import ru.rsreu.exchange.currency.CurrencyPair;
import ru.rsreu.exchange.dto.OrderRequestDto;
import ru.rsreu.exchange.exception.NotEnoughMoneyException;
import ru.rsreu.exchange.order.Order;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static ru.rsreu.exchange.util.CurrencyUtils.getCurrenciesCartesianProduct;
import static ru.rsreu.exchange.util.CurrencyUtils.getCurrencyPairByTwoCurrencies;
import static ru.rsreu.exchange.util.OrderUtils.takeMoneyFromAccountByOrder;
import static ru.rsreu.exchange.util.OrderUtils.validateOrder;

public class BlockingQueueExchangeImpl extends AbstractExchange {
    private static final long ORDER_PROCESSING_TIMEOUT_MIN = 2;

    private final BlockingQueue<OrderRequestDto> globalOrdersRequests = new LinkedBlockingQueue<>();

    private final Map<CurrencyPair, BlockingQueue<OrderRequestDto>> currencyPairsOrdersRequests = new HashMap<>();
    private final Map<CurrencyPair, List<Order>> orders = new HashMap<>();

    @SuppressWarnings("FieldCanBeLocal")
    private final Thread globalOrdersRequestsListenerThread;

    @SuppressWarnings("FieldCanBeLocal")
    private final Map<CurrencyPair, Thread> currencyPairsOrdersRequestsListeners = new HashMap<>();

    {
        for (CurrencyPair currencyPair : getCurrenciesCartesianProduct()) {
            currencyPairsOrdersRequests.put(currencyPair, new LinkedBlockingQueue<>());
            orders.put(currencyPair, new LinkedList<>());
        }
    }

    public BlockingQueueExchangeImpl() {
        GlobalOrdersRequestsListener globalOrdersRequestsListener = new GlobalOrdersRequestsListener(
                globalOrdersRequests, currencyPairsOrdersRequests
        );
        this.globalOrdersRequestsListenerThread = new Thread(globalOrdersRequestsListener);
        this.globalOrdersRequestsListenerThread.setDaemon(true);
        for (Map.Entry<CurrencyPair, BlockingQueue<OrderRequestDto>> entry : currencyPairsOrdersRequests.entrySet()) {
            CurrencyPair pair = entry.getKey();
            Thread listenerThread = new Thread(new CurrencyPairOrdersRequestsListener(pair, entry.getValue(), orders));
            listenerThread.setDaemon(true);
            currencyPairsOrdersRequestsListeners.put(pair, listenerThread);
        }
        currencyPairsOrdersRequestsListeners.values().forEach(Thread::start);
        this.globalOrdersRequestsListenerThread.start();
    }

    @Override
    public OrderRegistrationStatus registerNewOrder(Order order) throws NotEnoughMoneyException {
        validateOrder(order);
        takeMoneyFromAccountByOrder(order);
        OrderRequestDto orderRequest = new OrderRequestDto(order);
        globalOrdersRequests.add(orderRequest);
        try {
            if (orderRequest.getOperationCompletedLatch().await(ORDER_PROCESSING_TIMEOUT_MIN, TimeUnit.MINUTES)) {
                return orderRequest.getOrderRegistrationStatus();
            } else {
                throw new InterruptedException();
            }
        } catch (InterruptedException e) {
            System.out.println("Exchange interrupted");
        }
        return OrderRegistrationStatus.ERROR;
    }

    @Override
    public void declineOrder(Order order) {
        order.getClient().putMoney(order.getSellingCurrency(), order.getBuyingValue().multiply(order.getRate()));
        orders.get(getCurrencyPairByTwoCurrencies(order.getBuyingCurrency(), order.getSellingCurrency())).remove(order);
    }

    @Override
    public List<Order> getAllOpenedOrders() {
        return Collections.unmodifiableList(
                orders.values()
                        .stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList())
        );
    }
}
