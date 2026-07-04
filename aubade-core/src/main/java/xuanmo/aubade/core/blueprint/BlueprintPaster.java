package xuanmo.aubade.core.blueprint;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import xuanmo.aubade.core.AubadeCore;

/**
 * 蓝图异步粘贴器。
 * 在服务器主线程安全地放置方块。
 */
public class BlueprintPaster {

  private final AubadeCore core;

  public BlueprintPaster(AubadeCore core) {
    this.core = core;
  }

  /**
   * 同步粘贴蓝图到指定中心位置。
   *
   * @param blueprint 蓝图
   * @param center    中心位置（y 为基岩层）
   */
  public void pasteSync(Blueprint blueprint, Location center) {
    World world = center.getWorld();
    if (world == null) {
      return;
    }
    for (Blueprint.BlockEntry entry : blueprint.getBlocks()) {
      Block block = world.getBlockAt(center.getBlockX() + entry.x(), center.getBlockY() + entry.y(), center.getBlockZ() + entry.z());
      block.setType(entry.material(), false);
    }
  }

  /**
   * 异步粘贴蓝图（分 tick 执行，避免卡顿）。
   *
   * @param blueprint 蓝图
   * @param center    中心位置
   * @param blocksPerTick 每 tick 放置的方块数
   */
  public void pasteAsync(Blueprint blueprint, Location center, int blocksPerTick) {
    new BukkitRunnable() {
      int index = 0;
      final java.util.List<Blueprint.BlockEntry> blocks = blueprint.getBlocks();

      @Override
      public void run() {
        World world = center.getWorld();
        if (world == null) {
          cancel();
          return;
        }
        int placed = 0;
        while (index < blocks.size() && placed < blocksPerTick) {
          Blueprint.BlockEntry entry = blocks.get(index++);
          Block block = world.getBlockAt(center.getBlockX() + entry.x(), center.getBlockY() + entry.y(), center.getBlockZ() + entry.z());
          block.setType(entry.material(), false);
          placed++;
        }
        if (index >= blocks.size()) {
          cancel();
        }
      }
    }.runTaskTimer(core.plugin(), 0L, 1L);
  }
}
