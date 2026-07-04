package xuanmo.aubade.core.command;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.aubade.command.CompositeCommand;
import xuanmo.arcartxsuite.api.aubade.permission.Permission;
import xuanmo.aubade.core.AubadeCore;

/**
 * /island accept — 接受岛屿邀请。
 */
public class IslandAcceptCommand extends CompositeCommand {

  private final AubadeCore core;

  public IslandAcceptCommand(AubadeCore core) {
    super("accept", "接受岛屿邀请", Permission.PLAYER, true);
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
    if (manager.acceptInvite(player)) {
      player.sendMessage("§a你已加入岛屿！");
    }
    return true;
  }

  @Override
  public List<String> tabComplete(CommandSender sender, String[] args) {
    return List.of();
  }
}

