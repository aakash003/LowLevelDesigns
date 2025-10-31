package org.flipkart;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ConcurrentWindowedAverage {

    private static class Entry {
        final String key;
        volatile int value;
        volatile long expiryTime;
        Entry(String k, int v, long e) { key = k; value = v; expiryTime = e; }
    }

    private final ConcurrentHashMap<String, Entry> map;
    private final PriorityBlockingQueue<Entry> expiryQueue;
    private final long expiryWindow;
    private final Object pruneLock = new Object();
    private final AtomicLong sum = new AtomicLong(0);
    private final AtomicInteger count = new AtomicInteger(0);

    public ConcurrentWindowedAverage(long expiryWindowMillis) {
        this.map = new ConcurrentHashMap<>();
        this.expiryQueue = new PriorityBlockingQueue<>(11, Comparator.comparingLong(e -> e.expiryTime));
        this.expiryWindow = expiryWindowMillis;
    }

    public void put(String key, int value) {
        prune();
        long now = System.currentTimeMillis();
        long expiry = now + expiryWindow;

        map.compute(key, (k, existing) -> {
            if (existing != null) {
                sum.addAndGet(value - existing.value);
                existing.value = value;
                existing.expiryTime = expiry;
                expiryQueue.remove(existing);
                expiryQueue.offer(existing);
                return existing;
            } else {
                Entry e = new Entry(k, value, expiry);
                expiryQueue.offer(e);
                sum.addAndGet(value);
                count.incrementAndGet();
                return e;
            }
        });
    }

    public Integer get(String key) {
        prune();
        Entry e = map.get(key);
        if (e == null || e.expiryTime < System.currentTimeMillis()) return -1;
        return e.value;
    }

    public void delete(String key) {
        prune();
        Entry e = map.remove(key);
        if (e != null) {
            expiryQueue.remove(e);
            sum.addAndGet(-e.value);
            count.decrementAndGet();
        }
    }

    public int avg() {
        prune();
        int c = count.get();
        return c == 0 ? 0 : (int)(sum.get() / c);
    }

    private void prune() {
        long now = System.currentTimeMillis();
        synchronized (pruneLock) { // single-threaded cleanup
            while (!expiryQueue.isEmpty() && expiryQueue.peek().expiryTime <= now) {
                Entry expired = expiryQueue.poll();
                if (map.remove(expired.key, expired)) {
                    sum.addAndGet(-expired.value);
                    count.decrementAndGet();
                }
            }
        }
    }


    // --- Testing ---
    public static void main(String[] args) throws InterruptedException {
        ConcurrentWindowedAverage w = new ConcurrentWindowedAverage(3); // expiry = 3ms

        w.put("a", 10);
        System.out.println("avg=" + w.avg()); // 10

        Thread.sleep(1);
        w.put("b", 20);
        System.out.println("avg=" + w.avg()); // 15

        Thread.sleep(1);
        w.put("c", 30);
        System.out.println("avg=" + w.avg()); // 20

        Thread.sleep(2);
        System.out.println("get(a)=" + w.get("a")); // expired → -1
        System.out.println("avg=" + w.avg()); // only b,c alive → (20+30)/2 = 25

        Thread.sleep(2);
        System.out.println("avg=" + w.avg()); // all expired → 0
    }
}
