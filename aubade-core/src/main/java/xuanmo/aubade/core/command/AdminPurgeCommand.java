package xuanmo.aubade.core.command;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import xuanmo.arcartxsuite.api.aubade.command.CompositeCommand;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.arcartxsuite.api.aubade.permission.Permission;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.island.IslandManagerImpl;

/**
 * /isadmin purge — 清理无效岛屿数据。
 */
public class AdminPurgeCommand extends CompositeCommand {

  private final AubadeCore core;

  public AdminPurgeCommand(AubadeCore core) {
    super("purge", "清理无效岛屿数据", Permission.ADMIN_PURGE, false);
    this.core = core;
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    if (!checkPermission(sender)) {
      sender.sendMessage("§c你没有权限使用此命令。");
      return true;
    }
    IslandManagerImpl manager = core.getLifecycleManager() != null ? core.getLifecycleManager().getIslandManager() : null;
    if (manager == null) {
      sender.sendMessage("§c当前没有可用的岛屿管理器。");
      return true;
    }

    int removed = 0;
    int skippedProtected = 0;
    for (Island island : manager.getCachedIslands()) {
      if (island == null) {
        continue;
      }
      if (island.isPurgeProtected()) {
        skippedProtected++;
        continue;
      }
      if (isOrphanIsland(island)) {
        manager.deleteIsland(island);
        removed++;
      }
    }
    sender.sendMessage("§a清理完成：删除 §e" + removed + " §a个孤立岛屿，跳过 §e" + skippedProtected + " §a个受保护岛屿。");
    return true;
  }

  @Override
  public List<String> tabComplete(CommandSender sender, String[] args) {
    return List.of();
  }

  private boolean isOrphanIsland(Island island) {
    if (island.getOwner() == null) {
      return true;
    }
    OfflinePlayer owner = Bukkit.getOfflinePlayer(island.getOwner());
    return !owner.isOnline() && !owner.hasPlayedBefore();
  }
}
