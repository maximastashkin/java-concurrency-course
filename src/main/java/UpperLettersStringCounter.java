public class UpperLettersStringCounter {
    public long countUpperLetters(String consumer) {
        if (consumer == null) {
            return 0;
        }
        return countUpperLetters(consumer.trim().toCharArray());
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
