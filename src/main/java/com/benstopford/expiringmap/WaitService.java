package com.benstopford.expiringmap;

public interface WaitService {

    void wait(Object monitor, long ms, int ns) throws InterruptedException;
}
