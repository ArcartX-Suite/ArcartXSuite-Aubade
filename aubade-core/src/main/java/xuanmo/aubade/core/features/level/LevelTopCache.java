package xuanmo.aubade.core.features.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import xuanmo.arcartxsuite.api.aubade.island.Island;

/**
 * 岛屿等级排行榜缓存。
 * 定期刷新，提供 Top N 查询和玩家自身排名。
 */
public class LevelTopCache {

  private final Logger logger;
  private final Map<UUID, Long> levelMap = new ConcurrentHashMap<>();
  private volatile List<TopEntry> topList = Collections.emptyList();
  private volatile long lastRefresh = 0;
  private static final long REFRESH_INTERVAL_MS = TimeUnit.MINUTES.toMillis(5);
  private static final int DEFAULT_TOP_SIZE = 10;

  public LevelTopCache(Logger logger) {
    this.logger = logger;
  }

  /**
   * 更新单个岛屿的等级数据。
   */
  public void update(Island island) {
    if (island != null) {
      levelMap.put(island.getUniqueId(), island.getLevel());
    }
  }

  /**
   * 批量更新所有岛屿等级。
   */
  public void updateAll(Map<UUID, Long> levels) {
    levelMap.putAll(levels);
    refreshCache();
  }

  /**
   * 获取 Top N 排行榜。
   */
  public List<TopEntry> getTop(int n) {
    refreshIfNeeded();
    if (n >= topList.size()) {
      return new ArrayList<>(topList);
    }
    return new ArrayList<>(topList.subList(0, n));
  }

  /**
   * 获取默认 Top 10。
   */
  public List<TopEntry> getTop() {
    return getTop(DEFAULT_TOP_SIZE);
  }

  /**
   * 获取指定岛屿的排名（1-based）。
   */
  public int getRank(UUID islandId) {
    refreshIfNeeded();
    for (int i = 0; i < topList.size(); i++) {
      if (topList.get(i).islandId().equals(islandId)) {
        return i + 1;
      }
    }
    return -1;
  }

  private void refreshIfNeeded() {
    if (System.currentTimeMillis() - lastRefresh > REFRESH_INTERVAL_MS) {
      refreshCache();
    }
  }

  private synchronized void refreshCache() {
    long now = System.currentTimeMillis();
    if (now - lastRefresh < REFRESH_INTERVAL_MS && !topList.isEmpty()) {
      return; // 双重检查
    }
    List<TopEntry> sorted = new ArrayList<>();
    for (Map.Entry<UUID, Long> entry : levelMap.entrySet()) {
      sorted.add(new TopEntry(entry.getKey(), entry.getValue()));
    }
    sorted.sort(Comparator.comparingLong(TopEntry::level).reversed());
    this.topList = Collections.unmodifiableList(sorted);
    this.lastRefresh = now;
    logger.info("[Level] 排行榜缓存已刷新，共 " + sorted.size() + " 条记录。");
  }

  public record TopEntry(UUID islandId, long level) {
  }
}

