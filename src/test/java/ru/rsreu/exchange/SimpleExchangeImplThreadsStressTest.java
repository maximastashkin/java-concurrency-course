package ru.rsreu.exchange;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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

class SimpleExchangeImplThreadsStressTest {
    private final Exchange exchange = new SimpleExchangeImpl();
    private final DummyOrderStubGenerator dummyOrderStubGenerator;

    SimpleExchangeImplThreadsStressTest() {
        this.dummyOrderStubGenerator = new DummyOrderStubGenerator(new OrderStubGeneratorConfiguration());
    }

    //@RepeatedTest(10)
    @Test
    public void stressTest() throws InterruptedException {
        List<Client> clients = new ArrayList<>();
        int totalClientCount = 1000;
        for (int i = 0; i < totalClientCount; i++) {
            Client client = exchange.registerNewClient();
            for (Currency currency : Currency.values()) {
                client.putMoney(currency, BigDecimal.valueOf(1000));
            }
            clients.add(client);
        }

        List<Thread> clientThreads = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(totalClientCount);
        for (Client client : clients) {
            clientThreads.add(new Thread(() -> {
                try {
                    countDownLatch.countDown();
                    countDownLatch.await();
                    for (int i = 0; i < 10; i++) {
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
        for (Thread thread : clientThreads) {
            thread.start();
        }
        for (Thread thread : clientThreads) {
            thread.join();
        }

        Map<Currency, BigDecimal> exchangeBalance = new HashMap<>();
        for (Currency currency : Currency.values()) {
            exchangeBalance.put(currency, BigDecimal.valueOf(totalClientCount)
                    .multiply(BigDecimal.valueOf(1000).setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE)));
        }
        System.out.println(exchange.getAllOpenedOrders().size());
        for (Order order : exchange.getAllOpenedOrders()) {
            exchange.declineOrder(order);
        }
        Map<Currency, BigDecimal> clientsFinalBalance = new HashMap<>();
        for (Client client : clients) {
            Map<Currency, BigDecimal> clientBalance = client.getAccount();
            System.out.println(clientBalance);
            for (Currency currency : Currency.values()) {
                clientsFinalBalance.put(currency, clientsFinalBalance.getOrDefault(currency, BigDecimal.ZERO).add(clientBalance.get(currency)));
            }
        }
        Assertions.assertEquals(exchangeBalance, clientsFinalBalance);
    }
}