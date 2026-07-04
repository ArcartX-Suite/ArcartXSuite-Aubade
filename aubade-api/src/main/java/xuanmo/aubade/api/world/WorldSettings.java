package xuanmo.aubade.api.world;

import org.bukkit.block.Biome;

/**
 * 世界配置接口。
 * 每种游戏模式提供自己的实现。
 */
public interface WorldSettings {

  /** 显示名称，如 "经典空岛" */
  String getFriendlyName();

  /** 世界文件夹名 */
  String getWorldName();

  /** 是否启用 Nether */
  boolean isNetherEnabled();

  /** 是否启用 End */
  boolean isEndEnabled();

  /** 最大岛屿尺寸 */
  int getMaxIslandSize();

  /** 默认保护范围 */
  int getDefaultProtectionRange();

  /** 岛屿间距 */
  int getIslandSpacing();

  /** 海平面 */
  int getSeaLevel();

  /** 默认游戏规则 */
  default boolean getDefaultGameRule(String rule) {
    return true;
  }

  /** 默认生物群系 */
  default Biome getDefaultBiome() {
    return Biome.PLAINS;
  }

  /** 组件默认启用 */
  default boolean isFeatureEnabled(String featureId) {
    return true;
  }
}
