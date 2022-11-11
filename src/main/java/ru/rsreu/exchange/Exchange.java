package ru.rsreu.exchange;

import ru.rsreu.exchange.dto.ClientAccountOperationDto;
import ru.rsreu.exchange.exception.NotEnoughMoneyException;
import ru.rsreu.exchange.order.Order;

import java.util.List;

public interface Exchange {
    Client registerNewClient();

    OrderRegistrationStatus registerNewOrder(Order order) throws NotEnoughMoneyException;

    void putMoney(ClientAccountOperationDto clientAccountOperationDto);

    void takeMoney(ClientAccountOperationDto clientAccountOperationDto) throws NotEnoughMoneyException;

    List<Order> getAllOpenedOrders();
}
