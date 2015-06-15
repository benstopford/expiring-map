package com.benstopford.expiringmap;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class ExpiringMapTest {
    long now;

    @Test
    public void shouldPutAndGetValues() {

        //Given
        ExpiringMap<String, String> map = new ExpiringMap();

        //When
        map.put("key1", "value1", Long.MAX_VALUE);
        map.put("key2", "value2", Long.MAX_VALUE);

        //Then
        assertThat(map.get("key1"), is("value1"));
        assertThat(map.get("key2"), is("value2"));
    }

    @Test
    public void shouldRemoveEntries() {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap();
        map.put("key1", "value1", Long.MAX_VALUE);

        //When
        map.remove("key1");

        //Then
        assertThat(map.get("key1"), is(nullValue()));
    }

    @Test
    public void shouldNotExpireEntriesWithRemainingTime() {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap(() -> now);
        now = 42;
        int expiresIn = 10;
        map.put("key1", "value1", expiresIn);

        //When
        now = 50; //i.e. before 52

        //Then
        assertThat(map.get("key1"), is("value1"));
    }

    @Test
    public void shouldExpireEntriesInThePast() {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap(() -> now);
        now = 42;
        int expiresIn = 10;
        map.put("key1", "value1", expiresIn);

        //When
        now = 55; //i.e. after 52

        //Then
        assertThat(map.get("key1"), is(nullValue()));
    }

    @Test
    public void shouldExpireEntriesEqualToExpiryTime() {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap(() -> now);
        now = 42;
        int expiresIn = 10;
        map.put("key1", "value1", expiresIn);

        //When
        now = 52; //i.e. is 42+10

        //Then
        assertThat(map.get("key1"), is(nullValue()));
    }

    @Test
    public void shouldRetrieveMultipleValuesWithSameExpiry() {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap(() -> now);
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
        ExpiringMap<String, String> map = new ExpiringMap(() -> now);
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
        ExpiringMap<Integer, String> map = new ExpiringMap(clock);
        when(clock.now()).thenReturn(100L);

        //Given 100 entries
        for (int i = 0; i < 100; i++)
            map.put(i, "value", i);

        reset(clock);

        //When time moves on so five values should expire
        when(clock.now()).thenReturn(104L);
        map.get(-1);

        //We only do five plus one comparisons
        verify(clock, times(6)).now();
    }

}
    

