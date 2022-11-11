package ru.rsreu.exchange.dto;

import ru.rsreu.exchange.Client;
import ru.rsreu.exchange.currency.Currency;

import java.math.BigDecimal;

public class ClientAccountOperationDto {
    private final Client client;
    private final Currency currency;
    private final BigDecimal value;

    public ClientAccountOperationDto(Client client, Currency currency, BigDecimal value) {
        this.client = client;
        this.currency = currency;
        this.value = value;
    }

    public Client getClient() {
        return client;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getValue() {
        return value;
    }
}
