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

    private static Order findBestSellingOrder(Order order, List<Order> pairOrders, BigDecimal inverseOrderRate) {
        Order bestSellingOrder = null;
        for (Order pairOrder : pairOrders) {
            if (pairOrder.getSellingCurrency() == order.getBuyingCurrency() && pairOrder.getRate().compareTo(inverseOrderRate) >= 0) {
                if (bestSellingOrder == null || bestSellingOrder.getRate().compareTo(pairOrder.getRate()) > 0) {
                    bestSellingOrder = pairOrder;
                }
            }
        }
        return bestSellingOrder;
    }

    @Override
    public Client registerNewClient() {
        return new Client();
    }

    @Override
    public OrderRegistrationStatus registerNewOrder(Order order) throws NotEnoughMoneyException {
        BigDecimal orderCost = order.getBuyingValue().multiply(order.getRate());
        if (!order.getClient().takeMoney(order.getSellingCurrency(), orderCost)) {
            throw new NotEnoughMoneyException("Not enough money for open this order");
        }
        orders.computeIfPresent(
                CurrencyUtils.getCurrencyPairByTwoCurrencies(order.getSellingCurrency(), order.getBuyingCurrency()),
                (key, pairOrders) -> {
                    final BigDecimal inverseOrderRate = BigDecimal.ONE.divide(order.getRate(), 10, RoundingMode.FLOOR);
                    final Order bestSellingOrder = findBestSellingOrder(order, pairOrders, inverseOrderRate);
                    if (bestSellingOrder == null) {
                        pairOrders.add(order);
                    } else {
                        final BigDecimal bestOrderSellingCost = bestSellingOrder.getBuyingValue().multiply(bestSellingOrder.getRate());
                        // Относительно order
                        final BigDecimal dealOrderSellingCurrencyCost = orderCost.min(bestOrderSellingCost.divide(bestSellingOrder.getRate(), 10, RoundingMode.FLOOR));
                        final BigDecimal dealOrderBuyingCurrencyCost = order.getBuyingValue().min(
                                bestSellingOrder.getBuyingValue().multiply(bestSellingOrder.getRate())
                        );
                        order.getClient().putMoney(order.getSellingCurrency(), orderCost.subtract(dealOrderSellingCurrencyCost));
                        order.getClient().putMoney(order.getBuyingCurrency(), dealOrderBuyingCurrencyCost);
                        bestSellingOrder.getClient().putMoney(bestSellingOrder.getSellingCurrency(), bestOrderSellingCost.subtract(dealOrderBuyingCurrencyCost));
                        bestSellingOrder.getClient().putMoney(bestSellingOrder.getBuyingCurrency(), dealOrderSellingCurrencyCost);
                        pairOrders.remove(bestSellingOrder);
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
        if (!clientAccountOperationDto.getClient().takeMoney(clientAccountOperationDto.getCurrency(), clientAccountOperationDto.getValue())) {
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
