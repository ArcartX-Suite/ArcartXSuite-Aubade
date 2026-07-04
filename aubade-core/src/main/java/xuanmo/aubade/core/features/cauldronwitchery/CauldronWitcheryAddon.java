package xuanmo.aubade.core.features.cauldronwitchery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.AbstractExtensionAddon;

/**
 * 坩埚巫术扩展组件。
 * 玩家向坩埚中投掷特定物品组合，召唤生物或获得效果。
 */
public class CauldronWitcheryAddon extends AbstractExtensionAddon implements Listener {

  private final Map<RecipeKey, CauldronRecipe> recipes = new HashMap<>();

  public CauldronWitcheryAddon(AubadeCore plugin) {
    super(plugin, AddonDescriptor.builder("cauldronwitchery")
        .name("坩埚巫术")
        .version("1.0.0")
        .mainClass(CauldronWitcheryAddon.class.getName())
        .build());
  }

  @Override
  public String getExtensionId() {
    return "cauldronwitchery";
  }

  @Override
  public String getFriendlyName() {
    return "坩埚巫术";
  }

  @Override
  public void onEnable() {
    super.onEnable();
    loadDefaultRecipes();
    Bukkit.getPluginManager().registerEvents(this, javaPlugin());
    plugin.getLogger().info("[CauldronWitchery] 坩埚巫术扩展已启用。");
  }

  @Override
  public void onDisable() {
    super.onDisable();
    PlayerInteractEvent.getHandlerList().unregister(this);
    plugin.getLogger().info("[CauldronWitchery] 坩埚巫术扩展已禁用。");
  }

  private void loadDefaultRecipes() {
    registerRecipe(new RecipeKey(Material.BONE, Material.ROTTEN_FLESH),
        new CauldronRecipe(EntityType.ZOMBIE, null, 0, "§7一股腐臭之气涌出..."));
    registerRecipe(new RecipeKey(Material.BLAZE_POWDER, Material.NETHER_WART),
        new CauldronRecipe(EntityType.BLAZE, null, 0, "§6火焰在坩埚中燃烧！"));
    registerRecipe(new RecipeKey(Material.GLOWSTONE_DUST, Material.REDSTONE),
        new CauldronRecipe(null, PotionEffectType.SPEED, 200, "§b你感到身轻如燕！"));
    registerRecipe(new RecipeKey(Material.DIAMOND, Material.EMERALD),
        new CauldronRecipe(null, PotionEffectType.LUCK, 600, "§a幸运女神向你微笑！"));
  }

  public void registerRecipe(RecipeKey key, CauldronRecipe recipe) {
    recipes.put(key, recipe);
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    if (!event.getAction().isRightClick()) {
      return;
    }
    Block block = event.getClickedBlock();
    if (block == null || block.getType() != Material.CAULDRON) {
      return;
    }

    Player player = event.getPlayer();
    ItemStack hand = event.getItem();
    if (hand == null || hand.getType().isAir()) {
      return;
    }

    Optional<Island> opt = getIslandManager().getIslandAt(block.getLocation());
    if (opt.isEmpty()) {
      return;
    }
    Island island = opt.get();
    if (!island.hasPermission(player.getUniqueId(), xuanmo.arcartxsuite.api.aubade.island.IslandPermission.PLACE)) {
      return;
    }

    // 检查坩埚周围地面上是否有另一个物品
    Material handType = hand.getType();
    Material groundItem = findGroundItem(block);
    if (groundItem == null || groundItem == Material.AIR) {
      return;
    }

    RecipeKey key = new RecipeKey(handType, groundItem);
    CauldronRecipe recipe = recipes.get(key);
    if (recipe == null) {
      // 尝试反向匹配
      key = new RecipeKey(groundItem, handType);
      recipe = recipes.get(key);
    }

    if (recipe != null) {
      event.setCancelled(true);
      executeRecipe(player, block, recipe);
      hand.setAmount(hand.getAmount() - 1);
      removeGroundItem(block);
    }
  }

  private Material findGroundItem(Block cauldron) {
    for (BlockFace face : BlockFace.values()) {
      if (face == BlockFace.UP || face == BlockFace.DOWN) {
        continue;
      }
      Block neighbor = cauldron.getRelative(face);
      // 简化为检查邻居方块是否是可识别的"祭品"方块
      // 实际场景中可以用掉落物实体，这里简化为方块检测
      Material type = neighbor.getType();
      if (type != Material.AIR && type != Material.CAULDRON && type.isItem()) {
        return type;
      }
    }
    return null;
  }

  private void removeGroundItem(Block cauldron) {
    for (BlockFace face : BlockFace.values()) {
      if (face == BlockFace.UP || face == BlockFace.DOWN) {
        continue;
      }
      Block neighbor = cauldron.getRelative(face);
      if (neighbor.getType() != Material.AIR && neighbor.getType() != Material.CAULDRON) {
        neighbor.setType(Material.AIR);
        break;
      }
    }
  }

  private void executeRecipe(Player player, Block cauldron, CauldronRecipe recipe) {
    if (recipe.entityType != null) {
      cauldron.getWorld().spawnEntity(cauldron.getLocation().add(0.5, 1, 0.5), recipe.entityType);
    }
    if (recipe.effectType != null && recipe.duration > 0) {
      player.addPotionEffect(new PotionEffect(recipe.effectType, recipe.duration, 1));
    }
    if (recipe.message != null) {
      player.sendMessage(recipe.message);
    }
  }

  public record RecipeKey(Material item1, Material item2) {
  }

  public record CauldronRecipe(EntityType entityType, PotionEffectType effectType,
                                int duration, String message) {
  }
}
