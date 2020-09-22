package com.wiley.cache.twolevelcache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FileSystemCacheTest {
    private static final String KEY0 = "key0";
    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final String KEY3 = "key3";
    private static final Integer VALUE0 = 0;
    private static final Integer VALUE1 = 1;
    private static final Integer VALUE2 = 2;
    private static final Integer VALUE3 = 3;

    private FileSystemCache<String, Integer> fileSystemCache;

    @Before
    public void setFileSystemCache() {
        fileSystemCache = new FileSystemCache<>(2);
    }

    @After
    public void clearCache() {
        fileSystemCache.clearCache();
    }

    @Test
    public void testPutGetRemoveObject() {
        fileSystemCache.putIntoCache(KEY0, VALUE0);
        assertEquals(VALUE0, fileSystemCache.getFromCache(KEY0));
        assertEquals(1, fileSystemCache.getCacheSize());

        fileSystemCache.deleteFromCache(KEY0);
        assertNull(fileSystemCache.getFromCache(KEY0));
    }

    @Test
    public void testGetObjectFailedWhenObjectIsNotContained() {
        fileSystemCache.putIntoCache(KEY0, VALUE0);
        assertEquals(VALUE0, fileSystemCache.getFromCache(KEY0));
        assertNull(fileSystemCache.getFromCache(KEY2));
    }

    @Test
    public void testDeleteObjectFailedWhenObjectIsNotContained() {
        fileSystemCache.putIntoCache(KEY0, VALUE0);
        assertEquals(VALUE0, fileSystemCache.getFromCache(KEY0));
        assertEquals(1, fileSystemCache.getCacheSize());

        fileSystemCache.deleteFromCache(KEY2);

        assertEquals(VALUE0, fileSystemCache.getFromCache(KEY0));
        assertEquals(1, fileSystemCache.getCacheSize());
    }

    @Test
    public void testGetCacheSize() {
        fileSystemCache.putIntoCache(KEY0, VALUE0);
        assertEquals(1, fileSystemCache.getCacheSize());

        fileSystemCache.putIntoCache(KEY1, VALUE1);
        assertEquals(2, fileSystemCache.getCacheSize());
    }

    @Test
    public void testIsObjectContained() {
        assertFalse(fileSystemCache.isObjectContained(KEY0));

        fileSystemCache.putIntoCache(KEY0, VALUE0);
        assertTrue(fileSystemCache.isObjectContained(KEY0));
    }

    @Test
    public void testHasFreeSpace() {
        fileSystemCache = new FileSystemCache<>(4);

        fileSystemCache.putIntoCache(KEY0, VALUE0);
        fileSystemCache.putIntoCache(KEY1, VALUE1);
        fileSystemCache.putIntoCache(KEY2, VALUE2);
        assertTrue(fileSystemCache.hasFreeSpace());

        fileSystemCache.putIntoCache(KEY3, VALUE3);
        assertFalse(fileSystemCache.hasFreeSpace());
    }

    @Test
    public void testClearCache() {
        fileSystemCache.putIntoCache(KEY0, VALUE0);
        fileSystemCache.putIntoCache(KEY1, VALUE1);
        assertEquals(2, fileSystemCache.getCacheSize());

        fileSystemCache.clearCache();
        assertEquals(0, fileSystemCache.getCacheSize());
    }
}
