package xuanmo.aubade.core.features.limits;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.AbstractExtensionAddon;

/**
 * 岛屿限制扩展组件。
 * 限制岛屿内的方块放置数量和实体生成数量。
 */
public class LimitsAddon extends AbstractExtensionAddon implements Listener {

  private final Map<UUID, Map<String, Integer>> islandBlockCounts = new HashMap<>();
  private final Map<UUID, Map<String, Integer>> islandEntityCounts = new HashMap<>();
  private int maxBlocksPerType = 1000;
  private int maxEntitiesPerType = 50;

  public LimitsAddon(AubadeCore plugin) {
    super(plugin, AddonDescriptor.builder("limits")
        .name("岛屿限制")
        .version("1.0.0")
        .mainClass(LimitsAddon.class.getName())
        .build());
  }

  @Override
  public String getExtensionId() {
    return "limits";
  }

  @Override
  public String getFriendlyName() {
    return "岛屿限制";
  }

  @Override
  public void onEnable() {
    super.onEnable();
    Bukkit.getPluginManager().registerEvents(this, javaPlugin());
    plugin.getLogger().info("[Limits] 岛屿限制扩展已启用。");
  }

  @Override
  public void onDisable() {
    super.onDisable();
    BlockPlaceEvent.getHandlerList().unregister(this);
    CreatureSpawnEvent.getHandlerList().unregister(this);
    plugin.getLogger().info("[Limits] 岛屿限制扩展已禁用。");
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {
    var islandOpt = getIslandManager().getIslandAt(event.getBlock().getLocation());
    if (islandOpt.isEmpty()) {
      return;
    }
    Island island = islandOpt.get();
    if (!island.hasPermission(event.getPlayer().getUniqueId(), xuanmo.arcartxsuite.api.aubade.island.IslandPermission.PLACE)) {
      return;
    }

    String materialKey = event.getBlock().getType().getKey().toString();
    UUID islandId = island.getUniqueId();
    int current = islandBlockCounts
        .computeIfAbsent(islandId, k -> new HashMap<>())
        .getOrDefault(materialKey, 0);

    if (current >= maxBlocksPerType) {
      event.setCancelled(true);
      event.getPlayer().sendMessage("§c该岛屿此方块数量已达上限 (" + maxBlocksPerType + ")。");
      return;
    }

    islandBlockCounts.get(islandId).put(materialKey, current + 1);
  }

  @EventHandler
  public void onEntitySpawn(CreatureSpawnEvent event) {
    var islandOpt = getIslandManager().getIslandAt(event.getLocation());
    if (islandOpt.isEmpty()) {
      return;
    }
    Island island = islandOpt.get();
    UUID islandId = island.getUniqueId();
    String entityKey = event.getEntityType().name();

    int current = islandEntityCounts
        .computeIfAbsent(islandId, k -> new HashMap<>())
        .getOrDefault(entityKey, 0);

    if (current >= maxEntitiesPerType) {
      event.setCancelled(true);
      return;
    }

    islandEntityCounts.get(islandId).put(entityKey, current + 1);
  }

  public int getBlockCount(UUID islandId, String materialKey) {
    return islandBlockCounts.getOrDefault(islandId, new HashMap<>()).getOrDefault(materialKey, 0);
  }

  public int getEntityCount(UUID islandId, String entityKey) {
    return islandEntityCounts.getOrDefault(islandId, new HashMap<>()).getOrDefault(entityKey, 0);
  }

  public void decrementBlockCount(UUID islandId, String materialKey) {
    islandBlockCounts.computeIfAbsent(islandId, k -> new HashMap<>())
        .merge(materialKey, -1, Integer::sum);
  }
}
