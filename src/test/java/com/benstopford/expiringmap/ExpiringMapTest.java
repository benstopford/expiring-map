package com.benstopford.expiringmap;

import static java.util.concurrent.TimeUnit.HOURS;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class ExpiringMapTest {
    long now;

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

    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowExceptionForNegativeTimeouts() {
        new ExpiringMap<String, String>().put("k", "v", -5);
    }

    @Test
    public void shouldSupportTimeoutsOfAnyPositiveLong() {
        ExpiringMap<String, String> map = new ExpiringMap<>();
        map.put("k", "v", Long.MAX_VALUE);
        assertThat(map.get("k"), is("v"));
    }

    @Test
    public void shouldNotExpireFutureEntries() {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap<>(() -> now);
        now = 0;
        int expiresIn = 10;
        map.put("key1", "value1", expiresIn);

        //When
        now = 5;

        //Then
        assertThat(map.get("key1"), is("value1"));
    }

    @Test
    public void shouldExpirePastEntries() {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap<>(() -> now);
        now = 0;
        int expiresIn = 10;
        map.put("key1", "value1", expiresIn);

        //When
        now = 11;

        //Then
        assertThat(map.get("key1"), is(nullValue()));
    }

    @Test
    public void shouldExpireEntriesEqualToExpiryTime() {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap<>(() -> now);
        now = 0;
        int expiresIn = 10;
        map.put("key1", "value1", expiresIn);

        //When
        now = 10;

        //Then
        assertThat(map.get("key1"), is(nullValue()));
    }

    @Test
    public void shouldRetrieveMultipleValuesWithSameExpiry() {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap<>(() -> now);
        now = 0;

        //When
        map.put("key1", "value1", 5);
        map.put("key2", "value2", 5);

        //Then
        assertThat(map.get("key1"), is("value1"));
        assertThat(map.get("key2"), is("value2"));
    }

    @Test
    public void shouldExpireMultipleValuesWithSameExpiry() {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap<>(() -> now);
        now = 0;

        //When
        map.put("key1", "value1", 5);
        map.put("key2", "value2", 5);

        now = 6;

        //Then
        assertThat(map.get("key1"), is(nullValue()));
        assertThat(map.get("key2"), is(nullValue()));
    }

    @Test
    public void shouldOnlyAttemptExpiryOnValidEntriesForPerformanceReasons() {
        Clock clock = mock(Clock.class);
        when(clock.now()).thenReturn(0L);
        ExpiringMap<Integer, String> map = new ExpiringMap<>(clock);

        //Given 100 entries
        for (int i = 0; i < 100; i++)
            map.put(i, "value", i);

        reset(clock);

        //When time moves on so five values should expire
        when(clock.now()).thenReturn(4L);
        map.get(-1);

        //Then there should only have been five (plus one) comparisons
        verify(clock, times(6)).now();
    }

}
    

