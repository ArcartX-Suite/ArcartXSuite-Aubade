package xuanmo.aubade.core.island;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.arcartxsuite.api.aubade.island.IslandPermission;

/**
 * 岛屿保护管理器。
 * 提供事件判定能力，由 listener 层负责接线。
 */
public class IslandProtectionManager {

  private final IslandManagerImpl islandManager;

  public IslandProtectionManager(IslandManagerImpl islandManager) {
    this.islandManager = islandManager;
  }

  public boolean canBreak(Player player, Location location) {
    return canAct(player, location, IslandPermission.BREAK);
  }

  public boolean canPlace(Player player, Location location) {
    return canAct(player, location, IslandPermission.PLACE);
  }

  public boolean canInteract(Player player, Location location) {
    return canAct(player, location, IslandPermission.INTERACT);
  }

  public boolean canPvp(Player player, Location location) {
    Optional<Island> opt = islandManager.getIslandAt(location);
    if (opt.isEmpty()) {
      return true;
    }
    Island island = opt.get();
    return island.hasPermission(player.getUniqueId(), IslandPermission.PVP);
  }

  public boolean isProtected(Location location) {
    return islandManager.getIslandAt(location).isPresent();
  }

  public void removeProtectedExplosionBlocks(List<Block> blocks) {
    blocks.removeIf(block -> islandManager.getIslandAt(block.getLocation()).isPresent());
  }

  private boolean canAct(Player player, Location location, IslandPermission permission) {
    Optional<Island> opt = islandManager.getIslandAt(location);
    if (opt.isEmpty()) {
      return true;
    }
    Island island = opt.get();
    UUID uuid = player.getUniqueId();
    return island.hasPermission(uuid, permission);
  }
}
