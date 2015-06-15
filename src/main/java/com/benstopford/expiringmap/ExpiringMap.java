package com.benstopford.expiringmap;

import java.util.*;

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
    private Map<Long, List<K>> orderedExpiryTimes = new TreeMap<>();
    private Clock clock;

    public ExpiringMap(Clock clock) {
        this.clock = clock;
    }

    public ExpiringMap() {
        this(System::currentTimeMillis);
    }

    @Override
    public synchronized void put(K key, V value, long timeoutMs) {
        removeExpiredEntries();

        recordFutureExpiry(key, timeoutMs);

        map.put(key, value);
    }

    @Override
    public synchronized V get(K key) {
        removeExpiredEntries();
        return map.get(key);
    }

    @Override
    public synchronized void remove(K key) {
        removeExpiredEntries();
        map.remove(key);
    }

    private void removeExpiredEntries() {
        for (long expiry : orderedExpiryTimes.keySet()) {
            if (expiry <= clock.now()) {
                orderedExpiryTimes.get(expiry)
                        .forEach(map::remove);
            } else break; //all subsequent entries will be in the future
        }
    }

    private void recordFutureExpiry(K entryKey, long timeoutMs) {
        long expiryTime = getExpiryTime(timeoutMs);

        List<K> existingKeys = orderedExpiryTimes.get(expiryTime);

        if (existingKeys == null)
            existingKeys = new ArrayList<>();
        existingKeys.add(entryKey);

        orderedExpiryTimes.put(expiryTime, existingKeys);
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
