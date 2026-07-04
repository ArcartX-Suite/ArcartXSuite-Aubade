package xuanmo.aubade.core.blueprint;

import java.util.List;
import org.bukkit.Material;

/**
 * 岛屿蓝图。
 * 描述一组方块的相对位置和类型，用于创建初始岛屿地形。
 */
public class Blueprint {

  private final String id;
  private final String name;
  private final String description;
  private final Material icon;
  private final List<BlockEntry> blocks;

  public Blueprint(String id, String name, String description, Material icon, List<BlockEntry> blocks) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.icon = icon;
    this.blocks = blocks;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Material getIcon() {
    return icon;
  }

  public List<BlockEntry> getBlocks() {
    return blocks;
  }

  /**
   * 蓝图中的单一方块记录。
   */
  public record BlockEntry(int x, int y, int z, Material material) {
  }
}

