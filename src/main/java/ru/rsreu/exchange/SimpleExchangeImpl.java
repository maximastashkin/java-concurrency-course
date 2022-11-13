package ru.rsreu.exchange;

import ru.rsreu.exchange.currency.CurrencyPair;
import ru.rsreu.exchange.currency.CurrencyUtils;
import ru.rsreu.exchange.dto.ClientAccountOperationDto;
import ru.rsreu.exchange.exception.InvalidOrderValuesException;
import ru.rsreu.exchange.exception.NotEnoughMoneyException;
import ru.rsreu.exchange.order.Order;
import ru.rsreu.exchange.util.BigDecimalUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static ru.rsreu.exchange.currency.CurrencyUtils.getCurrenciesCartesianProduct;
import static ru.rsreu.exchange.util.BigDecimalUtils.getInverseNumber;

public class SimpleExchangeImpl implements Exchange {
    private final Map<CurrencyPair, List<Order>> orders;

    {
        orders = new ConcurrentHashMap<>();
        for (CurrencyPair currencyPair : getCurrenciesCartesianProduct()) {
            orders.put(currencyPair, new LinkedList<>());
        }
    }

    private static Optional<Order> findBestSellingOrder(Order order, List<Order> pairOrders, BigDecimal inverseOrderRate) {
        Order bestSellingOrder = null;
        for (Order pairOrder : pairOrders) {
            if (pairOrder.getSellingCurrency() == order.getBuyingCurrency() && pairOrder.getRate().compareTo(inverseOrderRate) >= 0) {
                if (bestSellingOrder == null ||
                        bestSellingOrder.getRate().setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE)
                                .compareTo(
                                        pairOrder.getRate().setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE)) > 0) {
                    bestSellingOrder = pairOrder;
                }
            }
        }
        return Optional.ofNullable(bestSellingOrder);
    }

    private static void validateOrder(Order order) {
        if (order.getRate()
                .setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE)
                .compareTo(BigDecimal.ZERO.setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE)) <= 0 ||
                order.getBuyingValue()
                        .setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE)
                        .compareTo(BigDecimal.ZERO.setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE)) <= 0) {
            throw new InvalidOrderValuesException("Order rate and buying value must be greater than zero");
        }
    }

    @Override
    public Client registerNewClient() {
        return new Client();
    }

    @Override
    public OrderRegistrationStatus registerNewOrder(Order order) throws NotEnoughMoneyException {
        validateOrder(order);
        BigDecimal orderExpectedCostInSellingValue = order.getBuyingValue().multiply(order.getRate());
        if (!order.getClient().takeMoney(order.getSellingCurrency(), orderExpectedCostInSellingValue)) {
            throw new NotEnoughMoneyException("Not enough money for open this order");
        }
        OrderRegisterer registerer = new OrderRegisterer(order, orderExpectedCostInSellingValue);
        orders.computeIfPresent(
                CurrencyUtils.getCurrencyPairByTwoCurrencies(order.getSellingCurrency(), order.getBuyingCurrency()),
                registerer
        );
        return registerer.status;
    }

    @Override
    public void putMoney(ClientAccountOperationDto clientAccountOperationDto) {
        clientAccountOperationDto.getClient().putMoney(clientAccountOperationDto.getCurrency(), clientAccountOperationDto.getValue());
    }

    @Override
    public void takeMoney(ClientAccountOperationDto clientAccountOperationDto) throws NotEnoughMoneyException {
        if (!clientAccountOperationDto.getClient().takeMoney(clientAccountOperationDto.getCurrency(), clientAccountOperationDto.getValue())) {
            throw new NotEnoughMoneyException("Not enough money for operation");
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

    private static class OrderRegisterer implements BiFunction<CurrencyPair, List<Order>, List<Order>> {
        private final Order order;
        private final BigDecimal orderExpectedCostInSellingValue;
        private OrderRegistrationStatus status = OrderRegistrationStatus.REGISTERED;

        private OrderRegisterer(Order order, BigDecimal orderExpectedCostInSellingValue) {
            this.order = order;
            this.orderExpectedCostInSellingValue = orderExpectedCostInSellingValue;
        }

        private static void processClientOrderMoneyTransfers(ClientAccountOperationDto sellingCurrencyRefundOperation,
                                                             ClientAccountOperationDto buyingCurrencyTransactionOperation) {
            sellingCurrencyRefundOperation.getClient().putMoney(sellingCurrencyRefundOperation.getCurrency(),
                    sellingCurrencyRefundOperation.getValue().setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE));
            buyingCurrencyTransactionOperation.getClient().putMoney(buyingCurrencyTransactionOperation.getCurrency(),
                    buyingCurrencyTransactionOperation.getValue().setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE));
        }

        @Override
        public List<Order> apply(CurrencyPair currencyPair, List<Order> pairOrders) {
            final BigDecimal inverseOrderRate = getInverseNumber(order.getRate());
            final Optional<Order> bestSellingOrderContainer = findBestSellingOrder(order, pairOrders, inverseOrderRate);
            if (!bestSellingOrderContainer.isPresent()) {
                pairOrders.add(order);
            } else {
                Order bestSellingOrder = bestSellingOrderContainer.get();
                // Все ссылки на валюту относительно order
                // Считаем стоимость лучшего найденного ордера в валюте продажи
                final BigDecimal bestSellingOrderExpectedCostInSellingValue = bestSellingOrder.getBuyingValue().multiply(bestSellingOrder.getRate());
                // Считаем фактическую стоимость ордера в валюте покупки
                final BigDecimal factOrderCostInBuyingValue = order.getBuyingValue().min(bestSellingOrderExpectedCostInSellingValue);
                // Считаем фактическую стоимость ордера в валюте продажи
                final BigDecimal factOrderCostInSellingValue = factOrderCostInBuyingValue.multiply(getInverseNumber(bestSellingOrder.getRate()))
                        .setScale(2, RoundingMode.HALF_UP);

                // Раскидываем деньги на счета в результате закрытой сделки
                // Считаем возврат для клиента order в валюте продажи (разница ожидаемой стоимости ордера и фактической)
                final BigDecimal orderSellingValueRefund = orderExpectedCostInSellingValue.subtract(factOrderCostInSellingValue);
                ClientAccountOperationDto.Builder orderClientOperationsDtoBuilder = new ClientAccountOperationDto.Builder(order.getClient());
                processClientOrderMoneyTransfers(
                        orderClientOperationsDtoBuilder
                                .withCurrency(order.getSellingCurrency())
                                .withValue(orderSellingValueRefund)
                                .build(),
                        orderClientOperationsDtoBuilder
                                .withCurrency(order.getBuyingCurrency())
                                .withValue(factOrderCostInBuyingValue)
                                .build()
                );

                // Считаем возврат для клиента лучшего ордера в валюте продажи (разница ожидаемой стоимости лучшего ордера и фактической)
                final BigDecimal bestSellingOrderSellingValueRefund = bestSellingOrderExpectedCostInSellingValue.subtract(factOrderCostInBuyingValue);
                ClientAccountOperationDto.Builder bestSellingOrderClientOperationsDtoBuilder = new ClientAccountOperationDto.Builder(bestSellingOrder.getClient());
                processClientOrderMoneyTransfers(
                        bestSellingOrderClientOperationsDtoBuilder
                                .withCurrency(bestSellingOrder.getSellingCurrency())
                                .withValue(bestSellingOrderSellingValueRefund)
                                .build(),
                        bestSellingOrderClientOperationsDtoBuilder
                                .withCurrency(bestSellingOrder.getBuyingCurrency())
                                .withValue(factOrderCostInSellingValue)
                                .build()
                );
                pairOrders.remove(bestSellingOrder);
                status = OrderRegistrationStatus.COMPLETED;
            }
            return pairOrders;
        }
    }
}