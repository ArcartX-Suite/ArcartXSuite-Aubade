package xuanmo.aubade.core.island;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.aubade.core.util.LruCache;

/**
 * 岛屿两级缓存：L1 (ConcurrentHashMap 热数据) + L2 (LRU 温数据)。
 * L1 保持全量热数据，L2 用于历史访问记录的快速回温。
 */
public class IslandCache {

  private final Map<UUID, Island> byId = new ConcurrentHashMap<>();
  private final Map<UUID, Island> byOwner = new ConcurrentHashMap<>();
  private final LruCache<UUID, Island> l2ById = new LruCache<>(256);
  private final LruCache<UUID, Island> l2ByOwner = new LruCache<>(256);

  public void put(Island island) {
    byId.put(island.getUniqueId(), island);
    byOwner.put(island.getOwner(), island);
    l2ById.put(island.getUniqueId(), island);
    l2ByOwner.put(island.getOwner(), island);
  }

  public Optional<Island> getById(UUID id) {
    Island island = byId.get(id);
    if (island != null) {
      return Optional.of(island);
    }
    island = l2ById.get(id);
    if (island != null) {
      byId.put(id, island);
      byOwner.put(island.getOwner(), island);
    }
    return Optional.ofNullable(island);
  }

  public Optional<Island> getByOwner(UUID owner) {
    Island island = byOwner.get(owner);
    if (island != null) {
      return Optional.of(island);
    }
    island = l2ByOwner.get(owner);
    if (island != null) {
      byId.put(island.getUniqueId(), island);
      byOwner.put(owner, island);
    }
    return Optional.ofNullable(island);
  }

  public void remove(UUID id) {
    Island island = byId.remove(id);
    if (island != null) {
      byOwner.remove(island.getOwner());
    }
    l2ById.remove(id);
    if (island != null) {
      l2ByOwner.remove(island.getOwner());
    }
  }

  public void removeByOwner(UUID owner) {
    Island island = byOwner.remove(owner);
    if (island != null) {
      byId.remove(island.getUniqueId());
    }
    l2ByOwner.remove(owner);
    if (island != null) {
      l2ById.remove(island.getUniqueId());
    }
  }

  public boolean contains(UUID id) {
    return byId.containsKey(id) || l2ById.containsKey(id);
  }

  public int size() {
    return byId.size();
  }

  public java.util.Collection<Island> values() {
    return byId.values();
  }

  public void clear() {
    byId.clear();
    byOwner.clear();
    l2ById.clear();
    l2ByOwner.clear();
  }

  public double getHitRate() {
    return l2ById.hitRate();
  }

  public long getHitCount() {
    return l2ById.getHitCount();
  }

  public long getMissCount() {
    return l2ById.getMissCount();
  }
}
