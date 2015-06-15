package com.benstopford.expiringmap;

import java.util.HashMap;
import java.util.Map;

public class ExpiringMap<K, V> implements ExpireMap<K, V> {
    Map<K, V> map = new HashMap();
    Map<K, Long> timeouts = new HashMap();
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

        timeouts.put(key, checkForOverflow(expiryTime));
    }

    private long checkForOverflow(long expiryTime) {
        if(expiryTime<0)
            expiryTime = Long.MAX_VALUE;
        return expiryTime;
    }

    private void expire() {
        for (K key : map.keySet()) {
            long expiry = timeouts.get(key);
            if (clock.now() >= expiry) {
                map.remove(key);
            }
        }
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
