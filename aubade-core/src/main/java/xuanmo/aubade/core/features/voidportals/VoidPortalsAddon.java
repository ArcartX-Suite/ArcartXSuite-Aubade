package xuanmo.aubade.core.features.voidportals;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.AbstractExtensionAddon;

/**
 * 虚空传送门扩展组件。
 * 玩家掉入虚空（Y < 0）时自动传送到指定世界（默认下界）。
 */
public class VoidPortalsAddon extends AbstractExtensionAddon implements Listener {

  private boolean enabled = true;
  private String targetWorld = "world_nether";
  private double targetX = 0;
  private double targetY = 128;
  private double targetZ = 0;

  public VoidPortalsAddon(AubadeCore plugin) {
    super(plugin, AddonDescriptor.builder("voidportals")
        .name("虚空传送")
        .version("1.0.0")
        .mainClass(VoidPortalsAddon.class.getName())
        .build());
  }

  @Override
  public String getExtensionId() {
    return "voidportals";
  }

  @Override
  public String getFriendlyName() {
    return "虚空传送";
  }

  @Override
  public void onEnable() {
    super.onEnable();
    Bukkit.getPluginManager().registerEvents(this, javaPlugin());
    plugin.getLogger().info("[VoidPortals] 虚空传送扩展已启用。");
  }

  @Override
  public void onDisable() {
    super.onDisable();
    PlayerMoveEvent.getHandlerList().unregister(this);
    plugin.getLogger().info("[VoidPortals] 虚空传送扩展已禁用。");
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    if (!enabled) {
      return;
    }
    Player player = event.getPlayer();
    if (event.getTo().getY() >= 0) {
      return;
    }

    // 防止玩家死亡
    event.setCancelled(true);

    World world = Bukkit.getWorld(targetWorld);
    if (world == null) {
      plugin.getLogger().warning("[VoidPortals] 目标世界 " + targetWorld + " 不存在，传送到主世界。");
      world = Bukkit.getWorlds().get(0);
    }

    Location target = new Location(world, targetX, targetY, targetZ);
    player.teleport(target);
    player.sendMessage("§a你掉入了虚空，已安全传送到 " + world.getName() + "。");
    plugin.getLogger().info("[VoidPortals] 玩家 " + player.getName() + " 虚空传送至 " + world.getName());
  }

  public void setTarget(String worldName, double x, double y, double z) {
    this.targetWorld = worldName;
    this.targetX = x;
    this.targetY = y;
    this.targetZ = z;
  }
}
