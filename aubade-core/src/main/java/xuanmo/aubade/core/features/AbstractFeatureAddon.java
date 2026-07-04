package xuanmo.aubade.core.features;

import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.arcartxsuite.api.aubade.addon.FeatureAddon;
import xuanmo.arcartxsuite.api.aubade.command.CommandManager;
import xuanmo.arcartxsuite.api.aubade.island.IslandManager;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.lifecycle.CoreLifecycleManager;
import xuanmo.aubade.core.ui.UiManager;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractFeatureAddon implements FeatureAddon {

  protected final AubadeCore plugin;
  protected final AddonDescriptor descriptor;
  protected boolean enabled = false;

  public AbstractFeatureAddon(AubadeCore plugin, AddonDescriptor descriptor) {
    this.plugin = plugin;
    this.descriptor = descriptor;
  }

  @Override
  public AddonDescriptor descriptor() {
    return descriptor;
  }

  @Override
  public void onLoad() {
  }

  @Override
  public void onEnable() {
    this.enabled = true;
  }

  @Override
  public void onDisable() {
    this.enabled = false;
  }

  @Override
  public void onReload() {
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  public AubadeCore getPlugin() {
    return plugin;
  }

  public CoreLifecycleManager getLifecycleManager() {
    return plugin.getLifecycleManager();
  }

  public IslandManager getIslandManager() {
    return plugin.getIslandManager();
  }

  public CommandManager getCommandManager() {
    return plugin.getCommandManager();
  }

  public UiManager getUiManager() {
    return plugin.getUiManager();
  }

  protected JavaPlugin javaPlugin() {
    return plugin.plugin();
  }
}
