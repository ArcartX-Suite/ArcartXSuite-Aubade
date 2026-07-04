package xuanmo.aubade.core;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Legacy Bukkit entrypoint retained for compatibility.
 */
public final class AubadePlugin extends JavaPlugin {

  private static AubadePlugin instance;

  @Override
  public void onEnable() {
    instance = this;
    getLogger().info("Aubade legacy Bukkit entrypoint is disabled in AXS mode.");
  }

  @Override
  public void onDisable() {
    if (instance == this) {
      instance = null;
    }
  }

  public static @NotNull AubadePlugin getInstance() {
    if (instance == null) {
      throw new IllegalStateException("AubadePlugin 尚未初始化");
    }
    return instance;
  }
}

