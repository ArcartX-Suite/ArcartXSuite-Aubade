package xuanmo.aubade.core.features.farmersdance;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.AbstractExtensionAddon;

/**
 * 农夫之舞扩展组件。
 * 玩家在作物附近潜行时，加速周围作物的生长。
 */
public class FarmersDanceAddon extends AbstractExtensionAddon implements Listener {

  private static final Set<Material> CROPS = EnumSet.of(
      Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS,
      Material.NETHER_WART, Material.COCOA, Material.SWEET_BERRY_BUSH
  );

  private final Set<UUID> sneakingPlayers = ConcurrentHashMap.newKeySet();
  private int radius = 3;
  private int growthTicks = 2;
  private int taskId = -1;

  public FarmersDanceAddon(AubadeCore plugin) {
    super(plugin, AddonDescriptor.builder("farmersdance")
        .name("农夫之舞")
        .version("1.0.0")
        .mainClass(FarmersDanceAddon.class.getName())
        .build());
  }

  @Override
  public String getExtensionId() {
    return "farmersdance";
  }

  @Override
  public String getFriendlyName() {
    return "农夫之舞";
  }

  @Override
  public void onEnable() {
    super.onEnable();
    Bukkit.getPluginManager().registerEvents(this, javaPlugin());
    startGrowthTask();
    plugin.getLogger().info("[FarmersDance] 农夫之舞扩展已启用。");
  }

  @Override
  public void onDisable() {
    super.onDisable();
    PlayerToggleSneakEvent.getHandlerList().unregister(this);
    if (taskId != -1) {
      Bukkit.getScheduler().cancelTask(taskId);
    }
    sneakingPlayers.clear();
    plugin.getLogger().info("[FarmersDance] 农夫之舞扩展已禁用。");
  }

  private void startGrowthTask() {
    taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(javaPlugin(), () -> {
      for (UUID playerId : sneakingPlayers) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline() || !player.isSneaking()) {
          continue;
        }
        Optional<Island> opt = getIslandManager().getIslandAt(player.getLocation());
        if (opt.isEmpty()) {
          continue;
        }
        Island island = opt.get();
        if (!island.hasPermission(playerId, xuanmo.arcartxsuite.api.aubade.island.IslandPermission.PLACE)) {
          continue;
        }

        Block center = player.getLocation().getBlock();
        for (int dx = -radius; dx <= radius; dx++) {
          for (int dy = -1; dy <= 2; dy++) {
            for (int dz = -radius; dz <= radius; dz++) {
              Block crop = center.getRelative(dx, dy, dz);
              if (!CROPS.contains(crop.getType())) {
                continue;
              }
              if (crop.getBlockData() instanceof Ageable ageable) {
                int current = ageable.getAge();
                int max = ageable.getMaximumAge();
                if (current < max) {
                  ageable.setAge(Math.min(current + growthTicks, max));
                  crop.setBlockData(ageable);
                }
              }
            }
          }
        }
      }
    }, 20L, 20L); // 每秒检查一次
  }

  @EventHandler
  public void onSneak(PlayerToggleSneakEvent event) {
    Player player = event.getPlayer();
    if (event.isSneaking()) {
      sneakingPlayers.add(player.getUniqueId());
    } else {
      sneakingPlayers.remove(player.getUniqueId());
    }
  }
}
