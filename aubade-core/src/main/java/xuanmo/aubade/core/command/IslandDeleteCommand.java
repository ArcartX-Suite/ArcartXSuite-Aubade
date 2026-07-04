package xuanmo.aubade.core.command;

import java.util.List;
import java.util.Optional;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.aubade.command.CompositeCommand;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.arcartxsuite.api.aubade.permission.Permission;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.island.IslandManagerImpl;

/**
 * /island delete [confirm] — 删除当前岛屿。
 */
public class IslandDeleteCommand extends CompositeCommand {

  private final AubadeCore core;

  public IslandDeleteCommand(AubadeCore core) {
    super("delete", "删除当前岛屿", Permission.PLAYER_DELETE, true);
    this.core = core;
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    if (!checkPlayer(sender)) {
      sender.sendMessage("§c此命令只能由玩家执行。");
      return true;
    }
    Player player = (Player) sender;
    IslandManagerImpl manager = core.getLifecycleManager().getIslandManager();
    Optional<Island> opt = manager.getIslandByOwner(player.getUniqueId());
    if (opt.isEmpty()) {
      player.sendMessage("§c你还没有岛屿。");
      return true;
    }
    if (args.length == 0 || !"confirm".equalsIgnoreCase(args[0])) {
      player.sendMessage("§c请使用 §e/island delete confirm §c确认删除。");
      return true;
    }
    manager.deleteIsland(opt.get());
    player.sendMessage("§a岛屿已删除。");
    return true;
  }

  @Override
  public List<String> tabComplete(CommandSender sender, String[] args) {
    if (args.length == 0) {
      return List.of("confirm");
    }
    return List.of();
  }
}

