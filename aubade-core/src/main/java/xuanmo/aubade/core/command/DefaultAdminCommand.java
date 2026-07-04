package xuanmo.aubade.core.command;

import java.util.List;
import org.bukkit.command.CommandSender;
import xuanmo.arcartxsuite.api.aubade.command.CompositeCommand;
import xuanmo.arcartxsuite.api.aubade.permission.Permission;

/**
 * /isadmin 根命令的默认 help 处理器。
 */
public class DefaultAdminCommand extends CompositeCommand {

  public DefaultAdminCommand() {
    super("help", "显示管理命令帮助", Permission.ADMIN, false);
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    if (!checkPermission(sender)) {
      sender.sendMessage("§c你没有权限使用此命令。");
      return true;
    }
    sender.sendMessage("§6========== §eAubade 管理帮助 §6==========");
    sender.sendMessage("§e/isadmin reload §7— 重载插件配置与组件");
    sender.sendMessage("§e/isadmin purge §7— 清理无效岛屿数据");
    sender.sendMessage("§e/isadmin info <玩家> §7— 查看玩家岛屿详情");
    sender.sendMessage("§e/isadmin delete <玩家> §7— 强制删除玩家岛屿");
    sender.sendMessage("§e/isadmin setspawn §7— 设置全局出生点");
    sender.sendMessage("§6=======================================");
    return true;
  }

  @Override
  public List<String> tabComplete(CommandSender sender, String[] args) {
    if (args.length == 0) {
      return List.of("reload", "purge", "info", "delete", "setspawn");
    }
    return List.of();
  }
}

