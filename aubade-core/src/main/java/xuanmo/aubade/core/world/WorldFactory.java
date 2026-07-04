package xuanmo.aubade.core.world;

import org.bukkit.World;
import xuanmo.arcartxsuite.api.aubade.world.WorldSettings;

/**
 * 世界创建工厂。
 * 负责根据 WorldSettings 生成 Overworld / Nether / End 三重维度。
 */
public class WorldFactory {

  private final WorldManagerImpl worldManager;

  public WorldFactory(WorldManagerImpl worldManager) {
    this.worldManager = worldManager;
  }

  /**
   * 创建完整的游戏世界（含可选的 Nether 和 End）。
   *
   * @param settings 世界配置
   * @return Overworld 主世界
   */
  public World createGameWorld(WorldSettings settings) {
    // Overworld
    World overworld = worldManager.createWorld(settings);

    // Nether
    if (settings.isNetherEnabled()) {
      String netherName = settings.getWorldName() + "_nether";
      org.bukkit.WorldCreator netherCreator = new org.bukkit.WorldCreator(netherName);
      netherCreator.environment(org.bukkit.World.Environment.NETHER);
      org.bukkit.Bukkit.createWorld(netherCreator);
    }

    // End
    if (settings.isEndEnabled()) {
      String endName = settings.getWorldName() + "_the_end";
      org.bukkit.WorldCreator endCreator = new org.bukkit.WorldCreator(endName);
      endCreator.environment(org.bukkit.World.Environment.THE_END);
      org.bukkit.Bukkit.createWorld(endCreator);
    }

    return overworld;
  }
}

