package ru.rsreu.sync;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class SemaphoreTest {
    private volatile int t1 = 0;
    private volatile int t2 = 0;

    @RepeatedTest(50)
    public void acquireTest() throws InterruptedException {
        final Semaphore semaphore = new Semaphore(1);
        final CountDownLatch firstLatch = new CountDownLatch(1);
        final CountDownLatch secondLatch = new CountDownLatch(1);
        Thread firstThread = new Thread(() -> {
            try {
                semaphore.acquire();
                t1 = 1;
                firstLatch.countDown();
            } catch (InterruptedException e) {
                System.out.println("Interrupted");
            } finally {
                semaphore.release();
            }
        });
        Thread secondThread = new Thread(() -> {
            try {
                secondLatch.await();
                semaphore.acquire();
                t2 = 1;
                secondLatch.countDown();
            } catch (InterruptedException e) {
                System.out.println("Interrupted");
            } finally {
                semaphore.release();
            }
        });
        firstThread.start();
        secondThread.start();
        firstLatch.await();
        Assertions.assertEquals(1, t1);
        secondLatch.countDown();
        secondThread.join();
        Assertions.assertEquals(1, t2);
    }

    @RepeatedTest(50)
    public void stressTest() throws InterruptedException {
        final AtomicBoolean passed = new AtomicBoolean(true);
        final AtomicInteger counter = new AtomicInteger();
        final Semaphore semaphore = new Semaphore(3);
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            threads.add(new Thread(() -> {
                try {
                    semaphore.acquire();
                    passed.set(passed.get() & counter.incrementAndGet() <= 3);
                    Thread.sleep(10);
                    counter.decrementAndGet();

                } catch (InterruptedException e) {
                    System.out.println("Interrupted");
                } finally {
                    semaphore.release();
                }
            }));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        Assertions.assertTrue(passed.get());
    }
}