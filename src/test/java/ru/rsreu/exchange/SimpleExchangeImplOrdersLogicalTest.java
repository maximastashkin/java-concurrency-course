package ru.rsreu.exchange;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.rsreu.exchange.currency.Currency;
import ru.rsreu.exchange.exception.InvalidCurrencyPairException;
import ru.rsreu.exchange.exception.InvalidOrderValuesException;
import ru.rsreu.exchange.exception.NotEnoughMoneyException;
import ru.rsreu.exchange.order.Order;
import ru.rsreu.exchange.util.BigDecimalUtils;

import java.math.BigDecimal;

class SimpleExchangeImplOrdersLogicalTest {
    private final Exchange exchange = new SimpleExchangeImpl();
    // Валюты для тестов. Валюты задаются относительно ордера firstClient
    // Имеются в виду валюты покупки-продажи
    Currency sellingCurrency = Currency.RUR;
    Currency buyuingCurrency = Currency.USD;

    @Test
    public void invalidOrderTest() {
        Client client = exchange.registerNewClient();
        Assertions.assertThrows(InvalidOrderValuesException.class, () ->
                exchange.registerNewOrder(new Order(client, Currency.RUR, Currency.USD, BigDecimal.ZERO, BigDecimal.ZERO)));
    }

    @Test
    public void invalidCurrencyPairOrderTest() {
        Client client = exchange.registerNewClient();
        client.putMoney(Currency.RUR, BigDecimal.valueOf(100));
        Assertions.assertThrows(InvalidCurrencyPairException.class, () ->
                exchange.registerNewOrder(new Order(client, Currency.RUR, Currency.RUR, BigDecimal.TEN, BigDecimal.TEN)));
    }

    @Test
    public void notEnoughMoneyForOpenOrderTest() {
        Client client = exchange.registerNewClient();
        Assertions.assertThrows(NotEnoughMoneyException.class, () ->
                exchange.registerNewOrder(new Order(client, Currency.USD, Currency.CNY, BigDecimal.TEN, BigDecimal.TEN)));
    }

    // Неподходящие по курсу ордеры
    // Проверка на количество активных ордеров
    @Test
    public void notMatchesOrdersOpenedCountTest() throws NotEnoughMoneyException {
        Client firstClient = exchange.registerNewClient();
        firstClient.putMoney(sellingCurrency, BigDecimal.valueOf(610));

        Client secondClient = exchange.registerNewClient();
        secondClient.putMoney(buyuingCurrency, BigDecimal.valueOf(100.0 / 62));
        exchange.registerNewOrder(
                new Order(firstClient, sellingCurrency, buyuingCurrency, BigDecimal.TEN, BigDecimal.valueOf(61)));
        exchange.registerNewOrder(
                new Order(secondClient, buyuingCurrency, sellingCurrency, BigDecimal.valueOf(100), BigDecimal.valueOf(1.0 / 62)));
        Assertions.assertEquals(2, exchange.getAllOpenedOrders().size());
    }

    // Неподходящие по курсу ордеры
    // Проверка на статус последнего ордера (не COMPLETED, а REGISTRED)
    @Test
    public void notMatchesOrdersStatusTest() throws NotEnoughMoneyException {
        Client firstClient = exchange.registerNewClient();
        firstClient.putMoney(sellingCurrency, BigDecimal.valueOf(610));

        Client secondClient = exchange.registerNewClient();
        secondClient.putMoney(buyuingCurrency, BigDecimal.valueOf(10));
        exchange.registerNewOrder(
                new Order(firstClient, sellingCurrency, buyuingCurrency, BigDecimal.TEN, BigDecimal.valueOf(61)));
        OrderRegistrationStatus secondOrder = exchange.registerNewOrder(
                new Order(secondClient, buyuingCurrency, sellingCurrency, BigDecimal.valueOf(100), BigDecimal.valueOf(1.0 / 62)));
        Assertions.assertEquals(OrderRegistrationStatus.REGISTERED, secondOrder);
    }

