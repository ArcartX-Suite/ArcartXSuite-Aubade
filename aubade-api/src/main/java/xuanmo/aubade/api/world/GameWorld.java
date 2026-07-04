package xuanmo.aubade.api.world;

import org.bukkit.World;

/**
 * 游戏世界接口。
 * 封装一个游戏世界及其关联维度（Nether、End）。
 */
public interface GameWorld {

  /**
   * Overworld 实例。
   */
  World getOverworld();

  /**
   * Nether 实例（可能为 null）。
   */
  World getNether();

  /**
   * End 实例（可能为 null）。
   */
  World getEnd();

  /**
   * 世界配置。
   */
  WorldSettings getSettings();

  /**
   * 是否已加载。
   */
  boolean isLoaded();
}
