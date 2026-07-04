package xuanmo.aubade.core.command;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.aubade.command.CompositeCommand;
import xuanmo.arcartxsuite.api.aubade.permission.Permission;
import xuanmo.aubade.core.AubadeCore;

/**
 * /isadmin setspawn — 设置全局出生点。
 */
public class AdminSetSpawnCommand extends CompositeCommand {

  private final AubadeCore core;

  public AdminSetSpawnCommand(AubadeCore core) {
    super("setspawn", "设置全局出生点", Permission.ADMIN_SETTINGS, true);
    this.core = core;
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    if (!checkPlayer(sender)) {
      sender.sendMessage("§c此命令只能由玩家执行。");
      return true;
    }
    if (!checkPermission(sender)) {
      sender.sendMessage("§c你没有权限使用此命令。");
      return true;
    }
    if (core.getCoreConfig() == null) {
      sender.sendMessage("§c当前没有可用的配置对象。");
      return true;
    }

    Player player = (Player) sender;
    Location location = player.getLocation();
    core.getCoreConfig().setGlobalSpawn(location);
    core.getCoreConfig().save();
    sender.sendMessage("§a全局出生点已设置为 §e" + formatLocation(location) + "§a。");
    return true;
  }

  @Override
  public List<String> tabComplete(CommandSender sender, String[] args) {
    return List.of();
  }

  private String formatLocation(Location location) {
    return location.getWorld().getName() + " "
        + String.format("%.1f, %.1f, %.1f, %.1f, %.1f",
        location.getX(), location.getY(), location.getZ(), (double) location.getYaw(), (double) location.getPitch());
  }
}
