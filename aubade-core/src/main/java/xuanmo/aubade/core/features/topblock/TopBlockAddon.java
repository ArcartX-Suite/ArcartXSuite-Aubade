package xuanmo.aubade.core.features.topblock;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.AbstractExtensionAddon;

/**
 * 最高方块排行扩展组件。
 * 扫描岛屿范围内最高 Y 坐标的方块，生成排行榜。
 */
public class TopBlockAddon extends AbstractExtensionAddon {

  private final Map<UUID, Integer> topYCache = new ConcurrentHashMap<>();
  private volatile long lastGlobalScan = 0;
  private static final long SCAN_INTERVAL_MS = 10 * 60 * 1000; // 10 分钟

  public TopBlockAddon(AubadeCore plugin) {
    super(plugin, AddonDescriptor.builder("topblock")
        .name("最高方块排行")
        .version("1.0.0")
        .mainClass(TopBlockAddon.class.getName())
        .build());
  }

  @Override
  public String getExtensionId() {
    return "topblock";
  }

  @Override
  public String getFriendlyName() {
    return "最高方块排行";
  }

  @Override
  public void onEnable() {
    super.onEnable();
    plugin.getLogger().info("[TopBlock] 最高方块排行扩展已启用。");
  }

  @Override
  public void onDisable() {
    super.onDisable();
    plugin.getLogger().info("[TopBlock] 最高方块排行扩展已禁用。");
  }

  /**
   * 异步扫描岛屿最高方块 Y 坐标。
   */
  public CompletableFuture<Integer> scanTopAsync(Island island) {
    return CompletableFuture.supplyAsync(() -> scanTopSync(island));
  }

  public int scanTopSync(Island island) {
    if (island == null || island.getCenter() == null || island.getWorld() == null) {
      return 0;
    }

    World world = island.getWorld();
    org.bukkit.Location center = island.getCenter();
    int range = island.getProtectionRange();

    int minX = center.getBlockX() - range;
    int maxX = center.getBlockX() + range;
    int minZ = center.getBlockZ() - range;
    int maxZ = center.getBlockZ() + range;

    int maxY = world.getMinHeight();

    int chunkMinX = minX >> 4;
    int chunkMaxX = maxX >> 4;
    int chunkMinZ = minZ >> 4;
    int chunkMaxZ = maxZ >> 4;

    for (int cx = chunkMinX; cx <= chunkMaxX; cx++) {
      for (int cz = chunkMinZ; cz <= chunkMaxZ; cz++) {
        if (!world.isChunkLoaded(cx, cz)) {
          continue;
        }
        Chunk chunk = world.getChunkAt(cx, cz);
        int baseX = cx << 4;
        int baseZ = cz << 4;
        for (int x = 0; x < 16; x++) {
          int wx = baseX + x;
          if (wx < minX || wx > maxX) {
            continue;
          }
          for (int z = 0; z < 16; z++) {
            int wz = baseZ + z;
            if (wz < minZ || wz > maxZ) {
              continue;
            }
            // 从最高处往下找第一个非空气方块
            for (int y = world.getMaxHeight(); y >= world.getMinHeight(); y--) {
              Block block = chunk.getBlock(x, y, z);
              if (!block.getType().isAir()) {
                if (y > maxY) {
                  maxY = y;
                }
                break;
              }
            }
          }
        }
      }
    }

    topYCache.put(island.getUniqueId(), maxY);
    return maxY;
  }

  /**
   * 获取缓存的最高 Y（若缓存过期则返回 -1）。
   */
  public int getCachedTopY(UUID islandId) {
    return topYCache.getOrDefault(islandId, -1);
  }

  /**
   * 全服扫描并返回排行榜。
   */
  public List<Map.Entry<UUID, Integer>> getGlobalLeaderboard(int topN) {
    long now = System.currentTimeMillis();
    if (now - lastGlobalScan > SCAN_INTERVAL_MS) {
      // 触发异步全局扫描
      Bukkit.getScheduler().runTaskAsynchronously(javaPlugin(), () -> {
        for (Island island : getIslandManager().getIslandsInWorld(null)) {
          scanTopSync(island);
        }
        lastGlobalScan = System.currentTimeMillis();
      });
    }

    List<Map.Entry<UUID, Integer>> sorted = new java.util.ArrayList<>(topYCache.entrySet());
    sorted.sort(Map.Entry.<UUID, Integer>comparingByValue().reversed());
    return sorted.size() > topN ? sorted.subList(0, topN) : sorted;
  }
}
