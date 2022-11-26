package ru.rsreu.exchange.util;

import ru.rsreu.exchange.currency.Currency;
import ru.rsreu.exchange.currency.CurrencyPair;
import ru.rsreu.exchange.exception.InvalidCurrencyPairException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class CurrencyUtils {
    private static Set<CurrencyPair> cartesianProduct;

    private CurrencyUtils() {
    }

    public static CurrencyPair getCurrencyPairByTwoCurrencies(Currency first, Currency second) {
        for (CurrencyPair pair : getCurrenciesCartesianProduct()) {
            if (pair.hasCurrency(first) && pair.hasCurrency(second) && first != second) {
                return pair;
            }
        }
        throw new InvalidCurrencyPairException(first, second);
    }

    public static int getCurrenciesCount() {
        return Currency.values().length;
    }

    public static Set<CurrencyPair> getCurrenciesCartesianProduct() {
        if (cartesianProduct == null) {
            cartesianProduct = performCartesianProduct();
        }
        return Collections.unmodifiableSet(cartesianProduct);
    }

    private static Set<CurrencyPair> performCartesianProduct() {
        Set<CurrencyPair> cartesianProduct = new HashSet<>();
        for (int i = 0; i < getCurrenciesCount(); i++) {
            for (int j = i + 1; j < getCurrenciesCount(); j++) {
                cartesianProduct.add(new CurrencyPair(Currency.values()[i], Currency.values()[j]));
            }
        }
        return cartesianProduct;
    }
}