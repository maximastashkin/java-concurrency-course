package ru.rsreu.exchange.disruptor.model;

import com.lmax.disruptor.EventFactory;
import ru.rsreu.exchange.dto.OrderRequestDto;
import ru.rsreu.exchange.order.Order;

public class CreatingOrderRequest {
    public static final EventFactory<CreatingOrderRequest> FACTORY = CreatingOrderRequest::new;

    private OrderRequestDto orderRequest;

    public CreatingOrderRequest() {

    }

    public OrderRequestDto getOrderRequest() {
        return orderRequest;
    }

    public void setOrderRequest(OrderRequestDto orderRequest) {
        this.orderRequest = orderRequest;
    }
}
