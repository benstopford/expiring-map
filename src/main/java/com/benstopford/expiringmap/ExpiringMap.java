package com.benstopford.expiringmap;

import java.util.*;

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

    private void flagForFutureExpiry(K entryKey, long timeoutMs) {
        long expiryTime = clock.now() + timeoutMs;

        if (timeoutMs < 0) {
            throw new IllegalArgumentException("Timeout must be a positive value");
        }

        if (expiryTime < 0) {
            expiryTime = Long.MAX_VALUE;
            System.err.println("Warning: timeout value too large: Rounding to Long.MAX_VALUE");
        }

        List<K> keys = orderedExpiryTimes.get(expiryTime);
        if (keys == null)
            keys = new ArrayList<>();
        keys.add(entryKey);

        orderedExpiryTimes.put(expiryTime, keys);
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
            if (hasExpired(expiry)) {
                orderedExpiryTimes.get(expiry)
                        .forEach(map::remove);
            } else {
                //all subsequent entries will be in the future
                break;
            }
        }
    }

    private boolean hasExpired(long expiry) {
        return expiry <= clock.now();
    }

}
