package ru.rsreu.exchange.util;

import ru.rsreu.exchange.exception.InvalidOrderValuesException;
import ru.rsreu.exchange.exception.NotEnoughMoneyException;
import ru.rsreu.exchange.order.Order;

import java.math.BigDecimal;

public class OrderUtils {
    public static BigDecimal getExpectedOrderCost(Order order) {
        return order.getBuyingValue().multiply(order.getRate());
    }

    public static void validateOrder(Order order) {
        if (order.getRate()
                .setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE)
                .compareTo(BigDecimal.ZERO.setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE)) <= 0 ||
                order.getBuyingValue()
                        .setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE)
                        .compareTo(BigDecimal.ZERO.setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE)) <= 0) {
            throw new InvalidOrderValuesException("Order rate and buying value must be greater than zero");
        }
    }

    public static void takeMoneyFromAccountByOrder(Order order) throws NotEnoughMoneyException {
        if (!order.getClient().takeMoney(order.getSellingCurrency(), getExpectedOrderCost(order))) {
            throw new NotEnoughMoneyException("Not enough money for open this order");
        }
    }
}
