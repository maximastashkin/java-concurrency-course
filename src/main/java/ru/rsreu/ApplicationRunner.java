package ru.rsreu;


import ru.rsreu.sync.Semaphore;

import java.util.concurrent.ThreadLocalRandom;

public class ApplicationRunner {
    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(3);
        Thread t1 = new Thread(new Task(semaphore));
        t1.setName("T1");
        Thread t2 = new Thread(new Task(semaphore));
        t2.setName("T2");
        Thread t3 = new Thread(new Task(semaphore));
        t3.setName("T3");
        Thread t4 = new Thread(new Task(semaphore));
        t4.setName("T4");
        Thread t5 = new Thread(new Task(semaphore));
        t5.setName("T5");
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
    }
    private static class Task implements Runnable {
        private final Semaphore semaphore;

        private Task(Semaphore semaphore) {
            this.semaphore = semaphore;
        }

        @Override
        public void run() {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                System.out.println("Interrupted");
            }
            for (int i = 0; i < 15; i++) {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(500));
                } catch (InterruptedException e) {
                    System.out.println("Interrupted");
                }
                System.out.println(Thread.currentThread().getName() + ": " + i);

            }
            semaphore.release();
        }
    }
}
