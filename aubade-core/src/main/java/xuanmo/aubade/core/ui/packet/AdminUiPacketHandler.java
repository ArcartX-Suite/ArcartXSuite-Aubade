package xuanmo.aubade.core.ui.packet;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.World;
import org.bukkit.entity.Player;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.arcartxsuite.api.aubade.island.Island;

public class AdminUiPacketHandler extends BasePacketHandler {

  private static final String PREFIX = "SKYDREAM_ADMIN";

  public AdminUiPacketHandler(AubadeCore core) {
    super(core);
  }

  @Override
  public boolean canHandle(String packetId) {
    return packetId != null && packetId.startsWith(PREFIX);
  }

  @Override
  protected void doHandle(Player player, String packetId, List<String> data) {
    String action = packetId.substring(PREFIX.length());
    if ("refresh".equalsIgnoreCase(action)) {
      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("worlds", buildWorlds());
      payload.put("players", buildPlayers());
      payload.put("islands", buildIslands());
      if (!core.getUiManager().sendPacket(player, "aubade_admin", "update", payload)) {
        player.sendMessage("§c管理面板当前不可用。");
      }
      return;
    }
    player.sendMessage("§c未知 UI 动作: " + action);
  }

  private Map<String, Map<String, Object>> buildWorlds() {
    Map<String, Map<String, Object>> result = new LinkedHashMap<>();
    for (World world : core.getServer().getWorlds()) {
      Map<String, Object> entry = new LinkedHashMap<>();
      entry.put("name", world.getName());
      entry.put("environment", world.getEnvironment().name());
      entry.put("players", world.getPlayers().size());
      result.put(world.getName(), entry);
    }
    return result;
  }

  private Map<String, Map<String, Object>> buildPlayers() {
    Map<String, Map<String, Object>> result = new LinkedHashMap<>();
    core.getServer().getOnlinePlayers().forEach(player -> {
      Map<String, Object> entry = new LinkedHashMap<>();
      entry.put("name", player.getName());
      entry.put("uuid", player.getUniqueId().toString());
      result.put(player.getUniqueId().toString(), entry);
    });
    return result;
  }

  private Map<String, Map<String, Object>> buildIslands() {
    Map<String, Map<String, Object>> result = new LinkedHashMap<>();
    var islandManager = core.getLifecycleManager() != null ? core.getLifecycleManager().getIslandManager() : null;
    if (islandManager == null) {
      return result;
    }
    for (Island island : islandManager.getCachedIslands()) {
      Map<String, Object> entry = new LinkedHashMap<>();
      entry.put("name", island.getName() != null ? island.getName() : "未命名");
      entry.put("owner", island.getOwner() != null ? island.getOwner().toString() : "");
      entry.put("level", island.getLevel());
      entry.put("range", island.getProtectionRange());
      result.put(island.getUniqueId().toString(), entry);
    }
    return result;
  }
}
