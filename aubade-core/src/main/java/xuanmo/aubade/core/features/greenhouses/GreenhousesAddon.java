package xuanmo.aubade.core.features.greenhouses;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.AbstractExtensionAddon;

/**
 * 温室扩展组件。
 * 扫描岛屿内的玻璃结构，在其内部模拟指定生物群系（通过定时设置生物群系）。
 */
public class GreenhousesAddon extends AbstractExtensionAddon {

  private static final Set<Material> GLASS_BLOCKS = EnumSet.of(
      Material.GLASS, Material.WHITE_STAINED_GLASS, Material.ORANGE_STAINED_GLASS,
      Material.MAGENTA_STAINED_GLASS, Material.LIGHT_BLUE_STAINED_GLASS,
      Material.YELLOW_STAINED_GLASS, Material.LIME_STAINED_GLASS,
      Material.PINK_STAINED_GLASS, Material.GRAY_STAINED_GLASS,
      Material.LIGHT_GRAY_STAINED_GLASS, Material.CYAN_STAINED_GLASS,
      Material.PURPLE_STAINED_GLASS, Material.BLUE_STAINED_GLASS,
      Material.BROWN_STAINED_GLASS, Material.GREEN_STAINED_GLASS,
      Material.RED_STAINED_GLASS, Material.BLACK_STAINED_GLASS
  );

  private final Map<UUID, Biome> greenhouseBiomes = new HashMap<>();
  private int taskId = -1;
  private long scanIntervalTicks = 100L; // 5 秒

  public GreenhousesAddon(AubadeCore plugin) {
    super(plugin, AddonDescriptor.builder("greenhouses")
        .name("温室")
        .version("1.0.0")
        .mainClass(GreenhousesAddon.class.getName())
        .build());
  }

  @Override
  public String getExtensionId() {
    return "greenhouses";
  }

  @Override
  public String getFriendlyName() {
    return "温室";
  }

  @Override
  public void onEnable() {
    super.onEnable();
    startScanTask();
    plugin.getLogger().info("[Greenhouses] 温室扩展已启用。");
  }

  @Override
  public void onDisable() {
    super.onDisable();
    if (taskId != -1) {
      Bukkit.getScheduler().cancelTask(taskId);
    }
    plugin.getLogger().info("[Greenhouses] 温室扩展已禁用。");
  }

  private void startScanTask() {
    taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(javaPlugin(), () -> {
      for (Island island : getIslandManager().getIslandsInWorld(null)) {
        if (island.getWorld() == null) {
          continue;
        }
        Biome targetBiome = greenhouseBiomes.get(island.getUniqueId());
        if (targetBiome == null) {
          continue;
        }
        scanAndApplyBiome(island, targetBiome);
      }
    }, scanIntervalTicks, scanIntervalTicks);
  }

  private void scanAndApplyBiome(Island island, Biome targetBiome) {
    World world = island.getWorld();
    org.bukkit.Location center = island.getCenter();
    int range = island.getProtectionRange();

    int minX = center.getBlockX() - range;
    int maxX = center.getBlockX() + range;
    int minZ = center.getBlockZ() - range;
    int maxZ = center.getBlockZ() + range;

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
            // 简单检测：如果该列顶部有玻璃覆盖，则内部设为目标生物群系
            if (hasGlassRoof(chunk, x, z, world.getMaxHeight())) {
              for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
                if (world.getBiome(wx, y, wz) != targetBiome) {
                  world.setBiome(wx, y, wz, targetBiome);
                }
              }
            }
          }
        }
      }
    }
  }

  private boolean hasGlassRoof(Chunk chunk, int x, int z, int maxHeight) {
    // 从最高处往下扫描，寻找连续玻璃层
    int glassCount = 0;
    for (int y = maxHeight - 1; y >= chunk.getWorld().getMinHeight(); y--) {
      Material type = chunk.getBlock(x, y, z).getType();
      if (GLASS_BLOCKS.contains(type)) {
        glassCount++;
        if (glassCount >= 2) {
          return true;
        }
      } else if (type.isSolid() && type != Material.AIR) {
        break;
      }
    }
    return false;
  }

  public void setGreenhouseBiome(UUID islandId, Biome biome) {
    if (biome == null) {
      greenhouseBiomes.remove(islandId);
    } else {
      greenhouseBiomes.put(islandId, biome);
    }
  }

  public Biome getGreenhouseBiome(UUID islandId) {
    return greenhouseBiomes.get(islandId);
  }
}
