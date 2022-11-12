package ru.rsreu.exchange.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalUtils {
    private BigDecimalUtils() {
    }

    public static BigDecimal getInverseNumber(BigDecimal number) {
        return BigDecimal.ONE.divide(number, 3, RoundingMode.HALF_UP);
    }
}