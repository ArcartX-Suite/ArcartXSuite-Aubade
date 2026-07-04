package xuanmo.aubade.api.capability;

import java.util.UUID;

/**
 * 岛屿经济能力接口。
 * 供 AXS market/warehouse 等模块查询和修改岛屿经济。
 */
public interface IslandEconomyCapable {

  /**
   * 获取岛屿余额。
   */
  double getBalance(UUID islandId);

  /**
   * 增加岛屿余额。
   *
   * @return 是否成功
   */
  boolean deposit(UUID islandId, double amount);

  /**
   * 减少岛屿余额。
   *
   * @return 是否成功（余额不足返回 false）
   */
  boolean withdraw(UUID islandId, double amount);

  /**
   * 岛屿间转账。
   */
  boolean transfer(UUID fromIslandId, UUID toIslandId, double amount);
}
