package xuanmo.aubade.core.command;

import java.util.List;
import java.util.Optional;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.aubade.command.CompositeCommand;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.arcartxsuite.api.aubade.permission.Permission;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.warp.IslandWarpHelper;

/**
 * /island setwarp <名称> — 在当前位置创建传送点。
 */
public class IslandSetWarpCommand extends CompositeCommand {

  private final AubadeCore plugin;

  public IslandSetWarpCommand(AubadeCore plugin) {
    super("setwarp", "创建岛屿传送点", Permission.PLAYER_WARP, true);
    this.plugin = plugin;
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    if (!checkPlayer(sender)) {
      sender.sendMessage("§c此命令只能由玩家执行。");
      return true;
    }
    if (args.length == 0) {
      sender.sendMessage("§c请指定传送点名称。用法: /island setwarp <名称>");
      return true;
    }
    Player player = (Player) sender;
    var manager = plugin.getLifecycleManager().getIslandManager();
    Optional<Island> opt = manager.getIslandByOwner(player.getUniqueId());
    if (opt.isEmpty()) {
      player.sendMessage("§c你还没有岛屿。");
      return true;
    }
    Island island = opt.get();

    // 检查是否在岛屿范围内
    if (!island.inProtectionRange(player.getLocation())) {
      player.sendMessage("§c你只能在自己的岛屿保护范围内设置传送点。");
      return true;
    }

    String warpName = args[0];
    IslandWarpHelper.setWarp(island, warpName, player.getLocation());
    manager.saveIsland(island);
    player.sendMessage("§a传送点 §e" + warpName + " §a已设置在当前位置。");
    return true;
  }

  @Override
  public List<String> tabComplete(CommandSender sender, String[] args) {
    return List.of();
  }
}

