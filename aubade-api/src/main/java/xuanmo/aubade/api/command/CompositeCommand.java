package xuanmo.aubade.api.command;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 复合命令基类。
 * 参考 BentoBox CompositeCommand 设计。
 */
public abstract class CompositeCommand {

  protected final String label;
  protected final String description;
  protected final String permission;
  protected final boolean playerOnly;

  protected CompositeCommand(String label, String description, String permission, boolean playerOnly) {
    this.label = label;
    this.description = description;
    this.permission = permission;
    this.playerOnly = playerOnly;
  }

  public abstract boolean execute(CommandSender sender, String[] args);

  public abstract List<String> tabComplete(CommandSender sender, String[] args);

  public String getLabel() {
    return label;
  }

  public String getDescription() {
    return description;
  }

  public String getPermission() {
    return permission;
  }

  public boolean isPlayerOnly() {
    return playerOnly;
  }

  protected boolean checkPermission(CommandSender sender) {
    if (permission == null || permission.isEmpty()) {
      return true;
    }
    return sender.hasPermission(permission);
  }

  protected boolean checkPlayer(CommandSender sender) {
    if (!playerOnly) {
      return true;
    }
    return sender instanceof Player;
  }
}
