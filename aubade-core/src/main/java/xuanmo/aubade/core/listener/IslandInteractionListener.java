package xuanmo.aubade.core.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.island.IslandProtectionManager;

public class IslandInteractionListener implements Listener {

  private final AubadeCore core;
  private final IslandProtectionManager protectionManager;

  public IslandInteractionListener(AubadeCore core, IslandProtectionManager protectionManager) {
    this.core = core;
    this.protectionManager = protectionManager;
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (!event.hasBlock()) {
      return;
    }
    Player player = event.getPlayer();
    if (!protectionManager.canInteract(player, event.getClickedBlock().getLocation())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    Player player = event.getPlayer();
    if (!protectionManager.canInteract(player, event.getRightClicked().getLocation())) {
      event.setCancelled(true);
    }
  }
}
