package xuanmo.aubade.core.listener;

import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.island.IslandProtectionManager;

public class IslandBlockListener implements Listener {

  private final AubadeCore core;
  private final IslandProtectionManager protectionManager;

  public IslandBlockListener(AubadeCore core, IslandProtectionManager protectionManager) {
    this.core = core;
    this.protectionManager = protectionManager;
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onBlockBreak(BlockBreakEvent event) {
    Player player = event.getPlayer();
    if (!protectionManager.canBreak(player, event.getBlock().getLocation())) {
      event.setCancelled(true);
      player.sendMessage("§c你没有权限在此破坏方块。");
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onBlockPlace(BlockPlaceEvent event) {
    Player player = event.getPlayer();
    if (!protectionManager.canPlace(player, event.getBlock().getLocation())) {
      event.setCancelled(true);
      player.sendMessage("§c你没有权限在此放置方块。");
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onBucketEmpty(PlayerBucketEmptyEvent event) {
    Player player = event.getPlayer();
    if (!protectionManager.canPlace(player, event.getBlock().getLocation())) {
      event.setCancelled(true);
      player.sendMessage("§c你没有权限在此使用桶。");
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onBucketFill(PlayerBucketFillEvent event) {
    Player player = event.getPlayer();
    if (!protectionManager.canBreak(player, event.getBlock().getLocation())) {
      event.setCancelled(true);
      player.sendMessage("§c你没有权限在此使用桶。");
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onHangingBreak(HangingBreakByEntityEvent event) {
    if (!(event.getRemover() instanceof Player player)) {
      return;
    }
    Hanging hanging = event.getEntity();
    if (!protectionManager.canBreak(player, hanging.getLocation())) {
      event.setCancelled(true);
    }
  }
}
