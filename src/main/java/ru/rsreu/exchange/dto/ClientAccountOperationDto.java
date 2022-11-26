package ru.rsreu.exchange.dto;

import ru.rsreu.exchange.client.Client;
import ru.rsreu.exchange.currency.Currency;

import java.math.BigDecimal;

public class ClientAccountOperationDto {
    private final Client client;
    private final Currency currency;
    private final BigDecimal value;

    private ClientAccountOperationDto(Builder builder) {
        this.client = builder.client;
        this.currency = builder.currency;
        this.value = builder.value;
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

    public static class Builder {
        private final Client client;
        private Currency currency;
        private BigDecimal value;

        public Builder(Client client) {
            this.client = client;
        }

        public Builder withCurrency(Currency currency) {
            this.currency = currency;
            return this;
        }

        public Builder withValue(BigDecimal value) {
            this.value = value;
            return this;
        }

        public Builder withValue(long value) {
            this.value = BigDecimal.valueOf(value);
            return this;
        }

        public Builder withValue(double value) {
            this.value = BigDecimal.valueOf(value);
            return this;
        }

        public ClientAccountOperationDto build() {
            return new ClientAccountOperationDto(this);
        }
    }
}