package xuanmo.aubade.core.features.border;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.AbstractFeatureAddon;

/**
 * 岛屿边界功能组件。
 * 通过 WorldBorder 为玩家提供岛屿边界视觉提示，支持动态调整保护范围。
 */
public class BorderAddon extends AbstractFeatureAddon {

  private final Map<UUID, Integer> borderSizeOverride = new HashMap<>();
  private boolean showBorder = true;

  public BorderAddon(AubadeCore core) {
    super(core, AddonDescriptor.builder("border")
        .name("岛屿边界")
        .version("1.0.0")
        .mainClass(BorderAddon.class.getName())
        .build());
  }

  @Override
  public String getFeatureId() {
    return "border";
  }

  @Override
  public String getFriendlyName() {
    return "岛屿边界";
  }

  @Override
  public void onLoad() {
    // 加载配置
  }

  @Override
  public void onEnable() {
    super.onEnable();
    registerUi("border_settings.yml", "border_settings");
    core.getLogger().info("[Border] 岛屿边界组件已启用。");

    // 启动边界刷新任务
    Bukkit.getScheduler().runTaskTimer(javaPlugin(), this::tickBorders, 20L, 20L);
  }

  @Override
  public void onDisable() {
    super.onDisable();
    // 清除所有玩家的世界边界
    for (Player player : Bukkit.getOnlinePlayers()) {
      WorldBorder wb = player.getWorld().getWorldBorder();
      player.setWorldBorder(null);
    }
    core.getLogger().info("[Border] 岛屿边界组件已禁用。");
  }

  /**
   * 设置是否显示边界。
   */
  public void setShowBorder(boolean show) {
    this.showBorder = show;
  }

  public boolean isShowBorder() {
    return showBorder;
  }

  /**
   * 获取岛屿实际边界半径（含 override）。
   */
  public int getBorderSize(Island island) {
    if (island == null) {
      return 0;
    }
    Integer override = borderSizeOverride.get(island.getUniqueId());
    return override != null ? override : island.getProtectionRange();
  }

  /**
   * 为玩家应用其所在岛屿的边界。
   */
  public void applyBorder(Player player) {
    if (!showBorder) {
      player.setWorldBorder(null);
      return;
    }

    Optional<Island> opt = getIslandManager().getIslandByOwner(player.getUniqueId());
    if (opt.isEmpty()) {
      player.setWorldBorder(null);
      return;
    }

    Island island = opt.get();
    if (island.getCenter() == null || island.getWorld() == null) {
      player.setWorldBorder(null);
      return;
    }

    if (!player.getWorld().equals(island.getWorld())) {
      player.setWorldBorder(null);
      return;
    }

    int size = getBorderSize(island) * 2;
    Location center = island.getCenter();

    WorldBorder border = Bukkit.createWorldBorder();
    border.setCenter(center);
    border.setSize(size);
    border.setDamageAmount(0);
    border.setDamageBuffer(0);
    border.setWarningDistance(5);
    border.setWarningTime(0);
    player.setWorldBorder(border);
  }

  /**
   * 设置特定岛屿的边界覆盖半径。
   */
  public void setBorderOverride(UUID islandId, int radius) {
    if (radius <= 0) {
      borderSizeOverride.remove(islandId);
    } else {
      borderSizeOverride.put(islandId, radius);
    }
  }

  private void tickBorders() {
    if (!showBorder) {
      return;
    }
    for (Player player : Bukkit.getOnlinePlayers()) {
      try {
        applyBorder(player);
      } catch (Exception e) {
        // 忽略单个玩家的边界异常
      }
    }
  }

  private void registerUi(String fileName, String uiId) {
    File uiDir = new File(core.getDataFolder(), "arcartx/ui");
    File uiFile = new File(uiDir, fileName);
    if (!uiFile.exists()) {
      core.saveResource("arcartx/ui/" + fileName, false);
    }
    getUiManager().registerUi(uiId, uiId, uiFile);
  }
}
