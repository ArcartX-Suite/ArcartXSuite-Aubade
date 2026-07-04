package xuanmo.aubade.core.features.upgrades;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.AbstractExtensionAddon;

public class UpgradesAddon extends AbstractExtensionAddon {

  private final Map<String, UpgradeConfig> upgradeConfigs = new HashMap<>();
  private final Map<UUID, Map<String, Integer>> islandUpgrades = new HashMap<>();

  public UpgradesAddon(AubadeCore plugin) {
    super(plugin, AddonDescriptor.builder("upgrades")
        .name("岛屿升级")
        .version("1.0.0")
        .mainClass(UpgradesAddon.class.getName())
        .build());
  }

  @Override
  public String getExtensionId() {
    return "upgrades";
  }

  @Override
  public String getFriendlyName() {
    return "岛屿升级";
  }

  @Override
  public void onLoad() {
    File configFile = new File(plugin.getDataFolder(), "features/upgrades.yml");
    if (!configFile.exists()) {
      plugin.saveResource("features/upgrades.yml", false);
    }
    YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
    loadUpgradeConfig("protection_range", config);
    loadUpgradeConfig("member_limit", config);
  }

  @Override
  public void onEnable() {
    super.onEnable();
    plugin.getLogger().info("[Upgrades] 岛屿升级扩展已启用。");
  }

  @Override
  public void onDisable() {
    super.onDisable();
  }

  @Override
  public void onReload() {
    upgradeConfigs.clear();
    onLoad();
  }

  private void loadUpgradeConfig(String key, YamlConfiguration config) {
    int maxLevel = config.getInt(key + ".max-level", 5);
    double baseCost = config.getDouble(key + ".base-cost", 1000);
    double costMultiplier = config.getDouble(key + ".cost-multiplier", 1.5);
    int baseValue = config.getInt(key + ".base-value", 50);
    int valueIncrement = config.getInt(key + ".value-increment", 10);
    upgradeConfigs.put(key, new UpgradeConfig(maxLevel, baseCost, costMultiplier, baseValue, valueIncrement));
  }

  public int getLevel(UUID islandId, String upgradeKey) {
    return islandUpgrades.getOrDefault(islandId, new HashMap<>()).getOrDefault(upgradeKey, 0);
  }

  public int getValue(UUID islandId, String upgradeKey) {
    UpgradeConfig cfg = upgradeConfigs.get(upgradeKey);
    if (cfg == null) {
      return 0;
    }
    int level = getLevel(islandId, upgradeKey);
    return cfg.baseValue + (level * cfg.valueIncrement);
  }

  public double getNextCost(UUID islandId, String upgradeKey) {
    UpgradeConfig cfg = upgradeConfigs.get(upgradeKey);
    if (cfg == null) {
      return -1;
    }
    int currentLevel = getLevel(islandId, upgradeKey);
    if (currentLevel >= cfg.maxLevel) {
      return -1;
    }
    return cfg.baseCost * Math.pow(cfg.costMultiplier, currentLevel);
  }

  public boolean upgrade(UUID islandId, String upgradeKey) {
    UpgradeConfig cfg = upgradeConfigs.get(upgradeKey);
    if (cfg == null) {
      return false;
    }
    int currentLevel = getLevel(islandId, upgradeKey);
    if (currentLevel >= cfg.maxLevel) {
      return false;
    }
    Optional<Island> opt = getIslandManager().getIslandById(islandId);
    if (opt.isEmpty()) {
      return false;
    }
    Island island = opt.get();
    double cost = getNextCost(islandId, upgradeKey);
    if (cost < 0 || island.getBankBalance() < cost) {
      return false;
    }
    island.setBankBalance(island.getBankBalance() - cost);
    getIslandManager().saveIsland(island);
    islandUpgrades.computeIfAbsent(islandId, k -> new HashMap<>()).put(upgradeKey, currentLevel + 1);
    if ("protection_range".equals(upgradeKey)) {
      island.setProtectionRange(getValue(islandId, upgradeKey));
      getIslandManager().saveIsland(island);
    }
    return true;
  }

  public Map<String, UpgradeConfig> getUpgradeConfigs() {
    return new HashMap<>(upgradeConfigs);
  }

  public record UpgradeConfig(int maxLevel, double baseCost, double costMultiplier, int baseValue, int valueIncrement) {
  }
}

