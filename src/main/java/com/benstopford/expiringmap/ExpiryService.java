package com.benstopford.expiringmap;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class ExpiryService<K> {

    public void attemptExpiry(Clock clock, WaitService waitService, BlockingQueue<ExpiryEntry<K>> queue, Map<K, ?> backingMap) throws InterruptedException {
        ExpiryEntry head = queue.take();
        if (head.expiry() <= clock.now()) {
            backingMap.remove(head.key());
        } else {
            long nanos = head.expiry() - clock.now();
            queue.offer(head); //put it back before we wait
            if (nanos > 0) {
                long ms = (long) Math.floor(nanos / 1000000);
                int ns = (int) Math.floor(nanos % 1000000);
                synchronized (WaitService.class) {
                    //ensure head has not been replaced (with more immanently expiring value) before entering wait
                    if (queue.peek().key().equals(head.key()))
                        waitService.doWait(ms, ns);
                }

            }
        }
    }
}
