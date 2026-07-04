package xuanmo.aubade.core.features.twerkingfortrees;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.AbstractExtensionAddon;

/**
 * 抖臀催树扩展组件。
 * 玩家在树苗附近潜行时，有几率立即催熟树苗生长为树木。
 */
public class TwerkingForTreesAddon extends AbstractExtensionAddon implements Listener {

  private static final Set<Material> SAPLINGS = EnumSet.of(
      Material.OAK_SAPLING, Material.SPRUCE_SAPLING, Material.BIRCH_SAPLING,
      Material.JUNGLE_SAPLING, Material.ACACIA_SAPLING, Material.DARK_OAK_SAPLING,
      Material.AZALEA, Material.FLOWERING_AZALEA, Material.CHERRY_SAPLING,
      Material.MANGROVE_PROPAGULE
  );

  private final Random random = new Random();
  private int radius = 4;
  private double chance = 0.3;

  public TwerkingForTreesAddon(AubadeCore core) {
    super(core, AddonDescriptor.builder("twerkingfortrees")
        .name("抖臀催树")
        .version("1.0.0")
        .mainClass(TwerkingForTreesAddon.class.getName())
        .build());
  }

  @Override
  public String getExtensionId() {
    return "twerkingfortrees";
  }

  @Override
  public String getFriendlyName() {
    return "抖臀催树";
  }

  @Override
  public void onEnable() {
    super.onEnable();
    Bukkit.getPluginManager().registerEvents(this, javaPlugin());
    core.getLogger().info("[TwerkingForTrees] 抖臀催树扩展已启用。");
  }

  @Override
  public void onDisable() {
    super.onDisable();
    PlayerToggleSneakEvent.getHandlerList().unregister(this);
    core.getLogger().info("[TwerkingForTrees] 抖臀催树扩展已禁用。");
  }

  @EventHandler
  public void onSneak(PlayerToggleSneakEvent event) {
    if (!event.isSneaking()) {
      return;
    }
    Player player = event.getPlayer();
    Optional<Island> opt = getIslandManager().getIslandAt(player.getLocation());
    if (opt.isEmpty()) {
      return;
    }
    Island island = opt.get();
    if (!island.hasPermission(player.getUniqueId(), xuanmo.arcartxsuite.api.aubade.island.IslandPermission.PLACE)) {
      return;
    }

    Block center = player.getLocation().getBlock();
    for (int dx = -radius; dx <= radius; dx++) {
      for (int dy = -2; dy <= 1; dy++) {
        for (int dz = -radius; dz <= radius; dz++) {
          Block block = center.getRelative(dx, dy, dz);
          if (!SAPLINGS.contains(block.getType())) {
            continue;
          }
          if (random.nextDouble() <= chance) {
            boolean grown = applyBonemeal(block);
            if (grown) {
              player.sendActionBar(net.kyori.adventure.text.Component.text("§a✨ 附近的树苗受到了你的鼓舞！"));
              return; // 每次潜行只触发一次
            }
          }
        }
      }
    }
  }

  private boolean applyBonemeal(Block sapling) {
    // 模拟骨粉效果：触发 StructureGrowEvent 或使用 World#generateTree
    Material type = sapling.getType();
    return switch (type) {
      case OAK_SAPLING -> sapling.getWorld().generateTree(sapling.getLocation(), org.bukkit.TreeType.TREE);
      case SPRUCE_SAPLING -> sapling.getWorld().generateTree(sapling.getLocation(), org.bukkit.TreeType.REDWOOD);
      case BIRCH_SAPLING -> sapling.getWorld().generateTree(sapling.getLocation(), org.bukkit.TreeType.BIRCH);
      case JUNGLE_SAPLING -> sapling.getWorld().generateTree(sapling.getLocation(), org.bukkit.TreeType.SMALL_JUNGLE);
      case ACACIA_SAPLING -> sapling.getWorld().generateTree(sapling.getLocation(), org.bukkit.TreeType.ACACIA);
      case DARK_OAK_SAPLING -> sapling.getWorld().generateTree(sapling.getLocation(), org.bukkit.TreeType.DARK_OAK);
      case CHERRY_SAPLING -> sapling.getWorld().generateTree(sapling.getLocation(), org.bukkit.TreeType.CHERRY);
      case MANGROVE_PROPAGULE -> sapling.getWorld().generateTree(sapling.getLocation(), org.bukkit.TreeType.TALL_MANGROVE);
      default -> false;
    };
  }
}
