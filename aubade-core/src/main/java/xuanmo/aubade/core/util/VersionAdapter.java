package xuanmo.aubade.core.util;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Bukkit 跨版本适配工具。
 * 处理不同 Paper/Spigot 版本间 API 变动，避免 deprecation 警告。
 */
public final class VersionAdapter {

  private VersionAdapter() {
  }

  /**
   * 获取实体的最大生命值。
   * 1.20.6+ 推荐使用 getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()
   */
  public static double getMaxHealth(LivingEntity entity) {
    try {
      var attr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
      if (attr != null) {
        return attr.getValue();
      }
    } catch (NoSuchFieldError | NoClassDefFoundError e) {
      // 旧版本回退
    }
    return entity.getMaxHealth();
  }

  /**
   * 获取插件版本字符串。
   */
  public static String getPluginVersion(JavaPlugin plugin) {
    try {
      // 1.20.6+ 推荐方式
      return plugin.getPluginMeta().getVersion();
    } catch (NoSuchMethodError e) {
      return plugin.getDescription().getVersion();
    }
  }

  /**
   * 设置玩家生命值，自动限制在最大值内。
   */
  public static void setHealthSafe(Player player, double health) {
    double max = getMaxHealth(player);
    player.setHealth(Math.min(health, max));
  }
}

