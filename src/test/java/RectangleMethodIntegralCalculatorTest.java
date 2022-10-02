import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.rsreu.calculation.RectangleMethodIntegralCalculator;

import static java.lang.Math.sqrt;

public class RectangleMethodIntegralCalculatorTest {
    private final double testingEpsilon = 1E-6;

    private final RectangleMethodIntegralCalculator calculator = new RectangleMethodIntegralCalculator(testingEpsilon, calculationProgress);

    @Test
    public void testIllegalParameters() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                calculator.calculate(x -> 1.0, 1, 0));
    }

    @Test
    public void testZeroSquare() throws InterruptedException {
        Assertions.assertEquals(
                0,
                calculator.calculate(x -> 1.0, 1, 1)
        );
    }

    @Test
    public void testRoundFunction() throws InterruptedException {
        Assertions.assertEquals(
                0.785398,
                calculator.calculate(x -> sqrt(1 - x * x), 0, 1),
                testingEpsilon
        );
    }

    @Test
    public void testSinFunction() throws InterruptedException {
        Assertions.assertEquals(
                0,
                calculator.calculate(Math::sin, -10, 10),
                testingEpsilon
        );
    }

    @Test
    public void testSqrtFunction() throws InterruptedException {
        Assertions.assertEquals(
                0.666666,
                calculator.calculate(Math::sqrt, 0, 1),
                testingEpsilon
        );
    }

    @Test
    public void calculationPiTest() throws InterruptedException {
        Assertions.assertEquals(
                Math.PI,
                calculator.calculate(x -> sqrt(1 - x * x), 0, 1) * 4,
                testingEpsilon * 10
        );
    }
}
