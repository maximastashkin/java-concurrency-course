package ru.rsreu.exchange.currency;

import ru.rsreu.exchange.exception.SuchPairNotExists;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class CurrencyUtils {
    private static Set<CurrencyPair> cartesianProduct;

    private CurrencyUtils() {
    }

    public static CurrencyPair getCurrencyPairByTwoCurrencies(Currency first, Currency second) {
        for (CurrencyPair pair : getCurrenciesCartesianProduct()) {
            if (pair.hasCurrency(first) && pair.hasCurrency(second)) {
                return pair;
            }
        }
        throw new SuchPairNotExists(first, second);
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