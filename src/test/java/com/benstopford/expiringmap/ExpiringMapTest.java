package com.benstopford.expiringmap;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class ExpiringMapTest {
    public static final int sleepTime = 100;
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
    public void shouldSupportTimeoutsOfAnyPositiveLong() {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap<>();

        //When
        map.put("k", "v", Long.MAX_VALUE);

        //Then
        assertThat(map.get("k"), is("v"));
    }

    @Test
    public void shouldNotExpireEntriesWithFutureExpiryTimes() {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap<>(() -> now);
        now = 0;
        int expiresIn = 10;
        map.put("key1", "value1", expiresIn);

        //When
        now += MILLISECONDS.toNanos(5);

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

        Thread.sleep(sleepTime);

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
        Thread.sleep(sleepTime);

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

        Thread.sleep(sleepTime);

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

        Thread.sleep(1000);

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

        Thread.sleep(sleepTime);

        assertThat(map.get("key1"), is("value1"));
        assertThat(map.get("key2"), is(nullValue()));
        assertThat(map.get("key3"), is("value3"));
    }


    @Test
    public void shouldWaitIfExpiryTimeIsNotReached() throws Exception {
        WaitService waitService = mock(WaitService.class);

        //Given
        ExpiringMap<String, String> map = new ExpiringMap<>(() -> now, waitService);
        now = 0;

        //When
        map.put("key1", "value1", 5);

        now = 1600000;

        Thread.sleep(sleepTime);

        verify(waitService, atLeastOnce()).wait(waitService, 3, 400000);

    }


    @Test
    public void shouldWakeEvictionThreadWhenEarlierExpiringEntryArrives() throws InterruptedException {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap<>(() -> now);
        now = 0;

        //When
        map.put("key1", "value1", 25);


        Thread.sleep(sleepTime);

        //eviction thread should now be sleeping

        //adding value with shorter expiry - this should cause the eviction thread to wake
        map.put("key2", "value2", 1);
        assertThat(map.get("key2"), is("value2"));

        //bump time forward a ms so that key2 expires
        now += 1 * 1000000;

        Thread.sleep(sleepTime); //let evictino run

        //key2 should have been evicted
        assertThat(map.get("key2"), is(nullValue()));

        //key1 should still be there
        assertThat(map.get("key1"), is("value1"));
    }


    @Test
    public void shouldEvictBasedOnNsDifferencesInTime() throws InterruptedException {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap<>(() -> now);
        now = 0;

        //When
        map.put("key1", "value1", 1);

        now += 1; //bump time by 2ns


        map.put("key2", "value2", 1);

        now+=999999; //bump forward to 1ms (so key1 should evict but not key2)

        Thread.sleep(sleepTime);

        //first shoudl have been evicted
        assertThat(map.get("key1"), is(nullValue()));
        assertThat(map.get("key2"), is("value2"));

        now += 1; //bump time by 1ns

        Thread.sleep(sleepTime);

        //second should now evict
        assertThat(map.get("key2"), is(nullValue()));
    }

}
    


