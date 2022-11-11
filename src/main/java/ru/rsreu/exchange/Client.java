package ru.rsreu.exchange;

import ru.rsreu.exchange.currency.Currency;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import static ru.rsreu.exchange.currency.CurrencyUtils.getCurrenciesCount;

public class Client {
    private final UUID id;

    private final Map<Currency, BigDecimal> account = new ConcurrentHashMap<>(
            getCurrenciesCount(), 1, getCurrenciesCount());

    Client() {
        this.id = UUID.randomUUID();
    }

    void putMoney(Currency currency, BigDecimal value) {
        account.compute(currency, (key, oldValue) -> oldValue == null ? value : oldValue.add(value));
    }

    boolean takeMoney(Currency currency, BigDecimal value) {
        class MoneyTaker implements BiFunction<Currency, BigDecimal, BigDecimal> {
            private boolean success = true;

            @Override
            public BigDecimal apply(Currency currency, BigDecimal oldValue) {
                if (value.compareTo(oldValue) <= 0) {
                    success = false;
                    return oldValue;
                }
                return oldValue.subtract(value);
            }

            public boolean isSuccess() {
                return success;
            }
        }
        MoneyTaker taker = new MoneyTaker();
        account.compute(currency, taker);
        return taker.isSuccess();
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