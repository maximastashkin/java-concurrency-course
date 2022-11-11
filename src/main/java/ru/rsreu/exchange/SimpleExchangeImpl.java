package ru.rsreu.exchange;

import ru.rsreu.exchange.currency.CurrencyPair;
import ru.rsreu.exchange.currency.CurrencyUtils;
import ru.rsreu.exchange.dto.ClientAccountOperationDto;
import ru.rsreu.exchange.exception.NotEnoughMoneyException;
import ru.rsreu.exchange.order.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static ru.rsreu.exchange.currency.CurrencyUtils.getCurrenciesCartesianProduct;

public class SimpleExchangeImpl implements Exchange {
    private final Map<CurrencyPair, List<Order>> orders;

    {
        orders = new ConcurrentHashMap<>();
        for (CurrencyPair currencyPair : getCurrenciesCartesianProduct()) {
            orders.put(currencyPair, new LinkedList<>());
        }
    }

    @Override
    public Client registerNewClient() {
        return new Client();
    }

    @Override
    public OrderRegistrationStatus registerNewOrder(Order order) throws NotEnoughMoneyException {
        if (order.getClient().takeMoney(order.getSellingCurrency(), order.getBuyingValue().multiply(order.getRate()))) {
            throw new NotEnoughMoneyException("Not enough money for open this order");
        }
        orders.computeIfPresent(
                CurrencyUtils.getCurrencyPairByTwoCurrencies(order.getSellingCurrency(), order.getBuyingCurrency()),
                (key, pairOrders) -> {
                    final BigDecimal inverseOrderRate = BigDecimal.ONE.divide(order.getRate(), 10, RoundingMode.HALF_UP);
                    Order bestSellingOrder = null;
                    for (Order pairOrder : pairOrders) {
                        if (pairOrder.getSellingCurrency() == order.getBuyingCurrency() && pairOrder.getRate().compareTo(inverseOrderRate) >= 0) {
                            if (bestSellingOrder == null || bestSellingOrder.getRate().compareTo(pairOrder.getRate()) > 0) {
                                bestSellingOrder = pairOrder;
                            }
                        }
                    }
                    if (bestSellingOrder == null) {
                        pairOrders.add(order);
                    } else {

                    }
                    return pairOrders;
                });
        return OrderRegistrationStatus.REGISTERED;
    }

    @Override
    public void putMoney(ClientAccountOperationDto clientAccountOperationDto) {
        clientAccountOperationDto.getClient().putMoney(clientAccountOperationDto.getCurrency(), clientAccountOperationDto.getValue());
    }

    @Override
    public void takeMoney(ClientAccountOperationDto clientAccountOperationDto) throws NotEnoughMoneyException {
        if (clientAccountOperationDto.getClient().takeMoney(clientAccountOperationDto.getCurrency(), clientAccountOperationDto.getValue())) {
            throw new NotEnoughMoneyException("Not enough money for taking");
        }
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
