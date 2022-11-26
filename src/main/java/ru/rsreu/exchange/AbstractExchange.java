package ru.rsreu.exchange;

import ru.rsreu.exchange.client.Client;
import ru.rsreu.exchange.dto.ClientAccountOperationDto;
import ru.rsreu.exchange.exception.NotEnoughMoneyException;

public abstract class AbstractExchange implements Exchange {
    @Override
    public Client registerNewClient() {
        return new Client();
    }

    @Override
    public void putMoney(ClientAccountOperationDto clientAccountOperationDto) {
        clientAccountOperationDto.getClient().putMoney(clientAccountOperationDto.getCurrency(), clientAccountOperationDto.getValue());
    }

    @Override
    public void takeMoney(ClientAccountOperationDto clientAccountOperationDto) throws NotEnoughMoneyException {
        if (!clientAccountOperationDto.getClient().takeMoney(clientAccountOperationDto.getCurrency(), clientAccountOperationDto.getValue())) {
            throw new NotEnoughMoneyException("Not enough money for operation");
        }
    }
}
