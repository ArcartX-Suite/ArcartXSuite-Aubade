package xuanmo.aubade.core.features.magicgen;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.AbstractExtensionAddon;

/**
 * 魔法生成器扩展组件。
 * 拦截 BlockFormEvent（刷石机/刷雪机等），将默认产物替换为配置化的任意方块。
 */
public class MagicGenAddon extends AbstractExtensionAddon implements Listener {

  private final Map<Material, Material> genMappings = new HashMap<>();
  private boolean enabled = true;

  public MagicGenAddon(AubadeCore plugin) {
    super(plugin, AddonDescriptor.builder("magicgen")
        .name("魔法生成器")
        .version("1.0.0")
        .mainClass(MagicGenAddon.class.getName())
        .build());
  }

  @Override
  public String getExtensionId() {
    return "magicgen";
  }

  @Override
  public String getFriendlyName() {
    return "魔法生成器";
  }

  @Override
  public void onEnable() {
    super.onEnable();
    loadDefaultMappings();
    Bukkit.getPluginManager().registerEvents(this, javaPlugin());
    plugin.getLogger().info("[MagicGen] 魔法生成器扩展已启用。");
  }

  @Override
  public void onDisable() {
    super.onDisable();
    BlockFormEvent.getHandlerList().unregister(this);
    plugin.getLogger().info("[MagicGen] 魔法生成器扩展已禁用。");
  }

  private void loadDefaultMappings() {
    genMappings.put(Material.COBBLESTONE, Material.COBBLESTONE); // 默认保持原样
    // 示例：未来可通过配置替换为任意 Material
  }

  @EventHandler
  public void onBlockForm(BlockFormEvent event) {
    if (!enabled) {
      return;
    }
    Block block = event.getBlock();
    Material source = event.getNewState().getType();

    Optional<Island> opt = getIslandManager().getIslandAt(block.getLocation());
    if (opt.isEmpty()) {
      return; // 不在岛屿范围内不处理
    }

    Material target = genMappings.get(source);
    if (target != null && target != source) {
      event.getNewState().setType(target);
    }
  }

  public void setMapping(Material source, Material target) {
    if (target == null) {
      genMappings.remove(source);
    } else {
      genMappings.put(source, target);
    }
  }

  public Map<Material, Material> getMappings() {
    return new HashMap<>(genMappings);
  }
}
