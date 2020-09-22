package com.wiley.cache.twolevelcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;

class MemoryCache<K extends Serializable, V extends Serializable> implements Cache<K, V> {
  private static final Logger LOG = LoggerFactory.getLogger(com.wiley.cache.twolevelcache.MemoryCache.class);

  private final Map<K, V> objectsRepository;
  private final int repositorySize;

  MemoryCache(int repositorySize) {
    this.repositorySize = repositorySize;
    this.objectsRepository = new ConcurrentHashMap<>(repositorySize);
  }

  @Override
  public V getFromCache(K key) {
    LOG.debug(format("Get an object with key '%s' from memory cache", key));
    return objectsRepository.get(key);
  }

  @Override
  public void putIntoCache(K key, V value) {
    LOG.debug(format("Put an object with key '%s' into memory cache", key));
    objectsRepository.put(key, value);
  }

  @Override
  public void deleteFromCache(K key) {
    LOG.debug(format("Delete an object with key '%s' from memory cache", key));
    objectsRepository.remove(key);
  }

  @Override
  public int getCacheSize() {
    return objectsRepository.size();
  }

  @Override
  public boolean isObjectContained(K key) {
    return objectsRepository.containsKey(key);
  }

  @Override
  public boolean hasFreeSpace() {
    return getCacheSize() < repositorySize;
  }

  @Override
  public void clearCache() {
    LOG.debug(format("Delete %d object(s) from memory cache", getCacheSize()));
    objectsRepository.clear();
  }
}
