package xuanmo.aubade.game.skyblock.command;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.aubade.command.CompositeCommand;
import xuanmo.arcartxsuite.api.aubade.world.WorldSettings;
import xuanmo.aubade.game.skyblock.SkyBlockAddon;

public class SkyBlockCommand extends CompositeCommand {

  private final SkyBlockAddon addon;

  public SkyBlockCommand(SkyBlockAddon addon) {
    super("skyblock", "skyblock info", "aubade.player.skyblock", true);
    this.addon = addon;
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    if (!checkPlayer(sender) || !checkPermission(sender)) {
      return false;
    }
    Player player = (Player) sender;
    WorldSettings worldSettings = addon.getWorldSettings();
    player.sendMessage("§a经典空岛模式信息");
    player.sendMessage("§7模式: §f" + worldSettings.getFriendlyName());
    player.sendMessage("§7世界名: §f" + worldSettings.getWorldName());
    player.sendMessage("§7保护范围: §f" + worldSettings.getDefaultProtectionRange());
    player.sendMessage("§7岛屿间距: §f" + worldSettings.getIslandSpacing());
    player.sendMessage("§7海平面: §f" + worldSettings.getSeaLevel());
    return true;
  }

  @Override
  public List<String> tabComplete(CommandSender sender, String[] args) {
    return List.of();
  }
}