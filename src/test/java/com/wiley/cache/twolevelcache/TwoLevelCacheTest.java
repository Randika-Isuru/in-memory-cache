package com.wiley.cache.twolevelcache;

import com.wiley.cache.strategies.PolicyType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TwoLevelCacheTest {
    private static final String KEY0 = "key0";
    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final String KEY3 = "key3";
    private static final Integer VALUE0 = 0;
    private static final Integer VALUE1 = 1;
    private static final Integer VALUE2 = 2;

    private TwoLevelCache<String, Integer> twoLevelCache;

    @Before
    public void setTwoLevelCache() {
        twoLevelCache = new TwoLevelCache<>(1, 1);
    }

    @After
    public void clearCache() {
        twoLevelCache.clearCache();
    }

    @Test
    public void testPutGetRemoveObject() {
        twoLevelCache.putIntoCache(KEY0, VALUE0);
        assertEquals(VALUE0, twoLevelCache.getFromCache(KEY0));
        assertEquals(1, twoLevelCache.getCacheSize());

        twoLevelCache.deleteFromCache(KEY0);
        assertNull(twoLevelCache.getFromCache(KEY0));
    }

    @Test
    public void testDeleteObjectFromFirstLevelCache() {
        twoLevelCache.putIntoCache(KEY0, VALUE0);
        twoLevelCache.putIntoCache(KEY1, VALUE1);

        assertEquals(VALUE0, twoLevelCache.getFirstLevelCache().getFromCache(KEY0));
        assertEquals(VALUE1, twoLevelCache.getSecondLevelCache().getFromCache(KEY1));

        twoLevelCache.deleteFromCache(KEY0);

        assertNull(twoLevelCache.getFirstLevelCache().getFromCache(KEY0));
        assertEquals(VALUE1, twoLevelCache.getSecondLevelCache().getFromCache(KEY1));
    }

    @Test
    public void testDeleteObjectFromSecondLevelCache() {
        twoLevelCache.putIntoCache(KEY0, VALUE0);
        twoLevelCache.putIntoCache(KEY1, VALUE1);

        assertEquals(VALUE0, twoLevelCache.getFirstLevelCache().getFromCache(KEY0));
        assertEquals(VALUE1, twoLevelCache.getSecondLevelCache().getFromCache(KEY1));

        twoLevelCache.deleteFromCache(KEY1);

        assertEquals(VALUE0, twoLevelCache.getFirstLevelCache().getFromCache(KEY0));
        assertNull(twoLevelCache.getSecondLevelCache().getFromCache(KEY1));
    }

    @Test
    public void testGetObjectFailedWhenObjectIsNotContained() {
        twoLevelCache.putIntoCache(KEY0, VALUE0);

        assertEquals(VALUE0, twoLevelCache.getFromCache(KEY0));
        assertNull(twoLevelCache.getFromCache(KEY3));
    }

    @Test
    public void testDeleteContainedObjectFromSecondLevelCacheWhenFirstLevelCacheHasEmptySpace() {
        assertTrue(twoLevelCache.getFirstLevelCache().hasFreeSpace());

        twoLevelCache.getSecondLevelCache().putIntoCache(KEY0, VALUE0);
        assertEquals(VALUE0, twoLevelCache.getSecondLevelCache().getFromCache(KEY0));

        twoLevelCache.putIntoCache(KEY0, VALUE0);

        assertEquals(VALUE0, twoLevelCache.getFirstLevelCache().getFromCache(KEY0));
        assertFalse(twoLevelCache.getSecondLevelCache().isObjectContained(KEY0));
    }

    @Test
    public void testPutObjectIntoCacheWhenFirstLevelCacheHasEmptySpace() {
        assertTrue(twoLevelCache.getFirstLevelCache().hasFreeSpace());

        twoLevelCache.putIntoCache(KEY0, VALUE0);

        assertEquals(VALUE0, twoLevelCache.getFromCache(KEY0));
        assertEquals(VALUE0, twoLevelCache.getFirstLevelCache().getFromCache(KEY0));
        assertFalse(twoLevelCache.getSecondLevelCache().isObjectContained(KEY0));
    }

    @Test
    public void testPutObjectIntoCacheWhenObjectIsContainedInFirstLevelCache() {
        twoLevelCache.putIntoCache(KEY0, VALUE0);
        assertEquals(VALUE0, twoLevelCache.getFromCache(KEY0));
        assertEquals(VALUE0, twoLevelCache.getFirstLevelCache().getFromCache(KEY0));
        assertEquals(1, twoLevelCache.getFirstLevelCache().getCacheSize());

        twoLevelCache.putIntoCache(KEY0, VALUE1);                 // Putting an object with the same key but different value

        assertEquals(VALUE1, twoLevelCache.getFromCache(KEY0));
        assertEquals(VALUE1, twoLevelCache.getFirstLevelCache().getFromCache(KEY0));
        assertEquals(1, twoLevelCache.getFirstLevelCache().getCacheSize());
    }

    @Test
    public void testPutObjectIntoCacheWhenSecondLevelCacheHasEmptySpace() {
        twoLevelCache.putIntoCache(KEY0, VALUE0);
        assertFalse(twoLevelCache.getFirstLevelCache().hasFreeSpace());
        assertTrue(twoLevelCache.getSecondLevelCache().hasFreeSpace());

        twoLevelCache.putIntoCache(KEY1, VALUE1);

        assertEquals(VALUE1, twoLevelCache.getFromCache(KEY1));
        assertEquals(VALUE1, twoLevelCache.getSecondLevelCache().getFromCache(KEY1));
    }

    @Test
    public void testPutObjectIntoCacheWhenObjectIsContainedInSecondLevelCache() {
        twoLevelCache.putIntoCache(KEY0, VALUE0);
        assertFalse(twoLevelCache.getFirstLevelCache().hasFreeSpace());

        twoLevelCache.putIntoCache(KEY1, VALUE1);

        assertEquals(VALUE1, twoLevelCache.getFromCache(KEY1));
        assertEquals(VALUE1, twoLevelCache.getSecondLevelCache().getFromCache(KEY1));
        assertEquals(1, twoLevelCache.getSecondLevelCache().getCacheSize());

        twoLevelCache.putIntoCache(KEY1, VALUE2);                 // Putting an object with the same key but different value

        assertEquals(VALUE2, twoLevelCache.getFromCache(KEY1));
        assertEquals(VALUE2, twoLevelCache.getSecondLevelCache().getFromCache(KEY1));
        assertEquals(1, twoLevelCache.getSecondLevelCache().getCacheSize());
    }

    @Test
    public void testPutObjectIntoCacheAndReplaceCachedObject() {
        twoLevelCache.putIntoCache(KEY0, VALUE0);
        twoLevelCache.putIntoCache(KEY1, VALUE1);

        assertFalse(twoLevelCache.hasFreeSpace());
        assertFalse(twoLevelCache.getCachePolicy().isObjectContained(KEY2));

        twoLevelCache.putIntoCache(KEY2, VALUE2);    // Replacing a cached object and putting a new object into vacant space

        assertEquals(VALUE2, twoLevelCache.getFromCache(KEY2));
        assertTrue(twoLevelCache.getCachePolicy().isObjectContained(KEY2));
        assertTrue(twoLevelCache.getFirstLevelCache().isObjectContained(KEY2));
        assertFalse(twoLevelCache.getSecondLevelCache().isObjectContained(KEY2));
    }

    @Test
    public void testGetCacheSize() {
        twoLevelCache.putIntoCache(KEY0, VALUE0);
        assertEquals(1, twoLevelCache.getCacheSize());

        twoLevelCache.putIntoCache(KEY1, VALUE1);
        assertEquals(2, twoLevelCache.getCacheSize());
    }

    @Test
    public void testIsObjectContained() {
        assertFalse(twoLevelCache.isObjectContained(KEY0));

        twoLevelCache.putIntoCache(KEY0, VALUE0);
        assertTrue(twoLevelCache.isObjectContained(KEY0));
    }

    @Test
    public void testHasFreeSpace() {
        assertFalse(twoLevelCache.isObjectContained(KEY0));
        twoLevelCache.putIntoCache(KEY0, VALUE0);
        assertTrue(twoLevelCache.hasFreeSpace());

        twoLevelCache.putIntoCache(KEY1, VALUE1);
        assertFalse(twoLevelCache.hasFreeSpace());
    }

    @Test
    public void testClearCache() {
        twoLevelCache.putIntoCache(KEY0, VALUE0);
        twoLevelCache.putIntoCache(KEY1, VALUE1);

        assertEquals(2, twoLevelCache.getCacheSize());
        assertTrue(twoLevelCache.getCachePolicy().isObjectContained(KEY0));
        assertTrue(twoLevelCache.getCachePolicy().isObjectContained(KEY1));

        twoLevelCache.clearCache();

        assertEquals(0, twoLevelCache.getCacheSize());
        assertFalse(twoLevelCache.getCachePolicy().isObjectContained(KEY0));
        assertFalse(twoLevelCache.getCachePolicy().isObjectContained(KEY1));
    }

    @Test
    public void testUseLeastRecentlyUsedCachePolicy() {
        twoLevelCache = new TwoLevelCache<>(1, 1, PolicyType.LEAST_RECENTLY_USED);

        twoLevelCache.putIntoCache(KEY0, VALUE0);
        assertEquals(VALUE0, twoLevelCache.getFromCache(KEY0));
        assertEquals(VALUE0, twoLevelCache.getFirstLevelCache().getFromCache(KEY0));
        assertFalse(twoLevelCache.getSecondLevelCache().isObjectContained(KEY0));
    }
}