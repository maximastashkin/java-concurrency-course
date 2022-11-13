package ru.rsreu.exchange;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.rsreu.exchange.currency.Currency;
import ru.rsreu.exchange.exception.NotEnoughMoneyException;
import ru.rsreu.exchange.order.Order;
import ru.rsreu.exchange.util.BigDecimalUtils;

import java.math.BigDecimal;

class SimpleExchangeImplOrdersTest {
    @Test
    public void notEnoughMoneyForOpenOrderTest() {
        Exchange exchange = new SimpleExchangeImpl();
        Client client = exchange.registerNewClient();
        Assertions.assertThrows(NotEnoughMoneyException.class, () ->
                exchange.registerNewOrder(new Order(client, Currency.USD, Currency.CNY, BigDecimal.TEN, BigDecimal.TEN)));
    }

    @Test
    public void notMatchesOrdersOpenedCountTest() throws NotEnoughMoneyException {
        Exchange exchange = new SimpleExchangeImpl();
        Client firstClient = exchange.registerNewClient();
        firstClient.putMoney(Currency.RUR, BigDecimal.valueOf(610));

        Client secondClient = exchange.registerNewClient();
        secondClient.putMoney(Currency.USD, BigDecimal.valueOf(100.0 / 62));
        exchange.registerNewOrder(
                new Order(firstClient, Currency.RUR, Currency.USD, BigDecimal.TEN, BigDecimal.valueOf(61)));
        exchange.registerNewOrder(
                new Order(secondClient, Currency.USD, Currency.RUR, BigDecimal.valueOf(100), BigDecimal.valueOf(1.0 / 62)));
        Assertions.assertEquals(2, exchange.getAllOpenedOrders().size());
    }

    @Test
    public void notMatchesOrdersStatusTest() throws NotEnoughMoneyException {
        Exchange exchange = new SimpleExchangeImpl();
        Client firstClient = exchange.registerNewClient();
        firstClient.putMoney(Currency.RUR, BigDecimal.valueOf(610));

        Client secondClient = exchange.registerNewClient();
        secondClient.putMoney(Currency.USD, BigDecimal.valueOf(10));
        exchange.registerNewOrder(
                new Order(firstClient, Currency.RUR, Currency.USD, BigDecimal.TEN, BigDecimal.valueOf(61)));
        OrderRegistrationStatus secondOrder = exchange.registerNewOrder(
                new Order(secondClient, Currency.USD, Currency.RUR, BigDecimal.valueOf(100), BigDecimal.valueOf(1.0 / 62)));
        Assertions.assertEquals(OrderRegistrationStatus.REGISTERED, secondOrder);
    }

    @Test
    public void notFullLastOrderClosingTest() throws NotEnoughMoneyException {
        Exchange exchange = new SimpleExchangeImpl();
        Client firstClient = exchange.registerNewClient();
        firstClient.putMoney(Currency.RUR, BigDecimal.valueOf(610));

        Client secondClient = exchange.registerNewClient();
        secondClient.putMoney(Currency.USD, BigDecimal.valueOf(5));
        System.out.println("Before deal: ");
        System.out.println("First client account: " + firstClient.getAccount());
        System.out.println("Second client account: " + secondClient.getAccount());

        exchange.registerNewOrder(new Order(secondClient, Currency.USD, Currency.RUR, BigDecimal.valueOf(305), BigDecimal.valueOf(1.0 / 61)));
        exchange.registerNewOrder(new Order(firstClient, Currency.RUR, Currency.USD, BigDecimal.valueOf(10), BigDecimal.valueOf(61)));
        System.out.println("\nAfterDeal: ");
        System.out.println("First client account: " + firstClient.getAccount());
        System.out.println("Second client account: " + secondClient.getAccount());
        Assertions.assertEquals(BigDecimal.valueOf(5).setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                firstClient.getAccount().get(Currency.USD));
        Assertions.assertEquals(BigDecimal.valueOf(305).setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                firstClient.getAccount().get(Currency.RUR));

        Assertions.assertEquals(BigDecimal.ZERO.setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                secondClient.getAccount().get(Currency.USD));
        Assertions.assertEquals(BigDecimal.valueOf(305).setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                secondClient.getAccount().get(Currency.RUR));
    }
}