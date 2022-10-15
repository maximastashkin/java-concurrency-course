import org.junit.jupiter.api.RepeatedTest;
import ru.rsreu.Runner;

public class MainTest {
    @RepeatedTest(100)
    public void test() {
        Runner.main(new String[] {"a.txt", "b.txt", "c.txt", "d.txt", "e.txt"});
    }
}
