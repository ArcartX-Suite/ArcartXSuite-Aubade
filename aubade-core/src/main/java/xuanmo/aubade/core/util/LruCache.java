package xuanmo.aubade.core.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 简单的线程安全 LRU 缓存。
 * 作为 IslandCache 的 L2 层，避免引入外部依赖。
 */
public class LruCache<K, V> {

  private final LinkedHashMap<K, V> map;
  private final int maxSize;
  private volatile long hitCount;
  private volatile long missCount;

  public LruCache(int maxSize) {
    this.maxSize = maxSize;
    this.map = new LinkedHashMap<>(16, 0.75f, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > LruCache.this.maxSize;
      }
    };
  }

  public synchronized V get(K key) {
    V value = map.get(key);
    if (value != null) {
      hitCount++;
    } else {
      missCount++;
    }
    return value;
  }

  public synchronized void put(K key, V value) {
    map.put(key, value);
  }

  public synchronized V remove(K key) {
    return map.remove(key);
  }

  public synchronized boolean containsKey(K key) {
    return map.containsKey(key);
  }

  public synchronized int size() {
    return map.size();
  }

  public synchronized void clear() {
    map.clear();
    hitCount = 0;
    missCount = 0;
  }

  public synchronized double hitRate() {
    long total = hitCount + missCount;
    return total == 0 ? 0.0 : (double) hitCount / total;
  }

  public long getHitCount() {
    return hitCount;
  }

  public long getMissCount() {
    return missCount;
  }
}

