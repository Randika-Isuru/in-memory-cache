package com.wiley.cache.strategies;

public class LeastFrequentlyUsedCacheStrategy<K> extends CacheStrategy<K> {
  @Override
  public void putObject(K key) {
    long frequency = 1;
    if (isObjectContained(key)) {
      frequency = getRepository().get(key) + 1;
    }
    getRepository().put(key, frequency);
  }

  @Override
  public K getKeyToReplace() {
    refreshSortedRepository();
    return getSortedRepository().firstKey();
  }
}
