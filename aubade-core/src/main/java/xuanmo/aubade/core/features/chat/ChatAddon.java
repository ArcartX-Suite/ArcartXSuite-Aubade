package xuanmo.aubade.core.features.chat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.AbstractExtensionAddon;

/**
 * 岛屿/团队聊天频道扩展组件。
 * 支持 /island chat toggle 在全局聊天和岛屿聊天间切换。
 */
public class ChatAddon extends AbstractExtensionAddon implements Listener {

  private final Map<UUID, ChatMode> playerChatMode = new HashMap<>();

  public ChatAddon(AubadeCore core) {
    super(core, AddonDescriptor.builder("island_chat")
        .name("岛屿聊天")
        .version("1.0.0")
        .mainClass(ChatAddon.class.getName())
        .build());
  }

  @Override
  public String getExtensionId() {
    return "island_chat";
  }

  @Override
  public String getFriendlyName() {
    return "岛屿聊天";
  }

  @Override
  public void onEnable() {
    super.onEnable();
    Bukkit.getPluginManager().registerEvents(this, javaPlugin());
    core.getLogger().info("[Chat] 岛屿聊天扩展已启用。");
  }

  @Override
  public void onDisable() {
    super.onDisable();
    AsyncPlayerChatEvent.getHandlerList().unregister(this);
    playerChatMode.clear();
    core.getLogger().info("[Chat] 岛屿聊天扩展已禁用。");
  }

  public ChatMode getChatMode(UUID playerId) {
    return playerChatMode.getOrDefault(playerId, ChatMode.GLOBAL);
  }

  public void setChatMode(UUID playerId, ChatMode mode) {
    if (mode == ChatMode.GLOBAL) {
      playerChatMode.remove(playerId);
    } else {
      playerChatMode.put(playerId, mode);
    }
  }

  @EventHandler
  public void onPlayerChat(AsyncPlayerChatEvent event) {
    Player player = event.getPlayer();
    ChatMode mode = getChatMode(player.getUniqueId());
    if (mode == ChatMode.GLOBAL) {
      return;
    }

    Optional<Island> opt = getIslandManager().getIslandByOwner(player.getUniqueId());
    if (opt.isEmpty()) {
      // 没有岛屿却设置了岛屿聊天模式，切回全局
      setChatMode(player.getUniqueId(), ChatMode.GLOBAL);
      return;
    }

    Island island = opt.get();
    event.setCancelled(true);

    String prefix = mode == ChatMode.ISLAND ? "§b[岛屿] " : "§d[团队] ";
    String format = prefix + "§f" + player.getName() + " §7» §f" + event.getMessage();

    // 发送给岛屿所有成员
    for (UUID memberId : island.getMembers().keySet()) {
      Player member = Bukkit.getPlayer(memberId);
      if (member != null && member.isOnline()) {
        member.sendMessage(format);
      }
    }
    // 岛主
    Player owner = Bukkit.getPlayer(island.getOwner());
    if (owner != null && owner.isOnline()) {
      owner.sendMessage(format);
    }

    // 控制台也打印
    core.getLogger().info("[Chat] " + format.replaceAll("§[0-9a-fk-or]", ""));
  }

  public enum ChatMode {
    GLOBAL("全局"),
    ISLAND("岛屿"),
    TEAM("团队");

    private final String displayName;

    ChatMode(String displayName) {
      this.displayName = displayName;
    }

    public String getDisplayName() {
      return displayName;
    }
  }
}
