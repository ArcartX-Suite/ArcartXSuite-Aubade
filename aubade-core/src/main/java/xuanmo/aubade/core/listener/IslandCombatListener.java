package xuanmo.aubade.core.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.island.IslandProtectionManager;

public class IslandCombatListener implements Listener {

  private final AubadeCore core;
  private final IslandProtectionManager protectionManager;

  public IslandCombatListener(AubadeCore core, IslandProtectionManager protectionManager) {
    this.core = core;
    this.protectionManager = protectionManager;
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onEntityDamage(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player attacker)) {
      return;
    }
    if (!protectionManager.canPvp(attacker, event.getEntity().getLocation())) {
      event.setCancelled(true);
      attacker.sendMessage("§c此岛屿禁止 PvP。");
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onEntityExplode(EntityExplodeEvent event) {
    protectionManager.removeProtectedExplosionBlocks(event.blockList());
  }
}
