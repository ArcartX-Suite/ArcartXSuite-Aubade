package xuanmo.aubade.core.features.extramobs;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.AbstractExtensionAddon;

/**
 * 额外生物扩展组件。
 * 调整岛屿内的刷怪规则，支持禁用特定生物生成或修改生成概率。
 */
public class ExtraMobsAddon extends AbstractExtensionAddon implements Listener {

  private final Map<EntityType, Boolean> spawnAllowed = new HashMap<>();
  private double globalSpawnMultiplier = 1.0;

  public ExtraMobsAddon(AubadeCore plugin) {
    super(plugin, AddonDescriptor.builder("extramobs")
        .name("额外生物")
        .version("1.0.0")
        .mainClass(ExtraMobsAddon.class.getName())
        .build());
  }

  @Override
  public String getExtensionId() {
    return "extramobs";
  }

  @Override
  public String getFriendlyName() {
    return "额外生物";
  }

  @Override
  public void onEnable() {
    super.onEnable();
    loadDefaultRules();
    Bukkit.getPluginManager().registerEvents(this, javaPlugin());
    plugin.getLogger().info("[ExtraMobs] 额外生物扩展已启用。");
  }

  @Override
  public void onDisable() {
    super.onDisable();
    CreatureSpawnEvent.getHandlerList().unregister(this);
    plugin.getLogger().info("[ExtraMobs] 额外生物扩展已禁用。");
  }

  private void loadDefaultRules() {
    // 默认允许所有自然生成
    for (EntityType type : EntityType.values()) {
      if (type.isAlive() && type != EntityType.PLAYER) {
        spawnAllowed.put(type, true);
      }
    }
    // 示例：禁用苦力怕自然生成
    spawnAllowed.put(EntityType.CREEPER, false);
  }

  @EventHandler
  public void onCreatureSpawn(CreatureSpawnEvent event) {
    if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
      return;
    }

    Optional<Island> opt = getIslandManager().getIslandAt(event.getLocation());
    if (opt.isEmpty()) {
      return;
    }

    EntityType type = event.getEntityType();
    Boolean allowed = spawnAllowed.get(type);
    if (allowed != null && !allowed) {
      event.setCancelled(true);
      return;
    }

    // 概率倍率
    if (globalSpawnMultiplier < 1.0 && Math.random() > globalSpawnMultiplier) {
      event.setCancelled(true);
    }
  }

  public void setSpawnAllowed(EntityType type, boolean allowed) {
    spawnAllowed.put(type, allowed);
  }

  public boolean isSpawnAllowed(EntityType type) {
    return spawnAllowed.getOrDefault(type, true);
  }

  public void setGlobalSpawnMultiplier(double multiplier) {
    this.globalSpawnMultiplier = Math.max(0, Math.min(multiplier, 5.0));
  }
}
