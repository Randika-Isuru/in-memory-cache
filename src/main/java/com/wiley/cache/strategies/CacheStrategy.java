package com.wiley.cache.strategies;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class CacheStrategy<K> {
  private final Map<K, Long> repository;
  private final SortedMap<K, Long> sortedRepository;

  CacheStrategy() {
    this.repository = new HashMap<>();
    this.sortedRepository = new TreeMap<>(new MyComparator<K>(repository));
  }

  protected Map<K, Long> getRepository() {
    return repository;
  }

  protected SortedMap<K, Long> getSortedRepository() {
    return sortedRepository;
  }

  public abstract void putObject(K key);

  public abstract K getKeyToReplace();

  public boolean isObjectContained(K key) {
    return repository.containsKey(key);
  }

  public void deleteObject(K key) {
    if (isObjectContained(key)) repository.remove(key);
  }

  void refreshSortedRepository() {
    sortedRepository.clear();
    sortedRepository.putAll(repository);
  }

  public void clear() {
    repository.clear();
    sortedRepository.clear();
  }
}
