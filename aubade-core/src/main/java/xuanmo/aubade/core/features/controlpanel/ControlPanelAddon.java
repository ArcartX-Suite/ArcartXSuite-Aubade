package xuanmo.aubade.core.features.controlpanel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.AbstractExtensionAddon;

/**
 * 管理员控制面板扩展组件。
 * 提供全局岛屿管理接口（清理、重置、统计等），UI 集成到 aubade_admin.yml。
 */
public class ControlPanelAddon extends AbstractExtensionAddon {

  private final Map<UUID, Long> lastPurgeCheck = new HashMap<>();

  public ControlPanelAddon(AubadeCore plugin) {
    super(plugin, AddonDescriptor.builder("controlpanel")
        .name("控制面板")
        .version("1.0.0")
        .mainClass(ControlPanelAddon.class.getName())
        .build());
  }

  @Override
  public String getExtensionId() {
    return "controlpanel";
  }

  @Override
  public String getFriendlyName() {
    return "控制面板";
  }

  @Override
  public void onEnable() {
    super.onEnable();
    plugin.getLogger().info("[ControlPanel] 管理员控制面板扩展已启用。");
  }

  @Override
  public void onDisable() {
    super.onDisable();
    plugin.getLogger().info("[ControlPanel] 管理员控制面板扩展已禁用。");
  }

  /**
   * 获取全局统计信息。
   */
  public Map<String, Object> getGlobalStats() {
    Map<String, Object> stats = new HashMap<>();
    stats.put("island_count", getIslandManager().getIslandCount());
    stats.put("worlds", getIslandManager().getIslandsInWorld(null).stream()
        .map(i -> i.getWorld() != null ? i.getWorld().getName() : "未知")
        .distinct()
        .toList());
    return stats;
  }

  /**
   * 获取可清理的离线岛屿（超过指定天数未登录）。
   */
  public java.util.List<Island> getPurgeableIslands(int inactiveDays) {
    long threshold = System.currentTimeMillis() - (inactiveDays * 86400000L);
    return getIslandManager().getIslandsInWorld(null).stream()
        .filter(island -> !island.isPurgeProtected())
        .filter(island -> island.getLastLoginTime() < threshold)
        .toList();
  }

  /**
   * 标记岛屿为清理保护状态。
   */
  public void setPurgeProtected(UUID islandId, boolean protected_) {
    getIslandManager().getIslandById(islandId).ifPresent(island -> {
      island.setPurgeProtected(protected_);
      getIslandManager().saveIsland(island);
    });
  }

  /**
   * 重置岛屿（清空所有方块，保留岛屿元数据）。
   */
  public boolean resetIsland(UUID islandId) {
    var opt = getIslandManager().getIslandById(islandId);
    if (opt.isEmpty()) {
      return false;
    }
    Island island = opt.get();
    // 将在线玩家传送走
    if (island.getWorld() != null) {
      for (Player player : Bukkit.getOnlinePlayers()) {
        if (island.inProtectionRange(player.getLocation())) {
          player.teleport(island.getWorld().getSpawnLocation());
          player.sendMessage("§c你所在的岛屿已被管理员重置。");
        }
      }
    }
    // 重置等级和银行
    island.setLevel(0);
    island.setBankBalance(0);
    getIslandManager().saveIsland(island);
    plugin.getLogger().info("[ControlPanel] 岛屿 " + islandId + " 已被重置。");
    return true;
  }
}

