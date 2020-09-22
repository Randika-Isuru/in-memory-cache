package com.wiley.cache.twolevelcache;

import com.wiley.cache.strategies.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

import static java.lang.String.format;

public class TwoLevelCache<K extends Serializable, V extends Serializable> implements Cache<K, V> {
  private static final Logger LOG = LoggerFactory.getLogger(com.wiley.cache.twolevelcache.TwoLevelCache.class);

  private final MemoryCache<K, V> firstLevelCache;
  private final FileSystemCache<K, V> secondLevelCache;
  private final CacheStrategy<K> cachePolicy;

  public TwoLevelCache(final int memorySize, final int fileSystemSize, final PolicyType policyType) {
    this.firstLevelCache = new MemoryCache<>(memorySize);
    this.secondLevelCache = new FileSystemCache<>(fileSystemSize);
    this.cachePolicy = getPolicyByType(policyType);
  }

  public TwoLevelCache(final int memorySize, final int fileSystemSize) {
    this.firstLevelCache = new MemoryCache<>(memorySize);
    this.secondLevelCache = new FileSystemCache<>(fileSystemSize);
    this.cachePolicy = getPolicyByType(PolicyType.LEAST_FREQUENTLY_USED);
  }

  MemoryCache<K, V> getFirstLevelCache() {
    return firstLevelCache;
  }

  FileSystemCache<K, V> getSecondLevelCache() {
    return secondLevelCache;
  }

  CacheStrategy<K> getCachePolicy() {
    return cachePolicy;
  }

  private CacheStrategy<K> getPolicyByType(PolicyType policyType) {
    if (policyType == PolicyType.LEAST_RECENTLY_USED) {
      return new LeastRecentlyUsedCacheStrategy<>();
    }
    return new LeastFrequentlyUsedCacheStrategy<>();
  }

  @Override
  public synchronized void putIntoCache(K newKey, V newValue) {
    if (firstLevelCache.isObjectContained(newKey) || firstLevelCache.hasFreeSpace()) {
      LOG.debug(format("Put an object with key '%s' to the 1st level cache", newKey));
      firstLevelCache.putIntoCache(newKey, newValue);
      if (secondLevelCache.isObjectContained(newKey)) {
        secondLevelCache.deleteFromCache(newKey);
      }
    } else if (secondLevelCache.isObjectContained(newKey) || secondLevelCache.hasFreeSpace()) {
      LOG.debug(format("Put an object with key '%s' to the 2nd level cache", newKey));
      secondLevelCache.putIntoCache(newKey, newValue);
    } else {
      replaceObject(newKey, newValue);
    }

    if (!cachePolicy.isObjectContained(newKey)) {
      LOG.debug(format("Put an object with key '%s' to cache policy repository", newKey));
      cachePolicy.putObject(newKey);
    }
  }

  private void replaceObject(K key, V value) {
    K keyToReplace = cachePolicy.getKeyToReplace();
    if (firstLevelCache.isObjectContained(keyToReplace)) {
      LOG.debug(format("Replace an object with key '%s' from 1st level cache", keyToReplace));
      firstLevelCache.deleteFromCache(keyToReplace);
      firstLevelCache.putIntoCache(key, value);
    } else if (secondLevelCache.isObjectContained(keyToReplace)) {
      LOG.debug(format("Replace an object with key '%s' from 2nd level cache", keyToReplace));
      secondLevelCache.deleteFromCache(keyToReplace);
      secondLevelCache.putIntoCache(key, value);
    }
    cachePolicy.deleteObject(keyToReplace);
  }

  @Override
  public synchronized V getFromCache(K key) {
    if (firstLevelCache.isObjectContained(key)) {
      cachePolicy.putObject(key);
      return firstLevelCache.getFromCache(key);
    } else if (secondLevelCache.isObjectContained(key)) {
      cachePolicy.putObject(key);
      return secondLevelCache.getFromCache(key);
    }
    return null;
  }

  @Override
  public synchronized void deleteFromCache(K key) {
    if (firstLevelCache.isObjectContained(key)) {
      LOG.debug(format("Delete an object with key '%s' from 1st level cache", key));
      firstLevelCache.deleteFromCache(key);
    } else if (secondLevelCache.isObjectContained(key)) {
      LOG.debug(format("Delete an object with key '%s' from 2nd level cache", key));
      secondLevelCache.deleteFromCache(key);
    }
    cachePolicy.deleteObject(key);
  }

  @Override
  public int getCacheSize() {
    return firstLevelCache.getCacheSize() + secondLevelCache.getCacheSize();
  }

  @Override
  public boolean isObjectContained(K key) {
    return firstLevelCache.isObjectContained(key) || secondLevelCache.isObjectContained(key);
  }

  @Override
  public boolean hasFreeSpace() {
    return firstLevelCache.hasFreeSpace() || secondLevelCache.hasFreeSpace();
  }

  @Override
  public void clearCache() {
    firstLevelCache.clearCache();
    secondLevelCache.clearCache();
    cachePolicy.clear();
  }
}
