package com.benstopford.expiringmap;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PerformanceTest {


    public static void main(String[] args) {
        new PerformanceTest().run();
    }

    public void run() {
        ExpiringMap<Integer, String> map = new ExpiringMap<>();

        int expiry = 1000;
        int numEntries = 1000;

        long insertStart = System.nanoTime();
        for (int i = 0; i < numEntries; i++) {
            map.put(i, "value" + i, expiry);
        }
        long insertEnd = System.nanoTime();

        assertThat(map.size(), is(numEntries));

        do {
        }while(map.size()>0);

        long expiryEnd = System.nanoTime();

        System.out.printf("insert took: %,d\n" , (insertEnd - insertStart));
        System.out.printf("expiry min: %,d\n" , (expiryEnd - insertEnd));
        System.out.printf("expiry max: %,d\n" , (expiryEnd - insertStart));

    }

}
