package ru.rsreu.exchange.simple;

import ru.rsreu.exchange.AbstractExchange;
import ru.rsreu.exchange.OrderProcessor;
import ru.rsreu.exchange.OrderRegistrationStatus;
import ru.rsreu.exchange.currency.CurrencyPair;
import ru.rsreu.exchange.util.CurrencyUtils;
import ru.rsreu.exchange.exception.NotEnoughMoneyException;
import ru.rsreu.exchange.order.Order;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static ru.rsreu.exchange.util.CurrencyUtils.getCurrenciesCartesianProduct;
import static ru.rsreu.exchange.util.CurrencyUtils.getCurrencyPairByTwoCurrencies;
import static ru.rsreu.exchange.util.OrderUtils.takeMoneyFromAccountByOrder;
import static ru.rsreu.exchange.util.OrderUtils.validateOrder;

public class SimpleExchangeImpl extends AbstractExchange {
    private final Map<CurrencyPair, List<Order>> orders = new ConcurrentHashMap<>();

    {
        for (CurrencyPair currencyPair : getCurrenciesCartesianProduct()) {
            orders.put(currencyPair, new LinkedList<>());
        }
    }

    @Override
    public OrderRegistrationStatus registerNewOrder(Order order) throws NotEnoughMoneyException {
        validateOrder(order);
        takeMoneyFromAccountByOrder(order);
        OrderProcessor orderProcessor = new OrderProcessor(order);
        orders.computeIfPresent(
                CurrencyUtils.getCurrencyPairByTwoCurrencies(order.getSellingCurrency(), order.getBuyingCurrency()),
                orderProcessor
        );
        return orderProcessor.getStatus();
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