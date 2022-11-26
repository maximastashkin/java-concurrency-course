package ru.rsreu;

import ru.rsreu.exchange.client.Client;
import ru.rsreu.exchange.Exchange;
import ru.rsreu.exchange.simple.SimpleExchangeImpl;
import ru.rsreu.exchange.currency.Currency;
import ru.rsreu.exchange.dto.ClientAccountOperationDto;
import ru.rsreu.exchange.exception.NotEnoughMoneyException;
import ru.rsreu.exchange.order.Order;

import java.math.BigDecimal;

public class ApplicationRunner {
    public static void main(String[] args) throws NotEnoughMoneyException {
        Exchange exchange = new SimpleExchangeImpl();

        Client firstClient = exchange.registerNewClient();
        Client secondClient = exchange.registerNewClient();
        exchange.putMoney(new ClientAccountOperationDto.Builder(firstClient).withCurrency(Currency.RUR).withValue(1000).build());
        exchange.putMoney(new ClientAccountOperationDto.Builder(secondClient).withCurrency(Currency.USD).withValue(1000).build());
        System.out.println(firstClient.getAccount());
        System.out.println(secondClient.getAccount());
        exchange.registerNewOrder(new Order(secondClient, Currency.USD, Currency.RUR, BigDecimal.valueOf(120), BigDecimal.valueOf(1.0/51.2)));
        exchange.registerNewOrder(new Order(firstClient, Currency.RUR, Currency.USD, BigDecimal.valueOf(3), BigDecimal.valueOf(51.3)));
        System.out.println("-----------------------------------------");
        System.out.println(firstClient.getAccount());
        System.out.println(secondClient.getAccount());
        System.out.println(exchange.getAllOpenedOrders());
    }
}
