package xuanmo.aubade.core.command;

import java.util.List;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.aubade.command.CompositeCommand;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.arcartxsuite.api.aubade.permission.Permission;
import xuanmo.aubade.core.AubadeCore;

/**
 * /island ban <玩家> — 禁止玩家进入岛屿。
 * /island unban <玩家> — 解除禁止。
 */
public class IslandBanCommand extends CompositeCommand {

  private final AubadeCore plugin;
  private final boolean unban;

  public IslandBanCommand(AubadeCore plugin, boolean unban) {
    super(unban ? "unban" : "ban",
        unban ? "解除禁止玩家" : "禁止玩家进入",
        unban ? Permission.PLAYER_UNBAN : Permission.PLAYER_BAN, true);
    this.plugin = plugin;
    this.unban = unban;
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    if (!checkPlayer(sender)) {
      sender.sendMessage("§c此命令只能由玩家执行。");
      return true;
    }
    if (args.length == 0) {
      sender.sendMessage("§c请指定玩家名。用法: /island " + label + " <玩家>");
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
    Player target = Bukkit.getPlayer(args[0]);
    if (target == null) {
      player.sendMessage("§c玩家不在线: " + args[0]);
      return true;
    }
    if (target.equals(player)) {
      player.sendMessage("§c不能对自己执行此操作。");
      return true;
    }

    if (unban) {
      if (island.getBannedPlayers().remove(target.getUniqueId())) {
        manager.saveIsland(island);
        player.sendMessage("§a已解除对 §e" + target.getName() + " §a的禁止。");
      } else {
        player.sendMessage("§c该玩家未被禁止。");
      }
    } else {
      if (island.getBannedPlayers().add(target.getUniqueId())) {
        // 如果目标在线且在岛屿上，踢出
        if (target.isOnline() && island.inProtectionRange(target.getLocation())) {
          target.teleport(target.getWorld().getSpawnLocation());
          target.sendMessage("§c你已被禁止进入该岛屿。");
        }
        manager.saveIsland(island);
        player.sendMessage("§a已禁止 §e" + target.getName() + " §a进入你的岛屿。");
      } else {
        player.sendMessage("§c该玩家已被禁止。");
      }
    }
    return true;
  }

  @Override
  public List<String> tabComplete(CommandSender sender, String[] args) {
    return List.of();
  }
}

