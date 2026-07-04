package xuanmo.aubade.core.features.bank;

import java.io.File;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.AbstractFeatureAddon;

/**
 * 岛屿银行功能组件。
 * 提供存取、利率和余额查询。
 */
public class BankAddon extends AbstractFeatureAddon {

  private double interestRate = 0.02; // 2% 默认利率

  public BankAddon(AubadeCore plugin) {
    super(plugin, AddonDescriptor.builder("bank")
        .name("岛屿银行")
        .version("1.0.0")
        .mainClass(BankAddon.class.getName())
        .build());
  }

  @Override
  public String getFeatureId() {
    return "bank";
  }

  @Override
  public String getFriendlyName() {
    return "岛屿银行";
  }

  @Override
  public void onLoad() {
    // 加载利率配置
  }

  @Override
  public void onEnable() {
    super.onEnable();
    registerUi("island_bank.yml", "island_bank");
    plugin.getLogger().info("[Bank] 岛屿银行组件已启用。");
  }

  @Override
  public void onDisable() {
    super.onDisable();
    plugin.getLogger().info("[Bank] 岛屿银行组件已禁用。");
  }

  public double getBalance(UUID islandId) {
    Optional<Island> opt = getIslandManager().getIslandById(islandId);
    return opt.map(Island::getBankBalance).orElse(0.0);
  }

  public boolean deposit(UUID islandId, double amount) {
    if (amount <= 0) {
      return false;
    }
    Optional<Island> opt = getIslandManager().getIslandById(islandId);
    if (opt.isEmpty()) {
      return false;
    }
    Island island = opt.get();
    island.setBankBalance(island.getBankBalance() + amount);
    getIslandManager().saveIsland(island);
    return true;
  }

  public boolean withdraw(UUID islandId, double amount) {
    if (amount <= 0) {
      return false;
    }
    Optional<Island> opt = getIslandManager().getIslandById(islandId);
    if (opt.isEmpty()) {
      return false;
    }
    Island island = opt.get();
    if (island.getBankBalance() < amount) {
      return false;
    }
    island.setBankBalance(island.getBankBalance() - amount);
    getIslandManager().saveIsland(island);
    return true;
  }

  public boolean transfer(UUID fromIslandId, UUID toIslandId, double amount) {
    if (!withdraw(fromIslandId, amount)) {
      return false;
    }
    return deposit(toIslandId, amount);
  }

  public double getInterestRate() {
    return interestRate;
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

