package xuanmo.aubade.core.ui.bridge;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.aubade.ui.UiBridge;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.arcartxsuite.api.bridge.PacketBridgeAPI;

public class ArcartXUiBridge implements UiBridge {

  private final Logger logger;
  private final Map<String, String> runtimeIds = new HashMap<>();
  private final PacketBridgeAPI packetBridge;

  public ArcartXUiBridge(AubadeCore core) {
    this.logger = core.getLogger();
    this.packetBridge = core.packetBridge();
  }

  public boolean isAvailable() {
    return packetBridge != null && packetBridge.isAvailable();
  }

  @Override
  public boolean registerUi(String name, String uiId, File uiFile) {
    if (!isAvailable()) {
      return false;
    }
    try {
      PacketBridgeAPI.UiRegistrationResult result = packetBridge.registerOrReloadUi(uiId, uiFile);
      runtimeIds.put(uiId, result.runtimeUiId());
      return result.success();
    } catch (Exception e) {
      logger.warning("[UI] 注册 UI 失败 [" + uiId + "]: " + e.getMessage());
      return false;
    }
  }

  @Override
  public boolean openUi(Player player, String uiId) {
    if (!isAvailable()) {
      return false;
    }
    try {
      String runtimeId = runtimeIds.getOrDefault(uiId, uiId);
      return packetBridge.openUi(player, runtimeId);
    } catch (Exception e) {
      logger.warning("[UI] 打开 UI 失败 [" + uiId + "]: " + e.getMessage());
      return false;
    }
  }

  @Override
  public boolean sendPacket(Player player, String uiId, String handler, Object payload) {
    if (!isAvailable()) {
      return false;
    }
    try {
      String runtimeId = runtimeIds.getOrDefault(uiId, uiId);
      return packetBridge.sendPacket(player, runtimeId, handler, payload);
    } catch (Exception e) {
      logger.warning("[UI] 发送 Packet 失败 [" + uiId + "]: " + e.getMessage());
      return false;
    }
  }
}
