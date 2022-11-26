package ru.rsreu.exchange.base;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.rsreu.exchange.Exchange;
import ru.rsreu.exchange.client.Client;
import ru.rsreu.exchange.currency.Currency;
import ru.rsreu.exchange.dto.ClientAccountOperationDto;
import ru.rsreu.exchange.exception.NotEnoughMoneyException;
import ru.rsreu.exchange.util.BigDecimalUtils;

import java.math.BigDecimal;

public abstract class ExchangeClientMoneyTest {
    private final Exchange exchange;

    protected ExchangeClientMoneyTest(Exchange exchange) {
        this.exchange = exchange;
    }

    @Test
    public void putMoneyClientTest() {
        Client client = exchange.registerNewClient();
        Currency testCurrency = Currency.RUR;
        ClientAccountOperationDto.Builder dtoBuilder = new ClientAccountOperationDto.Builder(client);
        exchange.putMoney(dtoBuilder.withCurrency(testCurrency).withValue(100).build());
        Assertions.assertEquals(BigDecimal.valueOf(100).setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                client.getAccount().get(testCurrency));
    }

    @Test
    public void takeMoneyClientTest() throws NotEnoughMoneyException {
        Client client = exchange.registerNewClient();
        Currency testCurrency = Currency.USD;
        client.putMoney(Currency.USD, BigDecimal.valueOf(555));
        ClientAccountOperationDto.Builder dtoBuilder = new ClientAccountOperationDto.Builder(client);
        exchange.takeMoney(dtoBuilder.withCurrency(testCurrency).withValue(500.45).build());
        Assertions.assertEquals(BigDecimal.valueOf(555 - 500.45).setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE),
                client.getAccount().get(testCurrency));
    }

    @Test
    public void notEnoughMoneyClientTest() {
        Client client = exchange.registerNewClient();
        Currency testCurrency = Currency.USD;
        ClientAccountOperationDto.Builder dtoBuilder = new ClientAccountOperationDto.Builder(client);
        Assertions.assertThrows(NotEnoughMoneyException.class, () ->
                exchange.takeMoney(dtoBuilder.withCurrency(testCurrency).withValue(1000).build()));
    }

    @Test
    public void illegalTakingMoneyValueClientTest() {
        Client client = exchange.registerNewClient();
        Currency testCurrency = Currency.USD;
        ClientAccountOperationDto.Builder dtoBuilder = new ClientAccountOperationDto.Builder(client);
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                exchange.takeMoney(dtoBuilder.withCurrency(testCurrency).withValue(-500).build()));
    }
}