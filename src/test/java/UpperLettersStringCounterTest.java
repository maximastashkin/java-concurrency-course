import org.junit.Assert;
import org.junit.Test;

public class UpperLettersStringCounterTest {
    private final UpperLettersStringCounter testedCounter = new UpperLettersStringCounter();

    @Test
    public void zeroUpperTest() {
        Assert.assertEquals(testedCounter.countUpperLetters("abcdef"), 0);
    }

    @Test
    public void oneUpperTest() {
        Assert.assertEquals(testedCounter.countUpperLetters("Abcdef"), 1);
    }

    @Test
    public void twoUpperTest() {
        Assert.assertEquals(testedCounter.countUpperLetters("AbcdeF"), 2);
    }

    @Test
    public void emptyFirstTest() {
        Assert.assertEquals(testedCounter.countUpperLetters("   "), 0);
    }

    @Test
    public void emptySecondTest() {
        Assert.assertEquals(testedCounter.countUpperLetters(""), 0);
    }

    @Test
    public void nullTest() {
        Assert.assertEquals(testedCounter.countUpperLetters(null), 0);
    }
}