    // Полностью закрывающиеся ордеры
    @Test
    public void ordersFullClosingTest() throws NotEnoughMoneyException {
        Client firstClient = exchange.registerNewClient();
        firstClient.putMoney(sellingCurrency, BigDecimal.valueOf(610));

        Client secondClient = exchange.registerNewClient();
        secondClient.putMoney(buyuingCurrency, BigDecimal.valueOf(10));
        System.out.println("\nBefore deal: ");
        System.out.println("First client account: " + firstClient.getAccount());
        System.out.println("Second client account: " + secondClient.getAccount());

        exchange.registerNewOrder(new Order(firstClient, sellingCurrency,buyuingCurrency, BigDecimal.valueOf(10), BigDecimal.valueOf(61)));
        exchange.registerNewOrder(new Order(secondClient, buyuingCurrency, sellingCurrency, BigDecimal.valueOf(610), BigDecimal.valueOf(1.0 / 61)));

        System.out.println("\nAfter deal: ");
        System.out.println("First client account: " + firstClient.getAccount());
        System.out.println("Second client account: " + secondClient.getAccount());
        Assertions.assertEquals(BigDecimal.ZERO.setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                firstClient.getAccount().get(sellingCurrency));
        Assertions.assertEquals(BigDecimal.valueOf(10).setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                firstClient.getAccount().get(buyuingCurrency));

        Assertions.assertEquals(BigDecimal.ZERO.setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                secondClient.getAccount().get(buyuingCurrency));
        Assertions.assertEquals(BigDecimal.valueOf(610).setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                secondClient.getAccount().get(sellingCurrency));
    }

    // Неполностью закрывающиеся ордеры с одинаковым курсом
    // Висел ордер на покупку 305 рублей по 61 рубль за доллар (списана сумма 5 долларов)
    // Пришел ордер на покупку 10 долларов по 61 рубль за доллар (списана сумма 610 рублей)
    // Сделка совершается по курсу 61 рубль за доллар
    // По первому ордеру:
    // Начисляется 305 рублей в счет покупки рублей (продажи 5 долларов) по 61 рубль за доллар (сумма сделки 5 долларов, поэтому ничего не вернулось)
    // По второму ордеру:
    // Начисляется 305 рублей (в счет списанных 610),
    // Начисляется 5 долларов в счет покупки долларов (продажи 305 рублей) по 61 рубль за доллар (сумма сделки 305 рублей, поэтому 305 (вторая половина) вернулась)
    // Таким образом, полностью закрылся первый ордер
    @Test
    public void ordersNotFullClosingTest() throws NotEnoughMoneyException {
        Client firstClient = exchange.registerNewClient();
        firstClient.putMoney(sellingCurrency, BigDecimal.valueOf(610));

        Client secondClient = exchange.registerNewClient();
        secondClient.putMoney(buyuingCurrency, BigDecimal.valueOf(5));
        System.out.println("\nBefore deal: ");
        System.out.println("First client account: " + firstClient.getAccount());
        System.out.println("Second client account: " + secondClient.getAccount());

        exchange.registerNewOrder(new Order(secondClient, buyuingCurrency, sellingCurrency, BigDecimal.valueOf(305), BigDecimal.valueOf(1.0 / 61)));
        exchange.registerNewOrder(new Order(firstClient, sellingCurrency, buyuingCurrency, BigDecimal.valueOf(10), BigDecimal.valueOf(61)));

        System.out.println("\nAfter deal: ");
        System.out.println("First client account: " + firstClient.getAccount());
        System.out.println("Second client account: " + secondClient.getAccount());
        Assertions.assertEquals(BigDecimal.valueOf(305).setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                firstClient.getAccount().get(sellingCurrency));
        Assertions.assertEquals(BigDecimal.valueOf(5).setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                firstClient.getAccount().get(buyuingCurrency));

        Assertions.assertEquals(BigDecimal.ZERO.setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                secondClient.getAccount().get(buyuingCurrency));
        Assertions.assertEquals(BigDecimal.valueOf(305).setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                secondClient.getAccount().get(sellingCurrency));
    }

