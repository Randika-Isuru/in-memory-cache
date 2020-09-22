package com.wiley.cache.strategies;

public class LeastRecentlyUsedCacheStrategy<K> extends CacheStrategy<K> {
  @Override
  public void putObject(K key) {
    getRepository().put(key, System.nanoTime());
  }

  @Override
  public K getKeyToReplace() {
    refreshSortedRepository();
    return getSortedRepository().firstKey();
  }
}
