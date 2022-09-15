import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

public class UpperLettersStringCounterTest {
    private final UpperLettersStringCounter testedCounter = new UpperLettersStringCounter();

    @RepeatedTest(50)
    public void zeroUpperTest() {
        Assertions.assertEquals(testedCounter.countUpperLetters("abcdef"), 0);
    }

    @RepeatedTest(50)
    public void oneUpperTest() {
        Assertions.assertEquals(testedCounter.countUpperLetters("Abcdef"), 1);
    }

    @RepeatedTest(50)
    public void twoUpperTest() {
        Assertions.assertEquals(testedCounter.countUpperLetters("AbcdeF"), 2);
    }

    @RepeatedTest(50)
    public void emptyFirstTest() {
        Assertions.assertEquals(testedCounter.countUpperLetters("   "), 0);
    }

    @RepeatedTest(50)
    public void emptySecondTest() {
        Assertions.assertEquals(testedCounter.countUpperLetters(""), 0);
    }

    @RepeatedTest(50)
    public void nullTest() {
        Assertions.assertEquals(testedCounter.countUpperLetters(null), 0);
    }
}
