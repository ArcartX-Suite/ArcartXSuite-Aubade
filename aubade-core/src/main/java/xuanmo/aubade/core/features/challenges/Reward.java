package xuanmo.aubade.core.features.challenges;

/**
 * 挑战奖励定义。
 */
public class Reward {

  private final RewardType type;
  private final String target; // 物品ID / 权限节点 / 经验类型
  private final double amount;
  private final String description;

  public Reward(RewardType type, String target, double amount, String description) {
    this.type = type;
    this.target = target;
    this.amount = amount;
    this.description = description;
  }

  public RewardType getType() {
    return type;
  }

  public String getTarget() {
    return target;
  }

  public double getAmount() {
    return amount;
  }

  public String getDescription() {
    return description;
  }

  public enum RewardType {
    EXP, ITEM, MONEY, PERMISSION, POINTS
  }
}

