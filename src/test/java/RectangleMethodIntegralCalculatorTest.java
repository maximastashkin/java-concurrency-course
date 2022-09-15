import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.rsreu.calculation.RectangleMethodIntegralCalculator;

import static java.lang.Math.sqrt;

public class RectangleMethodIntegralCalculatorTest {
    private final double testingEpsilon = 1E-6;

    private final RectangleMethodIntegralCalculator calculator = new RectangleMethodIntegralCalculator(testingEpsilon);

    @Test
    public void testIllegalParameters() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                calculator.calculate(1, 0, x -> 1.0));
    }

    @Test
    public void testZeroSquare() {
        Assertions.assertEquals(
                0,
                calculator.calculate(1, 1, x -> 1.0)
        );
    }

    @Test
    public void testRoundFunction() {
        Assertions.assertEquals(
                0.785398,
                calculator.calculate(0, 1, x -> sqrt(1 - x * x)),
                testingEpsilon
        );
    }

    @Test
    public void testSinFunction() {
        Assertions.assertEquals(
                0,
                calculator.calculate(-10, 10, Math::sin),
                testingEpsilon
        );
    }

    @Test
    public void testSqrtFunction() {
        Assertions.assertEquals(
                0.666666,
                calculator.calculate(0, 1, Math::sqrt),
                testingEpsilon
        );
    }

    @Test
    public void calculationPiTest() {
        Assertions.assertEquals(
                Math.PI,
                calculator.calculate(0, 1,  x -> sqrt(1 - x * x)) * 4,
                testingEpsilon * 10
        );
    }
}
