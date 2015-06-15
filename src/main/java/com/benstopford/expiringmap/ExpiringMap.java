package com.benstopford.expiringmap;

import java.util.*;

public class ExpiringMap<K, V> implements ExpireMap<K, V> {
    Map<K, V> map = new HashMap();
    Map<Long, List<K>> orderedExpiryTimes = new TreeMap();
    private Clock clock;

    public ExpiringMap(Clock clock) {
        this.clock = clock;
    }

    public ExpiringMap() {
        this(() -> System.currentTimeMillis());
    }

    @Override
    public synchronized void put(K entryKey, V value, long timeoutMs) {
        expire();
        map.put(entryKey, value);

        long expiryTime = clock.now() + timeoutMs;

        saveExpiry(entryKey, expiryTime);
    }

    private void saveExpiry(K entryKey, long expiryTime) {
        long expiry = checkForOverflow(expiryTime);

        List<K> keys = orderedExpiryTimes.get(expiry);
        if (keys == null)
            keys = new ArrayList();
        keys.add(entryKey);

        orderedExpiryTimes.put(expiry, keys);
    }

    private long checkForOverflow(long expiryTime) {
        if (expiryTime < 0)
            expiryTime = Long.MAX_VALUE;
        return expiryTime;
    }

    private void expire() {
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

    @Override
    public synchronized V get(K key) {
        expire();
        return map.get(key);
    }

    @Override
    public synchronized void remove(K key) {
        expire();
        map.remove(key);
    }
}
