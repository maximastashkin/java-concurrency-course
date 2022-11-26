package ru.rsreu.exchange;

import ru.rsreu.exchange.currency.CurrencyPair;
import ru.rsreu.exchange.dto.ClientAccountOperationDto;
import ru.rsreu.exchange.order.Order;
import ru.rsreu.exchange.util.BigDecimalUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import static ru.rsreu.exchange.util.BigDecimalUtils.getInverseNumber;

public class OrderProcessor implements BiFunction<CurrencyPair, List<Order>, List<Order>> {

    private final Order order;
    private OrderRegistrationStatus status = OrderRegistrationStatus.REGISTERED;

    public OrderProcessor(Order order) {
        this.order = order;
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

    private static void processClientOrderMoneyTransfers(ClientAccountOperationDto sellingCurrencyRefundOperation,
                                                         ClientAccountOperationDto buyingCurrencyTransactionOperation) {
        sellingCurrencyRefundOperation.getClient().putMoney(sellingCurrencyRefundOperation.getCurrency(),
                sellingCurrencyRefundOperation.getValue().setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE));
        buyingCurrencyTransactionOperation.getClient().putMoney(buyingCurrencyTransactionOperation.getCurrency(),
                buyingCurrencyTransactionOperation.getValue().setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE));
    }

    public OrderRegistrationStatus getStatus() {
        return status;
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

            final BigDecimal orderExpectedCostInSellingValue = order.getBuyingValue().multiply(order.getRate());
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