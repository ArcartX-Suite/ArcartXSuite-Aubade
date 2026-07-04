package xuanmo.aubade.core.command;

import java.util.List;
import java.util.Optional;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.aubade.command.CompositeCommand;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.arcartxsuite.api.aubade.permission.Permission;
import xuanmo.aubade.core.AubadeCore;

/**
 * /island sethome — 设置岛屿出生点。
 */
public class IslandSetHomeCommand extends CompositeCommand {

  private final AubadeCore core;

  public IslandSetHomeCommand(AubadeCore core) {
    super("sethome", "设置岛屿出生点", Permission.PLAYER, true);
    this.core = core;
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    if (!checkPlayer(sender)) {
      sender.sendMessage("§c此命令只能由玩家执行。");
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
    island.setCenter(player.getLocation());
    manager.saveIsland(island);
    player.sendMessage("§a岛屿出生点已设置。");
    return true;
  }

  @Override
  public List<String> tabComplete(CommandSender sender, String[] args) {
    return List.of();
  }
}

