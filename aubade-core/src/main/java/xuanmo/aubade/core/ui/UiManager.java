package xuanmo.aubade.core.ui;

import java.io.File;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.ui.bridge.ArcartXUiBridge;

public class UiManager {

  private final Logger logger;
  private final ArcartXUiBridge activeBridge;

  public UiManager(AubadeCore core) {
    this.logger = core.getLogger();
    this.activeBridge = new ArcartXUiBridge(core);
    if (activeBridge.isAvailable()) {
      logger.info("[UI] 已接入 ArcartX 客户端 UI 桥接。");
    } else {
      logger.warning("[UI] ArcartX UI 桥接不可用，UI 功能已降级。");
    }
  }

  public boolean isUiAvailable() {
    return activeBridge.isAvailable();
  }

  public boolean registerUi(String name, String uiId, File uiFile) {
    return activeBridge.registerUi(name, uiId, uiFile);
  }

  public boolean openUi(Player player, String uiId) {
    return activeBridge.openUi(player, uiId);
  }

  public boolean sendPacket(Player player, String uiId, String handler, Object payload) {
    return activeBridge.sendPacket(player, uiId, handler, payload);
  }

  public ArcartXUiBridge getActiveBridge() {
    return activeBridge;
  }
}
