public class UpperLettersStringCounter {
    public long countUpperLetters(String consumer) {
        if (consumer == null || (consumer = consumer.trim()).isEmpty()) {
            return 0;
        }
        return countUpperLetters(consumer.toCharArray());
    }

    private long countUpperLetters(char[] consumer) {
        long counter = 0;
        for (char ch : consumer) {
            if (Character.isUpperCase(ch)) {
                counter++;
            }
        }
        return counter;
    }
}
