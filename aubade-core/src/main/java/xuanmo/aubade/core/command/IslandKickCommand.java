package xuanmo.aubade.core.command;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.aubade.command.CompositeCommand;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.arcartxsuite.api.aubade.permission.Permission;
import xuanmo.aubade.core.AubadeCore;

/**
 * /island kick <player> — 踢出岛屿成员。
 */
public class IslandKickCommand extends CompositeCommand {

  private final AubadeCore plugin;

  public IslandKickCommand(AubadeCore plugin) {
    super("kick", "踢出岛屿成员", Permission.PLAYER, true);
    this.plugin = plugin;
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    if (!checkPlayer(sender)) {
      sender.sendMessage("§c此命令只能由玩家执行。");
      return true;
    }
    if (args.length == 0) {
      sender.sendMessage("§c用法: /island kick <玩家>");
      return true;
    }
    Player player = (Player) sender;
    Player target = Bukkit.getPlayer(args[0]);
    if (target == null) {
      player.sendMessage("§c玩家不在线。");
      return true;
    }
    var manager = plugin.getLifecycleManager().getIslandManager();
    Optional<Island> opt = manager.getIslandByOwner(player.getUniqueId());
    if (opt.isEmpty()) {
      player.sendMessage("§c你还没有岛屿。");
      return true;
    }
    Island island = opt.get();
    if (!island.getOwner().equals(player.getUniqueId())) {
      player.sendMessage("§c只有岛主才能踢出成员。");
      return true;
    }
    if (manager.kickMember(island, target.getUniqueId())) {
      player.sendMessage("§a已将 §e" + target.getName() + " §a移出岛屿。");
      target.sendMessage("§c你已被移出岛屿。");
    } else {
      player.sendMessage("§c无法踢出该玩家。");
    }
    return true;
  }

  @Override
  public List<String> tabComplete(CommandSender sender, String[] args) {
    if (args.length == 1) {
      return Bukkit.getOnlinePlayers().stream()
          .map(Player::getName)
          .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
          .toList();
    }
    return List.of();
  }
}

