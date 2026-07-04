package xuanmo.aubade.core.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.aubade.command.CompositeCommand;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.arcartxsuite.api.aubade.permission.Permission;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.island.IslandManagerImpl;

/**
 * /isadmin info <玩家> — 查看指定玩家的岛屿信息。
 */
public class AdminInfoCommand extends CompositeCommand {

  private final AubadeCore core;

  public AdminInfoCommand(AubadeCore core) {
    super("info", "查看指定玩家的岛屿信息", Permission.ADMIN_INFO, false);
    this.core = core;
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    if (!checkPermission(sender)) {
      sender.sendMessage("§c你没有权限使用此命令。");
      return true;
    }
    if (args.length == 0) {
      sender.sendMessage("§c用法: /isadmin info <玩家>");
      return true;
    }
    IslandManagerImpl manager = core.getLifecycleManager() != null ? core.getLifecycleManager().getIslandManager() : null;
    if (manager == null) {
      sender.sendMessage("§c当前没有可用的岛屿管理器。");
      return true;
    }

    OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
    Optional<Island> opt = manager.getIslandByOwner(target.getUniqueId());
    if (opt.isEmpty()) {
      sender.sendMessage("§c目标玩家没有岛屿。");
      return true;
    }

    Island island = opt.get();
    String ownerName = target.getName() != null ? target.getName() : args[0];
    Location center = island.getCenter();
    sender.sendMessage("§6========== 岛屿信息 ==========");
    sender.sendMessage("§e岛屿ID: §f" + island.getUniqueId().toString().substring(0, 8));
    sender.sendMessage("§e岛主: §f" + ownerName);
    sender.sendMessage("§e成员数: §f" + island.getMembers().size());
    sender.sendMessage("§e中心: §f" + formatLocation(center));
    sender.sendMessage("§e保护范围: §f" + island.getProtectionRange() + " 格");
    sender.sendMessage("§e等级: §f" + island.getLevel());
    sender.sendMessage("§e银行余额: §f" + island.getBankBalance());
    sender.sendMessage("§e锁岛: §f" + (island.isLocked() ? "是" : "否"));
    sender.sendMessage("§6==============================");
    return true;
  }

  @Override
  public List<String> tabComplete(CommandSender sender, String[] args) {
    List<String> result = new ArrayList<>();
    if (args.length <= 1) {
      String prefix = args.length == 0 ? "" : args[0].toLowerCase();
      for (Player player : Bukkit.getOnlinePlayers()) {
        if (player.getName().toLowerCase().startsWith(prefix)) {
          result.add(player.getName());
        }
      }
    }
    return result;
  }

  private String formatLocation(Location location) {
    if (location == null || location.getWorld() == null) {
      return "未知";
    }
    return location.getWorld().getName() + " "
        + String.format("%.1f, %.1f, %.1f", location.getX(), location.getY(), location.getZ());
  }
}
