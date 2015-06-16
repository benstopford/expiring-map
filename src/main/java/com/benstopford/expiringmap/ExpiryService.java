package com.benstopford.expiringmap;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class ExpiryService<K> {

    public void attemptExpiry(Clock clock, WaitService waitService, BlockingQueue<ExpiryEntry<K>> queue, Map<K, ?> backingMap) throws InterruptedException {
        ExpiryEntry head = queue.take();

        if (head.expiry() <= clock.now()) {
            backingMap.remove(head.key());
        } else {
            waitForItToExpire(clock, waitService, queue, head);
        }
    }

    private void waitForItToExpire(Clock clock, WaitService waitService, BlockingQueue<ExpiryEntry<K>> queue, ExpiryEntry head) throws InterruptedException {
        long waitTime = head.expiry() - clock.now();
        queue.offer(head); //put it back before we wait
        if (waitTime > 0) {
            long ms = (long) Math.floor(waitTime / 1000000);
            int ns = (int) Math.floor(waitTime % 1000000);
            synchronized (WaitService.class) {
                //ensure head has not been replaced (with more immanently expiring value) before entering wait
                if (queue.peek().key().equals(head.key()))
                    waitService.doWait(ms, ns);
            }
        }
    }
}
