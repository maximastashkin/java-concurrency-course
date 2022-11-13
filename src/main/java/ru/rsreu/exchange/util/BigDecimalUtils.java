package ru.rsreu.exchange.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalUtils {
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    public static final int SCALE = 5;
    private BigDecimalUtils() {
    }

    public static BigDecimal getInverseNumber(BigDecimal number) {
        return BigDecimal.ONE.divide(number, SCALE, RoundingMode.HALF_UP);
    }
}