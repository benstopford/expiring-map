package com.benstopford.expiringmap;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AcceptanceTest {


    @Test
    public void shouldExpireBatchOfValuesInAllotedTime() throws InterruptedException {
        ExpiringMap<Integer, String> map = new ExpiringMap<>();

        int expiry = 1000;
        int numEntries = 1000;

        for (int i = 0; i < numEntries; i++) {
            map.put(i, "value" + i, expiry);
        }

        assertThat(map.size(), is(numEntries));

        Thread.sleep(expiry+10);

        assertThat(map.size(), is(0));
    }

}
