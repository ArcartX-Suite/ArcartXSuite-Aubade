package xuanmo.aubade.api.player;

import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * 玩家管理接口。
 */
public interface PlayerManager {

  /**
   * 获取或加载玩家数据。
   *
   * @param player Bukkit Player
   * @return SkyPlayer
   */
  SkyPlayer getPlayer(Player player);

  /**
   * 按 UUID 获取玩家数据。
   *
   * @param uuid 玩家UUID
   * @return Optional
   */
  Optional<SkyPlayer> getPlayer(UUID uuid);

  /**
   * 保存玩家数据。
   *
   * @param player 玩家数据
   */
  void savePlayer(SkyPlayer player);

  /**
   * 卸载玩家缓存。
   *
   * @param uuid 玩家UUID
   */
  void unloadPlayer(UUID uuid);
}
