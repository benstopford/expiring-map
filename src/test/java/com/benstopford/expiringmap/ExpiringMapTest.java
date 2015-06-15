package com.benstopford.expiringmap;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;

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
    public void shouldNotExpireEntriesBeforeExpiryTime() {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap(() -> now);
        now = 42;
        int expiresIn = 10;
        map.put("key1", "value1", expiresIn);

        //When
        now = 55; //i.e. before than 42+10

        //Then
        assertThat(map.get("key1"), is("value1"));
    }

    @Test
    public void shouldExpireEntriesAfterExpiryTime() {
        //Given
        ExpiringMap<String, String> map = new ExpiringMap(() -> now);
        now = 42;
        int expiresIn = 10;
        map.put("key1", "value1", expiresIn);

        //When
        now = 53; //i.e. after than 42+10

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

}
    

