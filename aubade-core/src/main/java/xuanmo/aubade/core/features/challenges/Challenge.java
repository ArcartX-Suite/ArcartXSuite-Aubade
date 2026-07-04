package xuanmo.aubade.core.features.challenges;

import java.util.List;
import java.util.Map;

/**
 * 挑战定义。
 */
public class Challenge {

  private final String id;
  private final String name;
  private final String description;
  private final ChallengeType type;
  private final Map<String, Integer> requirements; // item/entity/amount
  private final List<Reward> rewards;
  private final boolean repeatable;
  private final int maxRepeats;
  private final List<String> requiredChallenges; // 前置挑战ID
  private final String icon; // itemJson 或 material 名称

  public Challenge(String id, String name, String description, ChallengeType type,
      Map<String, Integer> requirements, List<Reward> rewards, boolean repeatable,
      int maxRepeats, List<String> requiredChallenges, String icon) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.type = type;
    this.requirements = requirements;
    this.rewards = rewards;
    this.repeatable = repeatable;
    this.maxRepeats = maxRepeats;
    this.requiredChallenges = requiredChallenges;
    this.icon = icon;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public ChallengeType getType() {
    return type;
  }

  public Map<String, Integer> getRequirements() {
    return requirements;
  }

  public List<Reward> getRewards() {
    return rewards;
  }

  public boolean isRepeatable() {
    return repeatable;
  }

  public int getMaxRepeats() {
    return maxRepeats;
  }

  public List<String> getRequiredChallenges() {
    return requiredChallenges;
  }

  public String getIcon() {
    return icon;
  }
}

