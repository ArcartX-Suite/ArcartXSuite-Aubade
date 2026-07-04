package xuanmo.aubade.api.island;

/**
 * 岛屿权限枚举。
 */
public enum IslandPermission {
  BREAK("破坏方块", true),
  PLACE("放置方块", true),
  INTERACT("交互", true),
  USE_CONTAINER("使用容器", true),
  USE_REDSTONE("使用红石", true),
  SPAWN_MOBS("刷怪", true),
  PVP("PvP", false),
  FLY("飞行", false),
  ANIMAL_SPAWN("动物生成", true),
  MONSTER_SPAWN("怪物生成", true),
  FIRE_SPREAD("火焰蔓延", false),
  TNT_DAMAGE("TNT 伤害", false),
  PISTON_PUSH("活塞推动", true),
  LEAF_DECAY("树叶腐烂", true),
  CROP_GROWTH("作物生长", true),
  WEATHER_CHANGE("天气变化", false);

  private final String displayName;
  private final boolean defaultValue;

  IslandPermission(String displayName, boolean defaultValue) {
    this.displayName = displayName;
    this.defaultValue = defaultValue;
  }

  public String getDisplayName() {
    return displayName;
  }

  public boolean getDefaultValue() {
    return defaultValue;
  }
}
