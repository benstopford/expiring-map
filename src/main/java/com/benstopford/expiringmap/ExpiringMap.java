package com.benstopford.expiringmap;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ExpiringMap<K, V> implements ExpireMap<K, V> {
    Map<K, V> map = new HashMap();
    Map<Long, K> orderedExpiryTimes = new TreeMap();
    private Clock clock;

    public ExpiringMap(Clock clock) {
        this.clock = clock;
    }

    public ExpiringMap() {
        this(() -> System.currentTimeMillis());
    }

    @Override
    public void put(K key, V value, long timeoutMs) {
        expire();
        map.put(key, value);

        long expiryTime = clock.now() + timeoutMs;

        orderedExpiryTimes.put(checkForOverflow(expiryTime), key);
    }

    private long checkForOverflow(long expiryTime) {
        if(expiryTime<0)
            expiryTime = Long.MAX_VALUE;
        return expiryTime;
    }

    private void expire() {
        for (long expiry : orderedExpiryTimes.keySet()) {
            if (hasExpired(expiry)) {
                K key = orderedExpiryTimes.get(expiry);
                map.remove(key);
            }else{
                //all subsequent entries will be in the future
                break;
            }
        }
    }

    private boolean hasExpired(long expiry) {
        return expiry <= clock.now();
    }

    @Override
    public V get(K key) {
        expire();
        return map.get(key);
    }

    @Override
    public void remove(K key) {
        expire();
        map.remove(key);
    }
}
