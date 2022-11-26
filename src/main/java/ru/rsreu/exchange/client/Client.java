package ru.rsreu.exchange.client;

import ru.rsreu.exchange.currency.Currency;
import ru.rsreu.exchange.util.BigDecimalUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import static ru.rsreu.exchange.util.CurrencyUtils.getCurrenciesCount;

public class Client {
    private final UUID id;

    private final Map<Currency, BigDecimal> account;

    {
        account = new ConcurrentHashMap<>(
                getCurrenciesCount(), 1, getCurrenciesCount());
        for (Currency currency : Currency.values()) {
            account.put(currency, BigDecimal.ZERO);
        }
    }

    public Client() {
        this.id = UUID.randomUUID();
    }

    public void putMoney(Currency currency, BigDecimal value) {
        account.compute(currency, (key, oldValue) -> oldValue == null ? value :
                oldValue
                        .setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE)
                        .add(value.setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE)));
    }

    public boolean takeMoney(Currency currency, BigDecimal value) {
        if (BigDecimal.ZERO.compareTo(value) > 0) {
            throw new IllegalArgumentException("Value must be more than zero!");
        }
        class MoneyTaker implements BiFunction<Currency, BigDecimal, BigDecimal> {
            private boolean success = true;

            @Override
            public BigDecimal apply(Currency currency, BigDecimal oldValue) {
                if (value.setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE)
                        .compareTo(oldValue.setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE)) > 0) {
                    success = false;
                    return oldValue;
                }
                return oldValue
                        .setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE)
                        .subtract(value.setScale(BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE));
            }
        }
        MoneyTaker taker = new MoneyTaker();
        account.compute(currency, taker);
        return taker.success;
    }

    public Map<Currency, BigDecimal> getAccount() {
        return Collections.unmodifiableMap(account);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return id.equals(client.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}