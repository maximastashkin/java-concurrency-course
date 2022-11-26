package ru.rsreu.exchange.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.rsreu.exchange.Exchange;
import ru.rsreu.exchange.client.Client;
import ru.rsreu.exchange.currency.Currency;
import ru.rsreu.exchange.exception.NotEnoughMoneyException;
import ru.rsreu.exchange.generator.DummyOrderStubGenerator;
import ru.rsreu.exchange.generator.OrderStubGeneratorConfiguration;
import ru.rsreu.exchange.order.Order;
import ru.rsreu.exchange.util.BigDecimalUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public abstract class ExchangeThreadsStressTest {
    private static final int TOTAL_CLIENTS_COUNT = 10;
    private static final int ORDERS_PER_CLIENT = 30000;
    private static final BigDecimal BASE_CLIENT_MONEY = BigDecimal.valueOf(Integer.MAX_VALUE);

    private final Exchange exchange;
    private final DummyOrderStubGenerator dummyOrderStubGenerator;

    protected ExchangeThreadsStressTest(Exchange exchange) {
        this.exchange = exchange;
        this.dummyOrderStubGenerator = new DummyOrderStubGenerator(new OrderStubGeneratorConfiguration());
    }

    @Test
    public void stressTest() throws InterruptedException {
        List<Client> clients = new ArrayList<>();
        for (int i = 0; i < TOTAL_CLIENTS_COUNT; i++) {
            Client client = exchange.registerNewClient();
            for (Currency currency : Currency.values()) {
                client.putMoney(currency, BASE_CLIENT_MONEY);
            }
            clients.add(client);
        }

        List<Thread> clientThreads = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(TOTAL_CLIENTS_COUNT);
        for (Client client : clients) {
            clientThreads.add(new Thread(() -> {
                try {
                    countDownLatch.countDown();
                    countDownLatch.await();
                    for (int i = 0; i < ORDERS_PER_CLIENT; i++) {
                        Order order = dummyOrderStubGenerator.generateRandomOrder(client);
                        exchange.registerNewOrder(order);
                    }
                } catch (NotEnoughMoneyException e) {
                    System.err.println("Not enough money client");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        long start = System.currentTimeMillis();
        for (Thread thread : clientThreads) {
            thread.start();
        }
        for (Thread thread : clientThreads) {
            thread.join();
        }
        System.out.printf("%s exchange %.2f orders per second\n", exchange.getClass().getName(),
                (TOTAL_CLIENTS_COUNT * ORDERS_PER_CLIENT) / ((System.currentTimeMillis() - start) / 1000.0));
        Map<Currency, BigDecimal> exchangeBalance = new HashMap<>();
        for (Currency currency : Currency.values()) {
            exchangeBalance.put(currency, BigDecimal.valueOf(TOTAL_CLIENTS_COUNT)
                    .multiply(BASE_CLIENT_MONEY.setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE)));
        }
        for (Order order : exchange.getAllOpenedOrders()) {
            exchange.declineOrder(order);
        }
        Map<Currency, BigDecimal> clientsFinalBalance = new HashMap<>();
        for (Client client : clients) {
            Map<Currency, BigDecimal> clientBalance = client.getAccount();
            for (Currency currency : Currency.values()) {
                clientsFinalBalance.put(currency, clientsFinalBalance.getOrDefault(currency, BigDecimal.ZERO).add(clientBalance.get(currency)));
            }
        }
        Assertions.assertEquals(exchangeBalance, clientsFinalBalance);
    }
}