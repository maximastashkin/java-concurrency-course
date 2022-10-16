package ru.rsreu;

import ru.rsreu.counter_holder.CounterHolder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Consumer;

public class FileSymbolCounter {
    private final String fileName;
    private final CounterHolder counterHolder;
    private final char symbol;

    public FileSymbolCounter(String fileName, char symbol, CounterHolder counterHolder) {
        this.fileName = fileName;
        this.symbol = symbol;
        this.counterHolder = counterHolder;
    }

    public void count(int limit, Consumer<Integer> limitCallback) throws InterruptedException {
        try (FileReader fileReader = new FileReader(fileName)) {
            performCounting(limit, limitCallback, fileReader);
        } catch (FileNotFoundException e) {
            System.out.printf("File with name '%s' doesn't exist%n", fileName);
        } catch (IOException e) {
            System.out.printf("Error in file '%s' reading", fileName);
        }
    }

    private void performCounting(int limit, Consumer<Integer> limitCallback, FileReader fileReader)
            throws IOException, InterruptedException {
        int current;
        while ((current = fileReader.read()) != -1) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            if (current == symbol) {
                int currentGlobal = counterHolder.incrementAndGet();
                if (currentGlobal == limit) {
                    limitCallback.accept(currentGlobal);
                }
            }
        }
    }
}