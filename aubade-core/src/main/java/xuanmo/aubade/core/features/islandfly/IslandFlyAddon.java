package xuanmo.aubade.core.features.islandfly;

import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.arcartxsuite.api.aubade.island.IslandPermission;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.AbstractExtensionAddon;

/**
 * 岛屿飞行扩展组件。
 * 玩家进入自己的岛屿时自动允许飞行（若配置开启），离开时自动取消。
 */
public class IslandFlyAddon extends AbstractExtensionAddon implements Listener {

  private boolean enabledByDefault = true;

  public IslandFlyAddon(AubadeCore plugin) {
    super(plugin, AddonDescriptor.builder("island_fly")
        .name("岛屿飞行")
        .version("1.0.0")
        .mainClass(IslandFlyAddon.class.getName())
        .build());
  }

  @Override
  public String getExtensionId() {
    return "island_fly";
  }

  @Override
  public String getFriendlyName() {
    return "岛屿飞行";
  }

  @Override
  public void onEnable() {
    super.onEnable();
    Bukkit.getPluginManager().registerEvents(this, javaPlugin());
    plugin.getLogger().info("[IslandFly] 岛屿飞行扩展已启用。");
  }

  @Override
  public void onDisable() {
    super.onDisable();
    PlayerMoveEvent.getHandlerList().unregister(this);
    plugin.getLogger().info("[IslandFly] 岛屿飞行扩展已禁用。");
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    if (!enabledByDefault) {
      return;
    }
    // 仅检测跨区块移动，减少开销
    if (event.getFrom().getBlockX() == event.getTo().getBlockX()
        && event.getFrom().getBlockY() == event.getTo().getBlockY()
        && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
      return;
    }

    Player player = event.getPlayer();
    var islandOpt = getIslandManager().getIslandAt(event.getTo());

    if (islandOpt.isPresent()) {
      Island island = islandOpt.get();
      boolean canFly = island.hasPermission(player.getUniqueId(), IslandPermission.FLY);
      if (canFly && !player.getAllowFlight()) {
        player.setAllowFlight(true);
      } else if (!canFly && player.getAllowFlight() && !player.hasPermission("aubade.bypass.fly")) {
        player.setAllowFlight(false);
        player.setFlying(false);
      }
    } else {
      // 离开所有岛屿，取消飞行（除非是创造/旁观）
      if (player.getAllowFlight() && !player.hasPermission("aubade.bypass.fly")
          && player.getGameMode() != org.bukkit.GameMode.CREATIVE
          && player.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
        player.setAllowFlight(false);
        player.setFlying(false);
      }
    }
  }
}
