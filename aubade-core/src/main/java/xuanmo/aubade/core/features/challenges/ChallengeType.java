package xuanmo.aubade.core.features.challenges;

/**
 * 挑战类型枚举。
 */
public enum ChallengeType {
  COLLECT("收集"),
  PLACE("放置"),
  KILL("击杀"),
  EXPLORE("探索"),
  ECONOMY("经济"),
  ISLAND("岛屿属性"),
  CRAFT("合成");

  private final String displayName;

  ChallengeType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}

