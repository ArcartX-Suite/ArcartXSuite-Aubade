package xuanmo.aubade.core.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.aubade.command.CompositeCommand;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.arcartxsuite.api.aubade.permission.Permission;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.island.IslandManagerImpl;

/**
 * /isadmin delete <玩家> confirm — 强制删除指定玩家的岛屿。
 */
public class AdminDeleteCommand extends CompositeCommand {

  private final AubadeCore core;

  public AdminDeleteCommand(AubadeCore core) {
    super("delete", "强制删除指定玩家的岛屿", Permission.ADMIN_DELETE, false);
    this.core = core;
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    if (!checkPermission(sender)) {
      sender.sendMessage("§c你没有权限使用此命令。");
      return true;
    }
    if (args.length == 0) {
      sender.sendMessage("§c用法: /isadmin delete <玩家> confirm");
      return true;
    }
    if (args.length < 2 || !"confirm".equalsIgnoreCase(args[1])) {
      sender.sendMessage("§c请使用 §e/isadmin delete <玩家> confirm §c确认删除。");
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
    String targetName = target.getName() != null ? target.getName() : args[0];
    manager.deleteIsland(island);
    sender.sendMessage("§a已强制删除 §e" + targetName + " §a的岛屿。");
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
      return result;
    }
    if (args.length == 2 && "confirm".startsWith(args[1].toLowerCase())) {
      result.add("confirm");
    }
    return result;
  }
}