    // Тоже самое, что и предыдущий тест, только ордеры приходят в противоположном порядке => тот же результат
    @Test
    public void ordersNotFullClosingTest1() throws NotEnoughMoneyException {
        Client firstClient = exchange.registerNewClient();
        firstClient.putMoney(sellingCurrency, BigDecimal.valueOf(610));

        Client secondClient = exchange.registerNewClient();
        secondClient.putMoney(buyuingCurrency, BigDecimal.valueOf(5));
        System.out.println("\nBefore deal: ");
        System.out.println("First client account: " + firstClient.getAccount());
        System.out.println("Second client account: " + secondClient.getAccount());

        exchange.registerNewOrder(new Order(firstClient, sellingCurrency, buyuingCurrency, BigDecimal.valueOf(5), BigDecimal.valueOf(61)));
        exchange.registerNewOrder(new Order(secondClient, buyuingCurrency, sellingCurrency, BigDecimal.valueOf(305), BigDecimal.valueOf(1.0 / 61)));

        System.out.println("\nAfter deal: ");
        System.out.println("First client account: " + firstClient.getAccount());
        System.out.println("Second client account: " + secondClient.getAccount());
        Assertions.assertEquals(BigDecimal.valueOf(305).setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                firstClient.getAccount().get(sellingCurrency));
        Assertions.assertEquals(BigDecimal.valueOf(5).setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                firstClient.getAccount().get(buyuingCurrency));

        Assertions.assertEquals(BigDecimal.ZERO.setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                secondClient.getAccount().get(buyuingCurrency));
        Assertions.assertEquals(BigDecimal.valueOf(305).setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                secondClient.getAccount().get(sellingCurrency));
    }

    // Висел ордер на покупку 610 рублей по 61 рубль за доллар (списана сумма 10 долларов)
    // Пришел ордер на покупку 5 долларов по 61 рубль за доллар (списана сумма 305 рублей)
    // Сделка совершается по курсу 61 рубль за доллар
    // По первому ордеру:
    // Начисляется 5 долларов (в счет списанных 10),
    // Начисляется 305 рублей в счет покупки рублей (продажи 5 долларов) по 61 рубль за доллар (сумма сделки 5 долларов, поэтому 5 (вторая половина( вернулась)
    // По второму ордеру
    // Начисляется 5 долларов в счет покупки долларов (продажи 305 рублей) по 61 рубль за доллар (сумма сделки 305 рублей, поэтому нчиего не вернулось)
    @Test
    public void ordersNotFullClosingTest2() throws NotEnoughMoneyException {
        Client firstClient = exchange.registerNewClient();
        firstClient.putMoney(sellingCurrency, BigDecimal.valueOf(305));

        Client secondClient = exchange.registerNewClient();
        secondClient.putMoney(buyuingCurrency, BigDecimal.valueOf(10));
        System.out.println("\nBefore deal: ");
        System.out.println("First client account: " + firstClient.getAccount());
        System.out.println("Second client account: " + secondClient.getAccount());

        exchange.registerNewOrder(new Order(secondClient, buyuingCurrency, sellingCurrency, BigDecimal.valueOf(610), BigDecimal.valueOf(1.0 / 61)));
        exchange.registerNewOrder(new Order(firstClient, sellingCurrency, buyuingCurrency, BigDecimal.valueOf(5), BigDecimal.valueOf(61)));

        System.out.println("\nAfter deal: ");
        System.out.println("First client account: " + firstClient.getAccount());
        System.out.println("Second client account: " + secondClient.getAccount());
        Assertions.assertEquals(BigDecimal.ZERO.setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                firstClient.getAccount().get(sellingCurrency));
        Assertions.assertEquals(BigDecimal.valueOf(5).setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                firstClient.getAccount().get(buyuingCurrency));

        Assertions.assertEquals(BigDecimal.valueOf(5).setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                secondClient.getAccount().get(buyuingCurrency));
        Assertions.assertEquals(BigDecimal.valueOf(305).setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                secondClient.getAccount().get(sellingCurrency));
    }

