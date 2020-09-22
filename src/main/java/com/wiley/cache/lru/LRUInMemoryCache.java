package com.wiley.cache.lru;

import java.util.ArrayList;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.LRUMap;

public class LRUInMemoryCache<K, T> {
    private long timeToLive;
    private LRUMap cacheMapLRU;

    protected class LRUCacheObject {
        public long lastAccessed = System.currentTimeMillis();
        public T value;

        protected LRUCacheObject(T value) {
            this.value = value;
        }
    }


    public LRUInMemoryCache(long timeToLive, final long timerInterval, int maxItems) {
        this.timeToLive = timeToLive * 1000;

        cacheMapLRU = new LRUMap(maxItems);

        if (this.timeToLive > 0 && timerInterval > 0) {

            Thread t = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(timerInterval * 1000);
                        } catch (InterruptedException ex) {
                        }
                        cleanup();
                    }
                }
            });

            t.setDaemon(true);
            t.start();
        }
    }


    public void put(K key, T value) {
        synchronized (cacheMapLRU) {
            cacheMapLRU.put(key, new LRUCacheObject(value));
        }
    }

    @SuppressWarnings("unchecked")
    public T get(K key) {
        synchronized (cacheMapLRU) {
            LRUCacheObject c = (LRUCacheObject) cacheMapLRU.get(key);

            if (c == null)
                return null;
            else {
                c.lastAccessed = System.currentTimeMillis();
                return c.value;
            }
        }
    }

    public void remove(K key) {
        synchronized (cacheMapLRU) {
            cacheMapLRU.remove(key);
        }
    }

    public int size() {
        synchronized (cacheMapLRU) {
            return cacheMapLRU.size();
        }
    }

    @SuppressWarnings("unchecked")
    public void cleanup() {

        long now = System.currentTimeMillis();
        ArrayList<K> deleteKey = null;

        synchronized (cacheMapLRU) {
            MapIterator itr = cacheMapLRU.mapIterator();

            deleteKey = new ArrayList<K>((cacheMapLRU.size() / 2) + 1);
            K key = null;
            LRUCacheObject c = null;

            while (itr.hasNext()) {
                key = (K) itr.next();
                c = (LRUCacheObject) itr.getValue();

                if (c != null && (now > (timeToLive + c.lastAccessed))) {
                    deleteKey.add(key);
                }
            }
        }

        for (K key : deleteKey) {
            synchronized (cacheMapLRU) {
                cacheMapLRU.remove(key);
            }

            Thread.yield();
        }
    }

}
