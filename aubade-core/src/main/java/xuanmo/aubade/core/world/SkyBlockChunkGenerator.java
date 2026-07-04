package xuanmo.aubade.core.world;

import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

/**
 * 经典空岛区块生成器。
 * 生成虚空世界，仅在出生平台附近保留基岩和泥土。
 */
public class SkyBlockChunkGenerator extends ChunkGenerator {

  @Override
  public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
    // 虚空世界：不生成任何地形噪声
  }

  @Override
  public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
    // 虚空世界：不生成地表
  }

  @Override
  public void generateBedrock(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
    // 虚空世界：不生成基岩层
  }

  @Override
  public void generateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
    // 虚空世界：不生成洞穴
  }

  @Override
  public Location getFixedSpawnLocation(@NotNull World world, @NotNull Random random) {
    // 默认出生点
    return new Location(world, 0.5, 128, 0.5);
  }
}