    // Висел ордер на покупку 500 рублей по 40 рублей за доллар (списана сумма 12.5 долларов)
    // Пришел ордер на покупку 10 долларов по 50 рублей за доллар (списана сумма 500 рублей)
    // Сделка совершается по курсу 40 рублей за доллар (т.к. второй ордер по сути может "выбирать" курс => выбирает более выгодный для себя)
    // По первому ордеру:
    // Начисляется 2.5 доллара (в счет списанных 12.5),
    // Начисляется 400 рублей в счет покупки рублей (продажи 10 долларов) по 40 рублей за доллар (стоимость сделки 10 долларов, поэтому 2.5 вернулись)
    // По второму ордеру:
    // Начисляется 100 рублей (в счет списанных 500),
    // Начисляется 10 долларов в счет покупки долларов (продажи 400 рублей) по 40 рублей за доллар (стоимость сделки 400 рублей, поэтому 100 вернулись)
    @Test
    public void ordersClosingWithDiffInRatesWithLessRateTest() throws NotEnoughMoneyException {
        Client firstClient = exchange.registerNewClient();
        firstClient.putMoney(sellingCurrency, BigDecimal.valueOf(500));

        Client secondClient = exchange.registerNewClient();
        secondClient.putMoney(buyuingCurrency, BigDecimal.valueOf(12.5));
        System.out.println("\nBefore deal: ");
        System.out.println("First client account: " + firstClient.getAccount());
        System.out.println("Second client account: " + secondClient.getAccount());

        exchange.registerNewOrder(new Order(secondClient, buyuingCurrency, sellingCurrency, BigDecimal.valueOf(500), BigDecimal.valueOf(1.0 / 40)));
        exchange.registerNewOrder(new Order(firstClient, sellingCurrency, buyuingCurrency, BigDecimal.valueOf(10), BigDecimal.valueOf(50)));

        System.out.println("\nAfter deal: ");
        System.out.println("First client account: " + firstClient.getAccount());
        System.out.println("Second client account: " + secondClient.getAccount());
        Assertions.assertEquals(BigDecimal.valueOf(100).setScale(BigDecimalUtils.SCALE,
                BigDecimalUtils.ROUNDING_MODE), firstClient.getAccount().get(sellingCurrency));
        Assertions.assertEquals(BigDecimal.valueOf(10).setScale(BigDecimalUtils.SCALE,
                BigDecimalUtils.ROUNDING_MODE), firstClient.getAccount().get(buyuingCurrency));

        Assertions.assertEquals(BigDecimal.valueOf(2.5).setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                secondClient.getAccount().get(buyuingCurrency));
        Assertions.assertEquals(BigDecimal.valueOf(400).setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                secondClient.getAccount().get(sellingCurrency));
    }

    // Висел ордер на покупку 10 долларов по 50 рублей за доллар (списанна сумма 500 рублей)
    // Пришел ордер на покупку 500 рублей по 40 рублей за доллар (списана сумма 12.5 долларов)
    // Сделка совершается по курсу 50 рублей за доллар (т.к. второй ордер по сути может "выбирать" курс => выбирает более выгодный для себя)
    // По первому ордеру:
    // Начисляется 10 долларов в счет покупки долларов (продажи 500 рублей) по 50 рублей за доллар (стоимость сделки 500 рублей, поэтому ничего не вернулось)
    // По второму ордеру:
    // Начисляется 2.5 доллара (в счет списанных 12.5),
    // Начисляется 500 рублей в счет покупки рублей (продажи 10 долларов) по 50 рублей за доллар (стоимость сделки 10 долларов, поэтому 2.5 вернулись)
    @Test
    public void ordersClosingWithDiffInRatesWithLessRateTest1() throws NotEnoughMoneyException {
        Client firstClient = exchange.registerNewClient();
        firstClient.putMoney(sellingCurrency, BigDecimal.valueOf(500));

        Client secondClient = exchange.registerNewClient();
        secondClient.putMoney(buyuingCurrency, BigDecimal.valueOf(12.5));
        System.out.println("\nBefore deal: ");
        System.out.println("First client account: " + firstClient.getAccount());
        System.out.println("Second client account: " + secondClient.getAccount());

        exchange.registerNewOrder(new Order(firstClient, sellingCurrency, buyuingCurrency, BigDecimal.valueOf(10), BigDecimal.valueOf(50)));
        exchange.registerNewOrder(new Order(secondClient, buyuingCurrency, sellingCurrency, BigDecimal.valueOf(500), BigDecimal.valueOf(1.0 / 40)));

        System.out.println("\nAfter deal: ");
        System.out.println("First client account: " + firstClient.getAccount());
        System.out.println("Second client account: " + secondClient.getAccount());
        Assertions.assertEquals(BigDecimal.ZERO.setScale(BigDecimalUtils.SCALE,
                BigDecimalUtils.ROUNDING_MODE), firstClient.getAccount().get(sellingCurrency));
        Assertions.assertEquals(BigDecimal.valueOf(10).setScale(BigDecimalUtils.SCALE,
                BigDecimalUtils.ROUNDING_MODE), firstClient.getAccount().get(buyuingCurrency));

        Assertions.assertEquals(BigDecimal.valueOf(2.5).setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                secondClient.getAccount().get(buyuingCurrency));
        Assertions.assertEquals(BigDecimal.valueOf(500).setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                secondClient.getAccount().get(sellingCurrency));
    }
}