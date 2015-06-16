package com.benstopford.expiringmap.util;

public class ExpiryEntry<K> {
    private long expiry;
    private K key;

    public ExpiryEntry(long expiry, K key) {
        this.expiry = expiry;
        this.key = key;
    }

    public Long expiry() {
        return expiry;
    }

    public K key() {
        return key;
    }
}
