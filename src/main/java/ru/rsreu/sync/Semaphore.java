package ru.rsreu.sync;

public class Semaphore {
    private final static int DEFAULT_TIMEOUT_MILLIS = 5 * 1000 * 60;
    private final static int DEFAULT_TIMEOUT_NANOS = 0;

    private final Object lock = new Object();

    private final int permits;
    private int currentPermits;

    public Semaphore(int permits) {
        this.permits = permits;
    }

    public void acquire() throws InterruptedException {
        acquire(1);
    }

    public void acquire(int permits) throws InterruptedException {
        acquire(permits, DEFAULT_TIMEOUT_MILLIS, DEFAULT_TIMEOUT_NANOS);
    }

    public void acquire(int permits, long timeout, int nanos) throws InterruptedException {
        synchronized (lock) {
            while (currentPermits + permits > this.permits) {
                lock.wait(timeout, nanos);
            }
            currentPermits += permits;
        }
    }

    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    public boolean tryAcquire(int permits) {
        if (permits + currentPermits <= this.permits) {
            synchronized (lock) {
                if (permits + currentPermits <= this.permits) {
                    currentPermits += permits;
                    return true;
                }
            }
        }
        return false;
    }

    public void release() {
        release(1);
    }

    public void release(int permits) {
        if (permits < 0) {
            throw new IllegalArgumentException("Permits must be more than 0");
        }
        synchronized (lock) {
            currentPermits -= permits;
            lock.notifyAll();
        }
    }
}
