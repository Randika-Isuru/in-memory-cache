package com.wiley.cache.twolevelcache;

interface Cache<K, V> {

  void putIntoCache(K key, V value);

  V getFromCache(K key);

  void deleteFromCache(K key);

  int getCacheSize();

  boolean isObjectContained(K key);

  boolean hasFreeSpace();

  void clearCache();
}
