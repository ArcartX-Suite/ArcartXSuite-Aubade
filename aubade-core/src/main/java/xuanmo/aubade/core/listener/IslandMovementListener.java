package xuanmo.aubade.core.listener;

import java.util.Objects;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.island.IslandProtectionManager;
import xuanmo.arcartxsuite.api.aubade.island.Island;

public class IslandMovementListener implements Listener {

  private final AubadeCore core;
  private final IslandProtectionManager protectionManager;

  public IslandMovementListener(AubadeCore core, IslandProtectionManager protectionManager) {
    this.core = core;
    this.protectionManager = protectionManager;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerMove(PlayerMoveEvent event) {
    if (event.getTo() == null) {
      return;
    }
    Optional<Island> fromIsland = core.getIslandManager().getIslandAt(event.getFrom());
    Optional<Island> toIsland = core.getIslandManager().getIslandAt(event.getTo());
    if (Objects.equals(fromIsland.map(Island::getUniqueId).orElse(null), toIsland.map(Island::getUniqueId).orElse(null))) {
      return;
    }
    if (toIsland.isEmpty()) {
      return;
    }
    Player player = event.getPlayer();
    if (!protectionManager.canInteract(player, event.getTo())) {
      player.sendMessage("§e你已进入岛屿保护区域。");
    }
  }
}
