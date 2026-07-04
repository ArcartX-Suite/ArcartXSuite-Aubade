package xuanmo.aubade.core.command;

import java.util.List;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.aubade.command.CompositeCommand;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.arcartxsuite.api.aubade.permission.Permission;
import xuanmo.aubade.core.AubadeCore;

/**
 * /island home — 传送到岛屿出生点。
 */
public class IslandHomeCommand extends CompositeCommand {

  private final AubadeCore core;

  public IslandHomeCommand(AubadeCore core) {
    super("home", "返回岛屿出生点", Permission.PLAYER, true);
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
      player.sendMessage("§c你还没有岛屿，使用 §e/island create §c创建一个。");
      return true;
    }
    Location home = opt.get().getCenter();
    home.setY(home.getY() + 1);
    player.teleport(home);
    player.sendMessage("§a正在返回你的岛屿...");
    return true;
  }

  @Override
  public List<String> tabComplete(CommandSender sender, String[] args) {
    return List.of();
  }
}

