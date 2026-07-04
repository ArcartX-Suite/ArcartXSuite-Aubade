package xuanmo.aubade.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LruCacheTest {

  @Test
  void evictsLeastRecentlyUsedEntry() {
    LruCache<String, Integer> cache = new LruCache<>(2);
    cache.put("a", 1);
    cache.put("b", 2);

    assertEquals(Integer.valueOf(1), cache.get("a"));
    cache.put("c", 3);

    assertTrue(cache.containsKey("a"));
    assertFalse(cache.containsKey("b"));
    assertTrue(cache.containsKey("c"));
    assertEquals(2, cache.size());
  }

  @Test
  void tracksHitsAndMisses() {
    LruCache<String, Integer> cache = new LruCache<>(2);
    cache.put("a", 1);

    assertEquals(Integer.valueOf(1), cache.get("a"));
    assertEquals(null, cache.get("missing"));
    assertEquals(1L, cache.getHitCount());
    assertEquals(1L, cache.getMissCount());
    assertEquals(0.5d, cache.hitRate());
  }
}
