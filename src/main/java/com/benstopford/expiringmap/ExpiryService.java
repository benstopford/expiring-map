package com.benstopford.expiringmap;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class ExpiryService<K> {

    public void attemptExpiry(Clock clock, WaitService waitService, BlockingQueue<ExpiryEntry<K>> queue, Map<K, ?> backingMap) throws InterruptedException {
            ExpiryEntry entry = queue.take();
            if (entry.expiry() <= clock.now()) {
                backingMap.remove(entry.key());
            } else {
                long nanos = entry.expiry() - clock.now();
                queue.offer(entry); //put it back before we wait
                if (nanos > 0) {
                    long ms = (long) Math.floor(nanos / 1000000);
                    int ns = (int) Math.floor(nanos % 1000000);
                    waitService.doWait(ms, ns);
                }
            }
    }
}
