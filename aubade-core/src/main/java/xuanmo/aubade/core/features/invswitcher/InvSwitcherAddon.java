package xuanmo.aubade.core.features.invswitcher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.AbstractExtensionAddon;
import xuanmo.aubade.core.util.VersionAdapter;

/**
 * 世界间背包分离扩展组件。
 * 玩家切换世界时自动保存/恢复该世界对应的背包和属性。
 */
public class InvSwitcherAddon extends AbstractExtensionAddon implements Listener {

  // playerId -> worldName -> inventory snapshot
  private final Map<UUID, Map<String, InventorySnapshot>> worldInventories = new HashMap<>();

  public InvSwitcherAddon(AubadeCore core) {
    super(core, AddonDescriptor.builder("invswitcher")
        .name("背包分离")
        .version("1.0.0")
        .mainClass(InvSwitcherAddon.class.getName())
        .build());
  }

  @Override
  public String getExtensionId() {
    return "invswitcher";
  }

  @Override
  public String getFriendlyName() {
    return "背包分离";
  }

  @Override
  public void onEnable() {
    super.onEnable();
    Bukkit.getPluginManager().registerEvents(this, javaPlugin());
    core.getLogger().info("[InvSwitcher] 背包分离扩展已启用。");
  }

  @Override
  public void onDisable() {
    super.onDisable();
    PlayerChangedWorldEvent.getHandlerList().unregister(this);
    core.getLogger().info("[InvSwitcher] 背包分离扩展已禁用。");
  }

  @EventHandler
  public void onWorldChange(PlayerChangedWorldEvent event) {
    Player player = event.getPlayer();
    World fromWorld = event.getFrom();
    World toWorld = player.getWorld();

    // 不处理创造/旁观模式玩家
    if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
      return;
    }

    UUID playerId = player.getUniqueId();
    String fromName = fromWorld.getName();
    String toName = toWorld.getName();

    // 保存旧世界背包
    saveInventory(playerId, fromName, player);

    // 恢复新世界背包
    restoreInventory(playerId, toName, player);
  }

  private void saveInventory(UUID playerId, String worldName, Player player) {
    InventorySnapshot snap = new InventorySnapshot(
        player.getInventory().getContents(),
        player.getInventory().getArmorContents(),
        player.getInventory().getExtraContents(),
        player.getLevel(),
        player.getExp(),
        player.getHealth(),
        player.getFoodLevel()
    );
    worldInventories.computeIfAbsent(playerId, k -> new HashMap<>()).put(worldName, snap);
  }

  private void restoreInventory(UUID playerId, String worldName, Player player) {
    InventorySnapshot snap = worldInventories.getOrDefault(playerId, new HashMap<>()).get(worldName);
    if (snap == null) {
      // 首次进入该世界，清空背包
      player.getInventory().clear();
      player.setLevel(0);
      player.setExp(0);
      player.setHealth(20);
      player.setFoodLevel(20);
      return;
    }

    player.getInventory().setContents(snap.contents);
    player.getInventory().setArmorContents(snap.armor);
    player.getInventory().setExtraContents(snap.extra);
    player.setLevel(snap.level);
    player.setExp(snap.exp);
    player.setHealth(Math.min(snap.health, VersionAdapter.getMaxHealth(player)));
    player.setFoodLevel(snap.food);
  }

  public record InventorySnapshot(ItemStack[] contents, ItemStack[] armor, ItemStack[] extra,
                                   int level, float exp, double health, int food) {
  }
}
