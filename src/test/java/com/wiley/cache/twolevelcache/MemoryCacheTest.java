package com.wiley.cache.twolevelcache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MemoryCacheTest {
    private static final String KEY0 = "key0";
    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final String KEY3 = "key3";
    private static final Integer VALUE0 = 0;
    private static final Integer VALUE1 = 1;
    private static final Integer VALUE2 = 2;
    private static final Integer VALUE3 = 3;

    private MemoryCache<String, Integer> memoryCache;

    @Before
    public void setMemoryCache() {
        memoryCache = new MemoryCache<>(2);
    }

    @After
    public void clearCache() {
        memoryCache.clearCache();
    }

    @Test
    public void testPutGetRemoveObject() {
        memoryCache.putIntoCache(KEY0, VALUE0);
        assertEquals(VALUE0, memoryCache.getFromCache(KEY0));
        assertEquals(1, memoryCache.getCacheSize());

        memoryCache.deleteFromCache(KEY0);
        assertNull(memoryCache.getFromCache(KEY0));
    }

    @Test
    public void testGetObjectFailedWhenObjectIsNotContained() {
        memoryCache.putIntoCache(KEY0, VALUE0);
        assertEquals(VALUE0, memoryCache.getFromCache(KEY0));
        assertNull(memoryCache.getFromCache(KEY2));
    }

    @Test
    public void testDeleteObjectFailedWhenObjectIsNotContained() {
        memoryCache.putIntoCache(KEY0, VALUE0);
        assertEquals(VALUE0, memoryCache.getFromCache(KEY0));
        assertEquals(1, memoryCache.getCacheSize());

        memoryCache.deleteFromCache(KEY2);

        assertEquals(VALUE0, memoryCache.getFromCache(KEY0));
        assertEquals(1, memoryCache.getCacheSize());
    }

    @Test
    public void testGetCacheSize() {
        memoryCache.putIntoCache(KEY0, VALUE0);
        assertEquals(1, memoryCache.getCacheSize());

        memoryCache.putIntoCache(KEY1, VALUE1);
        assertEquals(2, memoryCache.getCacheSize());
    }

    @Test
    public void testIsObjectContained() {
        assertFalse(memoryCache.isObjectContained(KEY0));

        memoryCache.putIntoCache(KEY0, VALUE0);
        assertTrue(memoryCache.isObjectContained(KEY0));
    }

    @Test
    public void testHasFreeSpace() {
        memoryCache = new MemoryCache<>(4);

        memoryCache.putIntoCache(KEY0, VALUE0);
        memoryCache.putIntoCache(KEY1, VALUE1);
        memoryCache.putIntoCache(KEY2, VALUE2);

        assertTrue(memoryCache.hasFreeSpace());
        memoryCache.putIntoCache(KEY3, VALUE3);
        assertFalse(memoryCache.hasFreeSpace());
    }

    @Test
    public void testClearCache() {
        memoryCache.putIntoCache(KEY0, VALUE0);
        memoryCache.putIntoCache(KEY1, VALUE1);

        assertEquals(2, memoryCache.getCacheSize());
        memoryCache.clearCache();
        assertEquals(0, memoryCache.getCacheSize());
    }
}