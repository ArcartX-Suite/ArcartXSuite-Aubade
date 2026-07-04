package xuanmo.aubade.core.command;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.aubade.command.CompositeCommand;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.arcartxsuite.api.aubade.permission.Permission;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.warp.IslandWarpHelper;

/**
 * /island delwarp <名称> — 删除岛屿传送点。
 */
public class IslandDelWarpCommand extends CompositeCommand {

  private final AubadeCore core;

  public IslandDelWarpCommand(AubadeCore core) {
    super("delwarp", "删除岛屿传送点", Permission.PLAYER_WARP, true);
    this.core = core;
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    if (!checkPlayer(sender)) {
      sender.sendMessage("§c此命令只能由玩家执行。");
      return true;
    }
    if (args.length == 0) {
      sender.sendMessage("§c请指定要删除的传送点名称。用法: /island delwarp <名称>");
      return true;
    }
    Player player = (Player) sender;
    var manager = core.getLifecycleManager().getIslandManager();
    Optional<Island> opt = manager.getIslandByOwner(player.getUniqueId());
    if (opt.isEmpty()) {
      player.sendMessage("§c你还没有岛屿。");
      return true;
    }
    Island island = opt.get();
    String warpName = args[0];

    Map<String, Location> warps = IslandWarpHelper.getWarps(island);
    if (!warps.containsKey(warpName)) {
      player.sendMessage("§c传送点 §e" + warpName + " §c不存在。");
      return true;
    }

    IslandWarpHelper.removeWarp(island, warpName);
    manager.saveIsland(island);
    player.sendMessage("§a传送点 §e" + warpName + " §a已删除。");
    return true;
  }

  @Override
  public List<String> tabComplete(CommandSender sender, String[] args) {
    if (!(sender instanceof Player)) return List.of();
    if (args.length == 0) {
      var manager = core.getLifecycleManager().getIslandManager();
      var opt = manager.getIslandByOwner(((Player) sender).getUniqueId());
      if (opt.isEmpty()) return List.of();
      return new java.util.ArrayList<>(IslandWarpHelper.getWarps(opt.get()).keySet());
    }
    return List.of();
  }
}

