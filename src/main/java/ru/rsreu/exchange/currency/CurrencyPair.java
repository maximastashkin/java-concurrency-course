package ru.rsreu.exchange.currency;

public class CurrencyPair {
    private final Currency first;
    private final Currency second;

    CurrencyPair(Currency first, Currency second) {
        this.first = first;
        this.second = second;
    }

    public boolean hasCurrency(Currency currency) {
        return currency == first || currency == second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CurrencyPair that = (CurrencyPair) o;
        if (first == that.first && second == that.second)
            return true;
        return first == that.second && second == that.first;
    }

    @Override
    public int hashCode() {
        return first.hashCode() + second.hashCode();
    }
}
