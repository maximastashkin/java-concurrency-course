package ru.rsreu.exchange.dto;

import ru.rsreu.exchange.OrderRegistrationStatus;
import ru.rsreu.exchange.order.Order;

import java.util.concurrent.CountDownLatch;

public class OrderRequestDto {
    private final CountDownLatch operationCompletedLatch = new CountDownLatch(1);

    private final Order order;

    private OrderRegistrationStatus orderRegistrationStatus;

    public OrderRequestDto(Order order) {
        this.order = order;
    }

    public CountDownLatch getOperationCompletedLatch() {
        return operationCompletedLatch;
    }

    public Order getOrder() {
        return order;
    }

    public OrderRegistrationStatus getOrderRegistrationStatus() {
        return orderRegistrationStatus;
    }

    public void setOrderRegistrationStatus(OrderRegistrationStatus orderRegistrationStatus) {
        this.orderRegistrationStatus = orderRegistrationStatus;
    }
}
