package xuanmo.aubade.core.features.teams;

import java.io.File;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.AbstractFeatureAddon;

/**
 * 团队管理功能组件。
 * 提供成员管理、权限矩阵、岛主转让等 UI 与命令。
 */
public class TeamsAddon extends AbstractFeatureAddon {

  public TeamsAddon(AubadeCore core) {
    super(core, AddonDescriptor.builder("teams")
        .name("团队管理")
        .version("1.0.0")
        .mainClass(TeamsAddon.class.getName())
        .build());
  }

  @Override
  public String getFeatureId() {
    return "teams";
  }

  @Override
  public String getFriendlyName() {
    return "团队管理";
  }

  @Override
  public void onLoad() {
    // 加载配置
  }

  @Override
  public void onEnable() {
    super.onEnable();
    registerUi("member_manage.yml", "member_manage");
    registerUi("team_settings.yml", "team_settings");
    core.getLogger().info("[Teams] 团队管理组件已启用。");
  }

  @Override
  public void onDisable() {
    super.onDisable();
    core.getLogger().info("[Teams] 团队管理组件已禁用。");
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

