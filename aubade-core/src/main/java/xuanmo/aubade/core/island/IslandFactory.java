package xuanmo.aubade.core.island;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import xuanmo.arcartxsuite.api.aubade.addon.GameModeAddon;
import xuanmo.arcartxsuite.api.aubade.island.Island;

/**
 * 岛屿创建工厂。
 * 负责组装新的 Island 实例并计算世界坐标。
 */
public class IslandFactory {

  private final IslandGrid grid;

  public IslandFactory(IslandGrid grid) {
    this.grid = grid;
  }

  /**
   * 创建新岛屿。
   *
   * @param owner    岛主 UUID
   * @param world    所属世界
   * @param gameMode 游戏模式
   * @return 新的 Island 实例（尚未持久化）
   */
  public Island create(UUID owner, World world, GameModeAddon gameMode) {
    UUID islandId = UUID.randomUUID();
    int index = grid.nextIndex();
    Location center = grid.createLocation(world, index);

    int protectionRange = gameMode.getWorldSettings().getDefaultProtectionRange();
    int range = gameMode.getWorldSettings().getMaxIslandSize();

    Island island = new Island(islandId, owner, center, protectionRange, range, world, gameMode);
    island.setName("岛屿-" + index);
    island.setDescription("");
    return island;
  }
}

