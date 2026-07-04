package xuanmo.aubade.api.world;

import org.bukkit.World;

/**
 * 世界管理接口。
 */
public interface WorldManager {

  /**
   * 创建游戏世界。
   *
   * @param settings 世界配置
   * @return 创建的 Bukkit World
   */
  World createWorld(WorldSettings settings);
}
