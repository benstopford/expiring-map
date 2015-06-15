package com.benstopford.expiringmap;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * HashMap backed cache that provides configurable expiry.
 * <p>
 * Expired entries will be removed when any method on the <tt>ExpireMap</tt> interface
 * is invoked.
 * <p>
 * Expiry times for each entry are held in chronological order so that only
 * entries that require expiry will be examined on any operation against
 * this map.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */

public class ExpiringMap<K, V> implements ExpireMap<K, V> {
    private Map<K, V> map = new HashMap<>();
    private Clock clock;
    private PriorityBlockingQueue<Object[]> queue = new PriorityBlockingQueue<>(10, (o1, o2) -> {
        Long t1 = (long) o1[1];
        Long t2 = (long) o2[1];
        return t1.compareTo(t2);
    });

    public ExpiringMap(Clock clock) {
        this.clock = clock;
    }

    boolean started = false;

    private void startPollThread(final Clock clock) {
        if (started) return;
        started = true;

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                while (true) {
                    Object[] entry = queue.take();
                    long expiryTime = (long) entry[1];
                    Object key = entry[0];
                    if (expiryTime <= clock.now()) {
                        map.remove(key);
                    } else {
                        Thread.sleep(expiryTime - clock.now());
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public ExpiringMap() {
        this(System::currentTimeMillis);
    }

    @Override
    public synchronized void put(K key, V value, long timeoutMs) {
        startPollThread(clock);

        recordFutureExpiry(key, timeoutMs);

        map.put(key, value);
    }

    @Override
    public synchronized V get(K key) {
        return map.get(key);
    }

    @Override
    public synchronized void remove(K key) {
        map.remove(key);
    }

    private void recordFutureExpiry(K entryKey, long timeoutMs) {
        long expiryTime = getExpiryTime(timeoutMs);
        queue.add(new Object[]{entryKey, expiryTime});
    }

    private long getExpiryTime(long timeoutMs) {
        if (timeoutMs < 0)
            throw new IllegalArgumentException("Timeout must be a positive value");

        long expiryTime = clock.now() + timeoutMs;

        if (expiryTime < 0)
            expiryTime = Long.MAX_VALUE;

        return expiryTime;
    }
}
