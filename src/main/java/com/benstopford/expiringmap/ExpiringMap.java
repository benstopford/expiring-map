package com.benstopford.expiringmap;

import java.util.*;

/**
 * HashMap backed cache that provides configurable expiry.
 *
 * Entries will be validated for expiry on any action of the interface.
 *
 * Expiry times for each entry are held chronologically so that only
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
    public synchronized void put(K entryKey, V value, long timeoutMs) {
        removeExpiredEntries();

        flagForFutureExpiry(entryKey, timeoutMs);

        map.put(entryKey, value);
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
            } else {
                //all subsequent entries will be in the future
                break;
            }
        }
    }

    private void flagForFutureExpiry(K entryKey, long timeoutMs) {

        if (timeoutMs < 0)
            throw new IllegalArgumentException("Timeout must be a positive value");

        long expiryTime = clock.now() + timeoutMs;
        if (expiryTime < 0)
            expiryTime = Long.MAX_VALUE;

        save(entryKey, expiryTime);
    }

    private void save(K entryKey, long expiryTime) {
        List<K> keys = orderedExpiryTimes.get(expiryTime);
        if (keys == null)
            keys = new ArrayList<>();
        keys.add(entryKey);

        orderedExpiryTimes.put(expiryTime, keys);
    }
}
