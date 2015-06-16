package com.benstopford.expiringmap;

public class ExpiryEntry<K> {
    private long expiry;
    private K key;

    ExpiryEntry(long expiry, K key) {
        this.expiry = expiry;
        this.key = key;
    }

    Long expiry() {
        return expiry;
    }

    K key() {
        return key;
    }
}
