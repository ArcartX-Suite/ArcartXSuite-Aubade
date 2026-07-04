package xuanmo.aubade.api.ui;

import java.io.File;
import org.bukkit.entity.Player;

/**
 * UI 桥接接口。
 * 抽象 ArcartX 客户端 UI 的底层实现。
 */
public interface UiBridge {

  boolean registerUi(String name, String uiId, File uiFile);

  boolean openUi(Player player, String uiId);

  boolean sendPacket(Player player, String uiId, String handler, Object payload);
}
