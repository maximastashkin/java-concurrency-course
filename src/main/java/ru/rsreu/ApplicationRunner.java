package ru.rsreu;

import ru.rsreu.exchange.Client;
import ru.rsreu.exchange.Exchange;
import ru.rsreu.exchange.SimpleExchangeImpl;
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
        exchange.putMoney(new ClientAccountOperationDto(firstClient, Currency.RUR, BigDecimal.valueOf(1000)));
        exchange.putMoney(new ClientAccountOperationDto(firstClient, Currency.USD, BigDecimal.valueOf(150)));
        exchange.putMoney(new ClientAccountOperationDto(firstClient, Currency.USD, BigDecimal.valueOf(150)));
        exchange.putMoney(new ClientAccountOperationDto(secondClient, Currency.USD, BigDecimal.valueOf(50)));
        System.out.println(firstClient.getAccount());
        System.out.println(secondClient.getAccount());
        exchange.registerNewOrder(new Order(firstClient, Currency.RUR, Currency.USD, BigDecimal.valueOf(100), BigDecimal.valueOf(10)));
        System.out.println(exchange.getAllOpenedOrders());
    }
}
