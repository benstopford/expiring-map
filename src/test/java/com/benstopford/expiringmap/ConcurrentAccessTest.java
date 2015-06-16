package com.benstopford.expiringmap;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ConcurrentAccessTest {


    @Test
    public void shouldLoadAndExpireConcurrently() throws InterruptedException {
        ExpiringMap<Integer, String> map = new ExpiringMap<>();

        //Given
        int expiry = 5000;
        int numEntries = 100;
        int numTasks = 16;

        ExecutorService taskExecutor = Executors.newFixedThreadPool(16);

        //When
        for (int i = 1; i <= numTasks; i++)
            taskExecutor.execute(new Runner(i * numEntries, (i + 1) * numEntries, map, expiry));

        taskExecutor.shutdown();
        taskExecutor.awaitTermination(10, TimeUnit.SECONDS);

        //Then
        assertThat(map.size(), is(numEntries * numTasks));
        Thread.sleep(expiry + 10);
        assertThat(map.size(), is(0));
    }


    class Runner implements Runnable {
        private int from;
        private int to;
        private ExpiringMap map;
        private int expiry;

        Runner(int from, int to, ExpiringMap map, int expiry) {
            this.from = from;
            this.to = to;
            this.map = map;
            this.expiry = expiry;
        }

        @Override
        public void run() {
            for (int j = from; j < to; j++) {
                map.put(j, "value" + j, expiry);
            }
        }
    }
}
