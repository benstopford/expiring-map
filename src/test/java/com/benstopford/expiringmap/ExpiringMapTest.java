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
    public void shouldOnlyAttemptExpiryOnValidEntries() {
        Clock clock = mock(Clock.class);
        ExpiringMap<String, String> map = new ExpiringMap(clock);

        when(clock.now()).thenReturn(100L);

        map.put("key1", "value1", 5);
        map.put("key2", "value2", 6);
        map.put("key3", "value3", 7);
        map.put("key4", "value4", 8);
        map.put("key5", "value5", 9);

        reset(clock);
        when(clock.now()).thenReturn(106L);
        map.get("whatever");

        verify(clock, times(3)).now();
    }

}
    

