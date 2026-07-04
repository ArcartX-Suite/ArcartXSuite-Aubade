package xuanmo.aubade.core.island;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * 岛屿网格计算器。
 * 负责将岛屿索引转换为世界坐标，并维护间距保护。
 */
public class IslandGrid {

  private final int islandSpacing;
  private final AtomicInteger nextIndex = new AtomicInteger(0);

  public IslandGrid(int islandSpacing) {
    this.islandSpacing = islandSpacing;
  }

  /**
   * 分配下一个可用的网格索引。
   */
  public int nextIndex() {
    return nextIndex.getAndIncrement();
  }

  /**
   * 根据网格索引计算世界坐标。
   * 采用螺旋展开方式：0→(0,0), 1→(1,0), 2→(1,1), 3→(0,1), 4→(-1,1), ...
   *
   * @param index 网格索引
   * @return 中心点坐标 (x, z)
   */
  public int[] getCenter(int index) {
    if (index == 0) {
      return new int[]{0, 0};
    }

    int layer = (int) Math.ceil((Math.sqrt(index + 1) - 1) / 2);
    int sideLen = layer * 2 + 1;
    int maxIdx = sideLen * sideLen - 1;
    int minIdx = (sideLen - 2) * (sideLen - 2);
    int offset = index - minIdx;

    int x, z;
    int perimeter = sideLen - 1;
    if (offset < perimeter) {
      // 右边
      x = layer;
      z = -layer + 1 + offset;
    } else if (offset < perimeter * 2) {
      // 上边
      x = layer - 1 - (offset - perimeter);
      z = layer;
    } else if (offset < perimeter * 3) {
      // 左边
      x = -layer;
      z = layer - 1 - (offset - perimeter * 2);
    } else {
      // 下边
      x = -layer + 1 + (offset - perimeter * 3);
      z = -layer;
    }

    return new int[]{x * islandSpacing, z * islandSpacing};
  }

  /**
   * 创建岛屿中心位置。
   */
  public Location createLocation(World world, int index) {
    int[] center = getCenter(index);
    return new Location(world, center[0], 128, center[1]);
  }

  /**
   * 设置下一个索引（从数据库加载已有岛屿后使用）。
   */
  public void setNextIndex(int index) {
    nextIndex.set(index);
  }
}

