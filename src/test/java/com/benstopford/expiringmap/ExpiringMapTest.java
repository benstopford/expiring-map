package com.benstopford.expiringmap;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

public class ExpiringMapTest {


    @Test
    public void shouldPutAndGetValues() {
        ExpiringMap<String, String> map = new ExpiringMap<String, String>();

        map.put("key1", "value1", Long.MAX_VALUE);
        map.put("key2", "value2", Long.MAX_VALUE);

        assertThat(map.get("key1"), is("value1"));
        assertThat(map.get("key2"), is("value2"));
    }

    @Test
    public void shouldRemoveEntries() {
        ExpiringMap<String, String> map = new ExpiringMap<String, String>();

        map.put("key1", "value1", Long.MAX_VALUE);

        assertThat(map.get("key1"), is("value1"));

        map.remove("key1");

        assertThat(map.get("key1"), is(nullValue()));
    }



}
    

