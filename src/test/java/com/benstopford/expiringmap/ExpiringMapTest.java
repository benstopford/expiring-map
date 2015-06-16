package com.benstopford.expiringmap;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;

import com.benstopford.expiringmap.util.CountDownWaitService;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class ExpiringMapTest {
    private long now;

    @Test
    public void shouldPutAndGetValues() {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap<>();

        //When
        map.put("key1", "value1", HOURS.toMillis(1));
        map.put("key2", "value2", HOURS.toMillis(1));

        //Then
        assertThat(map.get("key1"), is("value1"));
        assertThat(map.get("key2"), is("value2"));
    }

    @Test
    public void shouldRemoveEntries() {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap<>();
        map.put("key1", "value1", Long.MAX_VALUE);

        //When
        map.remove("key1");

        //Then
        assertThat(map.get("key1"), is(nullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNegativeTimeouts() {
        new ExpiringMap<String, String>().put("k", "v", -5);
    }

    @Test
    public void shouldSupportTimeoutsOfLargePositiveLong() {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap<>();

        //When
        map.put("k", "v", Long.MAX_VALUE);

        //Then
        assertThat(map.get("k"), is("v"));
    }

    @Test
    public void shouldNotExpireEntriesWithFutureExpiryTimes() throws InterruptedException {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap<>(() -> now);
        now = 0;
        int expiresIn = 10;
        map.put("key1", "value1", expiresIn);

        //When
        now += MILLISECONDS.toNanos(5);

        Thread.sleep(10); //not strictly required

        //Then
        assertThat(map.get("key1"), is("value1"));
    }

    @Test
    public void shouldExpireEntriesWithPastExpiryTimes() throws InterruptedException {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap<>(() -> now);
        now = 0;
        int expiresIn = 10;
        map.put("key1", "value1", expiresIn);

        //When
        now += MILLISECONDS.toNanos(11);

        waitForKeyToBeRemoved("key1", map);

        //Then
        assertThat(map.get("key1"), is(nullValue()));
    }

    @Test
    public void shouldExpireEntriesWithEqualExpiryTime() throws InterruptedException {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap<>(() -> now);
        now = 0;
        int expiresIn = 10;
        map.put("key1", "value1", expiresIn);

        //When
        now += MILLISECONDS.toNanos(10);

        waitForKeyToBeRemoved("key1", map);

        //Then
        assertThat(map.get("key1"), is(nullValue()));
    }

    @Test
    public void shouldRetrieveMultipleValuesWithTheSameExpiry() throws InterruptedException {
        ExpiringMap<String, String> map = new ExpiringMap<>(() -> now);
        now = 0;

        //Given both expire in same ms
        map.put("key1", "value1", 5);
        map.put("key2", "value2", 5);

        //Then
        assertThat(map.get("key1"), is("value1"));
        assertThat(map.get("key2"), is("value2"));
    }

    @Test
    public void shouldExpireMultipleValuesWithSameExpiry() throws InterruptedException {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap<>(() -> now);
        now = 0;

        //When
        map.put("key1", "value1", 5);
        map.put("key2", "value2", 5);

        now += 6 * 1000000;

        waitForKeyToBeRemoved("key1", map);
        waitForKeyToBeRemoved("key2", map);

        //Then
        assertThat(map.get("key1"), is(nullValue()));
        assertThat(map.get("key2"), is(nullValue()));
    }

    @Test
    public void shouldSupportOutOfOrderTimeouts() throws InterruptedException {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap<>(() -> now);
        now = 0;

        //When
        map.put("key1", "value1", 25);
        map.put("key2", "value2", 5);
        map.put("key3", "value3", 15);

        now += MILLISECONDS.toNanos(7);

        waitForKeyToBeRemoved("key2", map);

        assertThat(map.get("key1"), is("value1"));
        assertThat(map.get("key2"), is(nullValue()));
        assertThat(map.get("key3"), is("value3"));
    }

    @Test
    public void shouldWaitIfExpiryTimeIsNotReached() throws Exception {

        CountDownLatch latch = new CountDownLatch(1);

        //Given
        ExpiringMap<String, String> map = new ExpiringMap<>(() -> now, new CountDownWaitService(latch));
        now = 0;

        //When
        map.put("key1", "value1", 5);

        now = 1600000;

        latch.await();

        assertThat(latch.getCount(), is(0L));
    }


    @Test
    public void shouldWakeEvictionThreadWhenEarlierExpiringEntryArrives() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        //Given
        ExpiringMap<String, String> map = new ExpiringMap<>(() -> now, new CountDownWaitService(latch));
        now = 0;

        //When
        map.put("key1", "value1", 25);

        latch.await();

        //add value with shorter expiry - this should cause the eviction thread to wake
        map.put("key2", "value2", 1);
        assertThat(map.get("key2"), is("value2"));

        //bump time forward a ms so that key2 expires
        now += 1000000;

        waitForKeyToBeRemoved("key2", map);

        //key2 should have been evicted
        assertThat(map.get("key2"), is(nullValue()));

        //key1 should still be there
        assertThat(map.get("key1"), is("value1"));
    }


    @Test
    public void shouldEvictBasedOnNanosecondDifferencesInTime() throws InterruptedException {
        ExpiringMap<String, String> map = new ExpiringMap<>(() -> now);
        now = 0;

        //Given we have two entries, 1 nanosecond apart
        map.put("key1", "value1", 1);

        now += 1;

        map.put("key2", "value2", 1);


        //When we move forward to 1ms
        now += 999999;

        waitForKeyToBeRemoved("key1", map);

        //Then the first should have been evicted
        assertThat(map.get("key1"), is(nullValue()));
        assertThat(map.get("key2"), is("value2"));


        //When we bump a single nanosecond forward
        now += 1; //bump time by 1ns

        waitForKeyToBeRemoved("key2", map);

        //Then the second should evict
        assertThat(map.get("key2"), is(nullValue()));
    }

    private void waitForKeyToBeRemoved(String key, ExpiringMap<String, String> map) throws InterruptedException {
        int count = 0;
        while (map.get(key)!=null) {
            Thread.sleep(1);
            if (count++ > 1000)
                throw new RuntimeException("Key took more than 2s to be removed: " + key);
        }
    }
}
    


