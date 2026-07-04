package xuanmo.aubade.api.capability;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;
import xuanmo.aubade.api.island.Island;

/**
 * 岛屿信息查询能力接口。
 * 供 AXS 其他模块反向调用，获取岛屿基本信息。
 */
public interface IslandQueryable {

  /**
   * 获取玩家所属岛屿。
   */
  Optional<Island> getPlayerIsland(UUID player);

  /**
   * 获取玩家当前所在位置的岛屿。
   */
  Optional<Island> getIslandAt(Location location);

  /**
   * 获取所有岛屿列表（建议分页或缓存）。
   */
  List<Island> getAllIslands();

  /**
   * 获取岛屿数量。
   */
  int getIslandCount();
}
