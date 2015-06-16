package com.benstopford.expiringmap;

public interface WaitService {
    public WaitService DEFAULT = new WaitService() {
        @Override
        public void doWait(long ms, int ns) throws InterruptedException {
            synchronized(WaitService.class) {
                WaitService.class.wait(ms, ns);
            }
        }

        @Override
        public void doNotify() {
            synchronized (WaitService.class) {
                WaitService.class.notifyAll();
            }
        }
    };

    void doWait(long ms, int ns) throws InterruptedException;

    void doNotify();
}
