package ru.rsreu.exchange.order;

import ru.rsreu.exchange.Client;
import ru.rsreu.exchange.currency.Currency;

import java.math.BigDecimal;
import java.util.Objects;

public class Order {
    private final Client client;
    private final Currency sellingCurrency;
    private final Currency buyingCurrency;
    private final BigDecimal buyingValue;
    private final BigDecimal rate; // selling / buying;

    public Order(Client client, Currency sellingCurrency, Currency buyingCurrency, BigDecimal buyingValue, BigDecimal rate) {
        this.client = client;
        this.sellingCurrency = sellingCurrency;
        this.buyingCurrency = buyingCurrency;
        this.buyingValue = buyingValue;
        this.rate = rate;
    }

    public Client getClient() {
        return client;
    }

    public Currency getSellingCurrency() {
        return sellingCurrency;
    }

    public Currency getBuyingCurrency() {
        return buyingCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public BigDecimal getBuyingValue() {
        return buyingValue;
    }

    @Override
    public String toString() {
        return "Order{" +
                "client=" + client +
                ", sellingCurrency=" + sellingCurrency +
                ", buyingCurrency=" + buyingCurrency +
                ", buyingValue=" + buyingValue +
                ", rate=" + rate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return client.equals(order.client) && sellingCurrency == order.sellingCurrency && buyingCurrency == order.buyingCurrency && buyingValue.equals(order.buyingValue) && rate.equals(order.rate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(client, sellingCurrency, buyingCurrency, buyingValue, rate);
    }
}