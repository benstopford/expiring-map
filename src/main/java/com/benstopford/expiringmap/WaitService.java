package com.benstopford.expiringmap;

public interface WaitService {
    public WaitService DEFAULT = new WaitService() {
        @Override
        public void doWait(long ms, int ns) throws InterruptedException {
            WaitService.class.wait(ms, ns);
        }

        @Override
        public void doNotify() {
            WaitService.class.notifyAll();
        }
    };

    void doWait(long ms, int ns) throws InterruptedException;

    void doNotify();
}
