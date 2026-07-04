package xuanmo.aubade.core.features.level;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import xuanmo.arcartxsuite.api.aubade.island.Island;

/**
 * 岛屿等级计算器。
 * 异步扫描岛屿保护范围内的方块，按价值表累加等级。
 */
public class LevelCalculator {

  private final BlockValues blockValues;
  private final Logger logger;
  private static final int MAX_BLOCKS = 10_000_000; // 扫描上限，防止恶意堆叠
  private static final int CHUNK_SIZE = 16;

  public LevelCalculator(BlockValues blockValues, Logger logger) {
    this.blockValues = blockValues;
    this.logger = logger;
  }

  /**
   * 异步扫描岛屿并返回等级。
   */
  public CompletableFuture<Long> scanAsync(Island island) {
    return CompletableFuture.supplyAsync(() -> scanSync(island));
  }

  /**
   * 同步扫描（应在异步线程中调用）。
   */
  public long scanSync(Island island) {
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
    int minY = world.getMinHeight();
    int maxY = world.getMaxHeight();

    long total = 0;
    long counted = 0;

    // 按 chunk 分批扫描，避免一次性加载过多区块
    int minChunkX = minX >> 4;
    int maxChunkX = maxX >> 4;
    int minChunkZ = minZ >> 4;
    int maxChunkZ = maxZ >> 4;

    for (int cx = minChunkX; cx <= maxChunkX; cx++) {
      for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
        if (!world.isChunkLoaded(cx, cz)) {
          continue; // 跳过未加载区块
        }
        Chunk chunk = world.getChunkAt(cx, cz);
        int chunkBaseX = cx << 4;
        int chunkBaseZ = cz << 4;

        for (int x = 0; x < CHUNK_SIZE; x++) {
          int worldX = chunkBaseX + x;
          if (worldX < minX || worldX > maxX) {
            continue;
          }
          for (int z = 0; z < CHUNK_SIZE; z++) {
            int worldZ = chunkBaseZ + z;
            if (worldZ < minZ || worldZ > maxZ) {
              continue;
            }
            for (int y = minY; y <= maxY; y++) {
              Block block = chunk.getBlock(x, y, z);
              Material type = block.getType();
              if (type.isAir()) {
                continue;
              }
              total += blockValues.getValue(type);
              counted++;
              if (counted >= MAX_BLOCKS) {
                logger.warning("[Level] 岛屿 " + island.getUniqueId() + " 方块数量超过上限 " + MAX_BLOCKS + "，停止扫描。");
                return total;
              }
            }
          }
        }
      }
    }

    return total;
  }
}

