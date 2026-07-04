package xuanmo.aubade.core.integration;

import java.util.Optional;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.util.VersionAdapter;

public class AubadePlaceholderExpansion extends PlaceholderExpansion {

  private final AubadeCore core;

  public AubadePlaceholderExpansion(AubadeCore core) {
    this.core = core;
  }

  @Override
  public @NotNull String getIdentifier() {
    return "aubade";
  }

  @Override
  public @NotNull String getAuthor() {
    return "xuanmo";
  }

  @Override
  public @NotNull String getVersion() {
    return VersionAdapter.getPluginVersion(core.plugin());
  }

  @Override
  public boolean persist() {
    return true;
  }

  @Override
  public boolean canRegister() {
    return true;
  }

  @Override
  public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
    if (player == null || core.getLifecycleManager() == null) {
      return "";
    }
    Optional<Island> opt = core.getLifecycleManager().getIslandManager().getIslandByOwner(player.getUniqueId());
    Island island = opt.orElse(null);
    switch (identifier.toLowerCase()) {
      case "has_island": return island != null ? "true" : "false";
      case "level": return island != null ? String.valueOf(island.getLevel()) : "0";
      case "name": return island != null && island.getName() != null ? island.getName() : "";
      case "members": return island != null ? String.valueOf(island.getMembers().size()) : "0";
      case "likes": return island != null ? String.valueOf(island.getLikes()) : "0";
      case "bank": return island != null ? String.valueOf(island.getBankBalance()) : "0";
      case "owner": return island != null ? (org.bukkit.Bukkit.getOfflinePlayer(island.getOwner()).getName() != null ? org.bukkit.Bukkit.getOfflinePlayer(island.getOwner()).getName() : "未知") : "无";
      case "range": return island != null ? String.valueOf(island.getProtectionRange()) : "0";
      case "locked": return island != null ? (island.isLocked() ? "已锁定" : "未锁定") : "无";
      default: return null;
    }
  }
}

