package xuanmo.aubade.core.features.level;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.arcartxsuite.api.aubade.event.IslandLevelChangeEvent;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.arcartxsuite.api.aubade.ui.PacketPayloadBuilder;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.AbstractFeatureAddon;
import xuanmo.aubade.core.features.level.command.LevelCommand;

/**
 * 岛屿等级功能组件。
 * 提供方块扫描、等级计算、排行榜和 HUD 显示。
 */
public class LevelAddon extends AbstractFeatureAddon {

  private BlockValues blockValues;
  private LevelCalculator calculator;
  private LevelTopCache topCache;

  public LevelAddon(AubadeCore plugin) {
    super(plugin, AddonDescriptor.builder("level")
        .name("岛屿等级")
        .version("1.0.0")
        .mainClass(LevelAddon.class.getName())
        .build());
  }

  @Override
  public String getFeatureId() {
    return "level";
  }

  @Override
  public String getFriendlyName() {
    return "岛屿等级";
  }

  @Override
  public void onLoad() {
    // 加载方块价值表
    File configFile = new File(plugin.getDataFolder(), "features/level.yml");
    if (!configFile.exists()) {
      plugin.saveResource("features/level.yml", false);
    }
    YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
    blockValues = new BlockValues(config.getConfigurationSection("block-values"), 1L, plugin.getLogger());
    calculator = new LevelCalculator(blockValues, plugin.getLogger());
    topCache = new LevelTopCache(plugin.getLogger());
  }

  @Override
  public void onEnable() {
    super.onEnable();

    // 注册命令
    try {
      getCommandManager().registerSubCommand("island", new LevelCommand(this));
    } catch (Exception e) {
      plugin.getLogger().warning("[Level] 注册命令失败: " + e.getMessage());
    }

    // 注册 UI
    registerUi("level_display.yml", "island_level_display");
    registerUi("level_top.yml", "island_level_top");

    plugin.getLogger().info("[Level] 岛屿等级组件已启用。");
  }

  @Override
  public void onDisable() {
    super.onDisable();
    plugin.getLogger().info("[Level] 岛屿等级组件已禁用。");
  }

  /**
   * 计算岛屿当前等级（同步扫描）。
   */
  public long calculateLevel(Island island) {
    return calculator.scanSync(island);
  }

  /**
   * 异步计算并更新岛屿等级。
   */
  public CompletableFuture<Long> updateLevelAsync(Island island) {
    return calculator.scanAsync(island).thenApply(newLevel -> {
      long oldLevel = island.getLevel();
      if (newLevel != oldLevel) {
        island.setLevel(newLevel);
        getIslandManager().saveIsland(island);
        topCache.update(island);

        // 切回主线程触发事件
        Bukkit.getScheduler().runTask(javaPlugin(), () -> {
          Bukkit.getPluginManager().callEvent(new IslandLevelChangeEvent(island, oldLevel, newLevel));
        });
      }
      return newLevel;
    });
  }

  /**
   * 获取排行榜 Top N。
   */
  public List<LevelTopCache.TopEntry> getTop(int n) {
    return topCache.getTop(n);
  }

  public BlockValues getBlockValues() {
    return blockValues;
  }

  public LevelTopCache getTopCache() {
    return topCache;
  }

  private void registerUi(String fileName, String uiId) {
    File uiDir = new File(plugin.getDataFolder(), "arcartx/ui");
    File uiFile = new File(uiDir, fileName);
    if (!uiFile.exists()) {
      plugin.saveResource("arcartx/ui/" + fileName, false);
    }
    getUiManager().registerUi(uiId, uiId, uiFile);
  }
}
