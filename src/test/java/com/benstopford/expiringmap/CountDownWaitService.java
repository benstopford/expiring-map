package com.benstopford.expiringmap;

import java.util.concurrent.CountDownLatch;

public class CountDownWaitService implements WaitService{
    private CountDownLatch latch;

    CountDownWaitService(CountDownLatch latch){
        this.latch = latch;
    }
    @Override
    public void doWait(long ms, int ns) throws InterruptedException {
        latch.countDown();
        WaitService.DEFAULT.doWait(ms,ns);
    }

    @Override
    public void doNotify() {
        WaitService.DEFAULT.doNotify();
    }
}
