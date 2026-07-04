package xuanmo.aubade.api.world;

import org.bukkit.generator.ChunkGenerator;

/**
 * 区块生成器工厂。
 * 各游戏模式提供自己的生成器实例。
 */
@FunctionalInterface
public interface ChunkGeneratorFactory {

  /**
   * 创建区块生成器。
   *
   * @param worldType 世界类型字符串，如 "overworld", "nether", "end"
   * @return ChunkGenerator 实例
   */
  ChunkGenerator create(String worldType);
}
