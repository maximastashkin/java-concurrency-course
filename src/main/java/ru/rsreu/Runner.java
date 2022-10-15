package ru.rsreu;

import ru.rsreu.counter_holder.CounterHolder;
import ru.rsreu.counter_holder.LazyInitCounterHolder;
import ru.rsreu.counter_holder.SimpleCounterHolder;

import java.util.ArrayList;
import java.util.List;

public class Runner {
    public static void main(String[] args) {                                    // handmade DI :)
        CounterHolder counterHolder = new SimpleCounterHolder(0); // */ new LazyInitCounterHolder(0);
        List<Thread> countingThreads = new ArrayList<>();
        for (String arg : args) {
            FileSymbolCounter fileSymbolCounter = new FileSymbolCounter(arg, 'a', counterHolder);
            Thread countingThread = new Thread(() -> {
                try {
                    fileSymbolCounter.count(
                            100,
                            number -> System.out.printf("Thread [%s] reach 'a' number: %d%n",
                                    Thread.currentThread().getName(), number)
                    );
                } catch (InterruptedException e) {
                    System.out.println("Calculation interrupted");
                }
            });
            countingThread.setName(String.format("file processor %s", arg));
            countingThreads.add(countingThread);
        }
        for (Thread thread : countingThreads) {
            thread.start();
        }
        for (Thread thread : countingThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                return;
            }
        }
        System.out.printf("Total 'a' number: %d%n%n", counterHolder.get());
    }
}
