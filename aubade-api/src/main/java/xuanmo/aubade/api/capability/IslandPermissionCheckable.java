package xuanmo.aubade.api.capability;

import java.util.UUID;
import xuanmo.aubade.api.island.IslandPermission;

/**
 * 岛屿权限检查能力接口。
 * 供 AXS regions/market 等模块检查玩家在岛屿内的权限。
 */
public interface IslandPermissionCheckable {

  /**
   * 检查玩家是否具有指定权限。
   */
  boolean hasPermission(UUID player, IslandPermission permission);

  /**
   * 检查玩家是否可以访问目标岛屿。
   */
  boolean canAccess(UUID player, UUID islandId);

  /**
   * 检查玩家是否为目标岛屿成员（含岛主）。
   */
  boolean isMember(UUID player, UUID islandId);

  /**
   * 检查玩家是否为目标岛屿岛主。
   */
  boolean isOwner(UUID player, UUID islandId);
}
