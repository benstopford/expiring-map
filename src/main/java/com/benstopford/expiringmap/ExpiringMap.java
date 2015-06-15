package com.benstopford.expiringmap;

import java.util.HashMap;
import java.util.Map;

public class ExpiringMap<K,V> implements ExpireMap<K,V> {
    Map<K,V> map = new HashMap();
    @Override
    public void put(K key, V value, long timeoutMs) {
        map.put(key, value);
    }

    @Override
    public V get(K key) {
        return map.get(key);
    }

    @Override
    public void remove(K key) {
        map.remove(key);
    }
}
