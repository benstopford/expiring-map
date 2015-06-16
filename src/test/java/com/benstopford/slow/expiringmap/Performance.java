package com.benstopford.slow.expiringmap;

import com.benstopford.expiringmap.ExpiringMap;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class Performance {


    public static void main(String[] args) {
        new Performance().run();
    }

    public void run() {
        ExpiringMap<Integer, String> map = new ExpiringMap<>();

        int expiry = 5000;
        int numEntries = 100;

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
