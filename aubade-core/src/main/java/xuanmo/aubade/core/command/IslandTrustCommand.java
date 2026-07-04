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
 * /island trust <玩家> — 将玩家设为受信任访客。
 * /island untrust <玩家> — 取消信任。
 */
public class IslandTrustCommand extends CompositeCommand {

  private final AubadeCore plugin;
  private final boolean untrust;

  public IslandTrustCommand(AubadeCore plugin, boolean untrust) {
    super(untrust ? "untrust" : "trust",
        untrust ? "取消信任玩家" : "信任玩家",
        untrust ? Permission.PLAYER_UNTRUST : Permission.PLAYER_TRUST, true);
    this.plugin = plugin;
    this.untrust = untrust;
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

    if (untrust) {
      island.getTrustedPlayers().remove(target.getUniqueId());
      manager.saveIsland(island);
      player.sendMessage("§a已取消对 §e" + target.getName() + " §a的信任。");
    } else {
      island.getTrustedPlayers().add(target.getUniqueId());
      manager.saveIsland(island);
      player.sendMessage("§a已信任 §e" + target.getName() + "§a，该玩家现在可以在你的岛屿上自由交互。");
    }
    return true;
  }

  @Override
  public List<String> tabComplete(CommandSender sender, String[] args) {
    return List.of();
  }
}

