package com.benstopford.expiringmap;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class ExpiryServiceTest {

    public static final int ms = 1000000;
    public static final int us = 1000;
    private ExpiryService service;
    private Clock clock;
    private WaitService waitService;
    private BlockingQueue queue;
    private Map map;

    @Before
    public void setUp() {
        service = new ExpiryService();
        clock = mock(Clock.class);
        waitService = mock(WaitService.class);
        queue = mock(BlockingQueue.class);
        map = mock(Map.class);
    }


    @Test
    public void shouldRemoveValueIfItExpiresInThePast() throws InterruptedException {

        long now = 5L;
        long expiry = 4;
        when(queue.take()).thenReturn(new ExpiryEntry<>(expiry, "key1"));
        when(clock.now()).thenReturn(now);

        service.attemptExpiry(clock, waitService, queue, map);

        verify(map).remove("key1");
    }

    @Test
    public void shouldRemoveValueIfItExpiresAtTheSameTime() throws InterruptedException {

        long now = 5L;
        long expiry = 5;
        when(queue.take()).thenReturn(new ExpiryEntry<>(expiry, "key1"));
        when(clock.now()).thenReturn(now);

        service.attemptExpiry(clock, waitService, queue, map);

        verify(map).remove("key1");
    }

    @Test
    public void shouldNotRemoveValueIfItExpiresInTheFuture() throws InterruptedException {

        long now = 5L;
        long expiry = 6;
        when(queue.take()).thenReturn(new ExpiryEntry<>(expiry, "key1"));
        when(clock.now()).thenReturn(now);

        service.attemptExpiry(clock, waitService, queue, map);

        verify(map, never()).remove("key1");
    }

    @Test
    public void shouldWait1Ms() throws InterruptedException {

        long now = 1 * ms;
        long expiry = 2 * ms;
        when(queue.take()).thenReturn(new ExpiryEntry<>(expiry, "key1"));
        when(clock.now()).thenReturn(now);

        service.attemptExpiry(clock, waitService, queue, map);

        verify(waitService).doWait(1, 0);
    }

    @Test
    public void shouldWait10Ms() throws InterruptedException {

        long now = 2 * ms;
        long expiry = 12 * ms;
        when(queue.take()).thenReturn(new ExpiryEntry<>(expiry, "key1"));
        when(clock.now()).thenReturn(now);

        service.attemptExpiry(clock, waitService, queue, map);

        verify(waitService).doWait(10, 0);
    }

    @Test
    public void shouldWait50us() throws InterruptedException {

        long now = 1 * ms + 50 * us;
        long expiry = 1 * ms + 100 * us;
        when(queue.take()).thenReturn(new ExpiryEntry<>(expiry, "key1"));
        when(clock.now()).thenReturn(now);

        service.attemptExpiry(clock, waitService, queue, map);

        verify(waitService).doWait(0, 50*us);
    }

    @Test
    public void shouldWait50ns() throws InterruptedException {

        long now = 1 * ms + 50;
        long expiry = 1 * ms + 100;
        when(queue.take()).thenReturn(new ExpiryEntry<>(expiry, "key1"));
        when(clock.now()).thenReturn(now);

        service.attemptExpiry(clock, waitService, queue, map);

        verify(waitService).doWait(0, 50);
    }

}
    

