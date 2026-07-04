package xuanmo.aubade.core.command;

import java.util.List;
import org.bukkit.command.CommandSender;
import xuanmo.arcartxsuite.api.aubade.command.CompositeCommand;
import xuanmo.arcartxsuite.api.aubade.permission.Permission;
import xuanmo.aubade.core.AubadeCore;

/**
 * /isadmin reload — 重载配置与组件。
 */
public class AdminReloadCommand extends CompositeCommand {

  private final AubadeCore core;

  public AdminReloadCommand(AubadeCore core) {
    super("reload", "重载配置与组件", Permission.ADMIN_RELOAD, false);
    this.core = core;
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    if (!checkPermission(sender)) {
      sender.sendMessage("§c你没有权限使用此命令。");
      return true;
    }
    if (core.getLifecycleManager() == null) {
      sender.sendMessage("§c当前没有可用的生命周期管理器。");
      return true;
    }
    core.getLifecycleManager().onReload();
    sender.sendMessage("§a配置与组件已重载。");
    return true;
  }

  @Override
  public List<String> tabComplete(CommandSender sender, String[] args) {
    return List.of();
  }
}
