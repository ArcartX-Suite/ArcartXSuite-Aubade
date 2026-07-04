package xuanmo.aubade.core.ui.packet;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.island.IslandManagerImpl;

public class IslandUiPacketHandler extends BasePacketHandler {

  private static final String PREFIX = "SKYDREAM_";

  public IslandUiPacketHandler(AubadeCore core) {
    super(core);
  }

  @Override
  public boolean canHandle(String packetId) {
    return packetId != null && packetId.startsWith(PREFIX);
  }

  @Override
  protected void doHandle(Player player, String packetId, List<String> data) {
    String action = packetId.substring(PREFIX.length());
    Map<String, String> params = parseParams(data);
    IslandManagerImpl islandManager = core.getLifecycleManager().getIslandManager();

    switch (action) {
      case "ISLAND_CREATE" -> {
        String blueprintId = params.getOrDefault("blueprintId", "default");
        islandManager.createIsland(player, core.getCoreConfig().getDefaultGameMode(), blueprintId);
      }
      case "ISLAND_HOME" -> {
        Optional<Island> opt = islandManager.getIslandByOwner(player.getUniqueId());
        opt.ifPresent(island -> {
          player.teleport(island.getCenter().clone().add(0, 1, 0));
          player.sendMessage("§a正在返回你的岛屿...");
        });
      }
      case "ISLAND_DELETE" -> {
        Optional<Island> opt = islandManager.getIslandByOwner(player.getUniqueId());
        opt.ifPresent(island -> {
          islandManager.deleteIsland(island);
          player.sendMessage("§a岛屿已删除。");
        });
      }
      case "ISLAND_INVITE" -> {
        String targetName = params.get("target");
        if (targetName != null) {
          Player target = Bukkit.getPlayer(targetName);
          if (target != null) {
            islandManager.getIslandByOwner(player.getUniqueId()).ifPresent(island -> islandManager.invitePlayer(island, target));
          }
        }
      }
      case "ISLAND_INVITE_ACCEPT" -> islandManager.acceptInvite(player);
      case "ISLAND_INVITE_REJECT" -> player.sendMessage("§c你已拒绝邀请。");
      case "ISLAND_UNLOCK" -> {
        Optional<Island> opt = islandManager.getIslandByOwner(player.getUniqueId());
        opt.ifPresent(island -> {
          island.setLocked(false);
          islandManager.saveIsland(island);
          player.sendMessage("§a岛屿已解锁。");
        });
      }
      case "SETTINGS_TOGGLE" -> {
        String flagName = params.get("flagName");
        boolean value = Boolean.parseBoolean(params.getOrDefault("value", "false"));
        if (flagName != null) {
          islandManager.getIslandByOwner(player.getUniqueId()).ifPresent(island -> {
            island.getFlags().put(flagName, value);
            islandManager.saveIsland(island);
          });
        }
      }
      default -> player.sendMessage("§c未知 UI 动作: " + action);
    }
  }
}

