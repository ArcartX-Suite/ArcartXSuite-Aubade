package xuanmo.aubade.core.ui.packet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xuanmo.aubade.core.AubadeCore;

public abstract class BasePacketHandler {

  protected final AubadeCore core;

  protected BasePacketHandler(AubadeCore core) {
    this.core = core;
  }

  public boolean handle(Player player, String packetId, List<String> data) {
    if (!canHandle(packetId)) {
      return false;
    }
    if (Bukkit.isPrimaryThread()) {
      doHandle(player, packetId, data);
    } else {
      Bukkit.getScheduler().runTask(core.plugin(), () -> doHandle(player, packetId, data));
    }
    return true;
  }

  public abstract boolean canHandle(String packetId);

  protected abstract void doHandle(Player player, String packetId, List<String> data);

  protected Map<String, String> parseParams(List<String> data) {
    Map<String, String> params = new HashMap<>();
    for (int i = 0; i < data.size(); i += 2) {
      String key = data.get(i);
      String value = (i + 1 < data.size()) ? data.get(i + 1) : "";
      params.put(key, value);
    }
    return params;
  }
}

