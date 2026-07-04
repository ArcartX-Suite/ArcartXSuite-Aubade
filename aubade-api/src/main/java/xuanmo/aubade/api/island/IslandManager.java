package xuanmo.aubade.api.island;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * 岛屿管理接口。
 * 负责岛屿的创建、删除、查询与缓存。
 */
public interface IslandManager {

  /**
   * 创建新岛屿。
   *
   * @param player   岛主
   * @param gameMode 游戏模式ID
   * @return 创建的岛屿
   */
  Island createIsland(Player player, String gameMode);

  /**
   * 删除岛屿。
   *
   * @param island 要删除的岛屿
   * @return 是否成功删除
   */
  boolean deleteIsland(Island island);

  /**
   * 按 UUID 查询岛屿。
   *
   * @param uniqueId 岛屿UUID
   * @return 岛屿Optional
   */
  Optional<Island> getIslandById(UUID uniqueId);

  /**
   * 按所有者查询岛屿。
   *
   * @param owner 所有者UUID
   * @return 岛屿Optional
   */
  Optional<Island> getIslandByOwner(UUID owner);

  /**
   * 按位置查询岛屿。
   *
   * @param location 位置
   * @return 岛屿Optional
   */
  Optional<Island> getIslandAt(Location location);

  /**
   * 获取所有岛屿数量。
   */
  int getIslandCount();

  /**
   * 获取指定世界的所有岛屿。
   *
   * @param worldName 世界名称
   * @return 岛屿列表
   */
  List<Island> getIslandsInWorld(String worldName);

  /**
   * 保存岛屿数据。
   *
   * @param island 岛屿
   */
  void saveIsland(Island island);

  /**
   * 邀请玩家加入岛屿。
   *
   * @param island 岛屿
   * @param player 被邀请的玩家
   * @return 是否成功
   */
  boolean invitePlayer(Island island, Player player);

  /**
   * 接受邀请。
   *
   * @param player 玩家
   * @return 是否成功
   */
  boolean acceptInvite(Player player);

  /**
   * 踢出成员。
   *
   * @param island 岛屿
   * @param player 被踢出的玩家UUID
   * @return 是否成功
   */
  boolean kickMember(Island island, UUID player);
}
