package com.wiley.cache.strategies;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

class MyComparator<K> implements Comparator<K>, Serializable {
  private static final long serialVersionUID = 1L;

  private final Map<K, Long> baseMap;

  MyComparator(Map<K, Long> baseMap) {
    this.baseMap = baseMap;
  }

  @Override
  public int compare(K key1, K key2) {
    long value1 = baseMap.get(key1);
    long value2 = baseMap.get(key2);
    return value1 > value2 ? 1 : -1;
  }
}
