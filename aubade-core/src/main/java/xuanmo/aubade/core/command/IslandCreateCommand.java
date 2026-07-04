package xuanmo.aubade.core.command;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.aubade.command.CompositeCommand;
import xuanmo.arcartxsuite.api.aubade.permission.Permission;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.island.IslandManagerImpl;

/**
 * /island create [blueprint] — 创建新岛屿。
 */
public class IslandCreateCommand extends CompositeCommand {

  private final AubadeCore plugin;

  public IslandCreateCommand(AubadeCore plugin) {
    super("create", "创建新岛屿", Permission.PLAYER_CREATE, true);
    this.plugin = plugin;
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    if (!checkPlayer(sender)) {
      sender.sendMessage("§c此命令只能由玩家执行。");
      return true;
    }
    Player player = (Player) sender;
    IslandManagerImpl manager = getIslandManager();
    if (manager == null) {
      player.sendMessage("§c岛屿管理器尚未初始化。");
      return true;
    }

    String blueprintId = args.length > 0 ? args[0] : "default";
    var island = manager.createIsland(player, plugin.getLifecycleManager().getCoreConfig().getDefaultGameMode(), blueprintId);
    if (island != null) {
      player.teleport(island.getCenter().clone().add(0, 1, 0));
      player.sendMessage("§a岛屿创建成功！使用 §e/island home §a返回。");
    }
    return true;
  }

  @Override
  public List<String> tabComplete(CommandSender sender, String[] args) {
    if (args.length == 1) {
      var addon = plugin.getLifecycleManager().getAddonLifecycleManager().getExtension("blueprint_generator");
      if (addon instanceof xuanmo.aubade.core.features.blueprint.BlueprintGeneratorAddon gen
          && gen.getRegistry() != null) {
        return gen.getRegistry().getBlueprintIds();
      }
      return List.of("default");
    }
    return List.of();
  }

  private IslandManagerImpl getIslandManager() {
    return plugin.getLifecycleManager().getIslandManager();
  }
}

