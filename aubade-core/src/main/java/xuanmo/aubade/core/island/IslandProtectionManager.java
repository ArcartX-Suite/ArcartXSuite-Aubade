package xuanmo.aubade.core.island;

import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.arcartxsuite.api.aubade.island.IslandPermission;

/**
 * 岛屿保护管理器。
 * 拦截方块破坏、放置、交互、实体伤害、爆炸等事件。
 */
public class IslandProtectionManager implements Listener {

  private final IslandManagerImpl islandManager;

  public IslandProtectionManager(IslandManagerImpl islandManager) {
    this.islandManager = islandManager;
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onBlockBreak(BlockBreakEvent event) {
    Player player = event.getPlayer();
    if (!canAct(player, event.getBlock().getLocation(), IslandPermission.BREAK)) {
      event.setCancelled(true);
      player.sendMessage("§c你没有权限在此破坏方块。");
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onBlockPlace(BlockPlaceEvent event) {
    Player player = event.getPlayer();
    if (!canAct(player, event.getBlock().getLocation(), IslandPermission.PLACE)) {
      event.setCancelled(true);
      player.sendMessage("§c你没有权限在此放置方块。");
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (!event.hasBlock()) {
      return;
    }
    Player player = event.getPlayer();
    if (!canAct(player, event.getClickedBlock().getLocation(), IslandPermission.INTERACT)) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    Player player = event.getPlayer();
    if (!canAct(player, event.getRightClicked().getLocation(), IslandPermission.INTERACT)) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onBucketEmpty(PlayerBucketEmptyEvent event) {
    Player player = event.getPlayer();
    if (!canAct(player, event.getBlock().getLocation(), IslandPermission.PLACE)) {
      event.setCancelled(true);
      player.sendMessage("§c你没有权限在此使用桶。");
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onBucketFill(PlayerBucketFillEvent event) {
    Player player = event.getPlayer();
    if (!canAct(player, event.getBlock().getLocation(), IslandPermission.BREAK)) {
      event.setCancelled(true);
      player.sendMessage("§c你没有权限在此使用桶。");
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onHangingBreak(HangingBreakByEntityEvent event) {
    Entity remover = event.getRemover();
    if (!(remover instanceof Player player)) {
      return;
    }
    if (!canAct(player, event.getEntity().getLocation(), IslandPermission.BREAK)) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onEntityDamage(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player attacker)) {
      return;
    }
    Location loc = event.getEntity().getLocation();
    Optional<Island> opt = islandManager.getIslandAt(loc);
    if (opt.isEmpty()) {
      return;
    }
    Island island = opt.get();
    if (!island.hasPermission(attacker.getUniqueId(), IslandPermission.PVP)) {
      event.setCancelled(true);
      attacker.sendMessage("§c此岛屿禁止 PvP。");
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onEntityExplode(EntityExplodeEvent event) {
    // 取消在岛屿保护范围内的所有爆炸
    event.blockList().removeIf(block -> islandManager.getIslandAt(block.getLocation()).isPresent());
  }

  /**
   * 检查玩家是否可以在该位置执行指定权限的操作。
   */
  private boolean canAct(Player player, Location location, IslandPermission permission) {
    Optional<Island> opt = islandManager.getIslandAt(location);
    if (opt.isEmpty()) {
      return true; // 不在任何岛屿保护范围内，允许
    }
    Island island = opt.get();
    UUID uuid = player.getUniqueId();
    return island.hasPermission(uuid, permission);
  }
}

