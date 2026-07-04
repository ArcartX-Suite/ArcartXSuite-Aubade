package xuanmo.aubade.core.features.dimensionaltrees;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.AbstractExtensionAddon;

/**
 * 维度树木扩展组件。
 * 在下界/末地种植树苗时，生成特殊材料掉落物（如黑曜石、荧石粉等）。
 */
public class DimensionalTreesAddon extends AbstractExtensionAddon implements Listener {

  private final Random random = new Random();
  private final Map<String, Material[]> dimensionDrops = new HashMap<>();

  public DimensionalTreesAddon(AubadeCore plugin) {
    super(plugin, AddonDescriptor.builder("dimensionaltrees")
        .name("维度树木")
        .version("1.0.0")
        .mainClass(DimensionalTreesAddon.class.getName())
        .build());
  }

  @Override
  public String getExtensionId() {
    return "dimensionaltrees";
  }

  @Override
  public String getFriendlyName() {
    return "维度树木";
  }

  @Override
  public void onEnable() {
    super.onEnable();
    loadDefaultDrops();
    Bukkit.getPluginManager().registerEvents(this, javaPlugin());
    plugin.getLogger().info("[DimensionalTrees] 维度树木扩展已启用。");
  }

  @Override
  public void onDisable() {
    super.onDisable();
    StructureGrowEvent.getHandlerList().unregister(this);
    plugin.getLogger().info("[DimensionalTrees] 维度树木扩展已禁用。");
  }

  private void loadDefaultDrops() {
    dimensionDrops.put("world_nether",
        new Material[]{Material.OBSIDIAN, Material.GLOWSTONE_DUST, Material.BLAZE_POWDER, Material.NETHER_WART});
    dimensionDrops.put("world_the_end",
        new Material[]{Material.ENDER_PEARL, Material.CHORUS_FRUIT, Material.DRAGON_BREATH, Material.END_STONE});
  }

  @EventHandler
  public void onTreeGrow(StructureGrowEvent event) {
    World world = event.getWorld();
    String worldName = world.getName();

    Material[] drops = dimensionDrops.get(worldName);
    if (drops == null) {
      return; // 只在特殊维度生效
    }

    Optional<Island> opt = getIslandManager().getIslandAt(event.getLocation());
    if (opt.isEmpty()) {
      return;
    }

    // 在树生长位置附近随机掉落特殊物品
    Block base = event.getLocation().getBlock();
    int dropCount = 1 + random.nextInt(3);
    for (int i = 0; i < dropCount; i++) {
      Material drop = drops[random.nextInt(drops.length)];
      int amount = 1 + random.nextInt(2);
      world.dropItemNaturally(base.getLocation().add(0.5, 1, 0.5), new ItemStack(drop, amount));
    }
  }

  public void setDimensionDrops(String worldName, Material[] drops) {
    if (drops == null || drops.length == 0) {
      dimensionDrops.remove(worldName);
    } else {
      dimensionDrops.put(worldName, drops);
    }
  }
}
