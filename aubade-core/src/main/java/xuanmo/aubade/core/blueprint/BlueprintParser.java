package xuanmo.aubade.core.blueprint;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.Material;

/**
 * 蓝图 JSON 解析器。
 */
public class BlueprintParser {

  private static final Logger logger = Logger.getLogger("Aubade");
  private static final Gson gson = new Gson();

  /**
   * 从 JSON 文件解析蓝图。
   */
  public static Blueprint parse(File file) {
    try (Reader reader = new FileReader(file, java.nio.charset.StandardCharsets.UTF_8)) {
      JsonObject root = gson.fromJson(reader, JsonObject.class);
      String id = root.has("id") ? root.get("id").getAsString() : file.getName().replace(".json", "");
      String name = root.has("name") ? root.get("name").getAsString() : id;
      String description = root.has("description") ? root.get("description").getAsString() : "";
      Material icon = Material.matchMaterial(root.has("icon") ? root.get("icon").getAsString() : "GRASS_BLOCK");
      if (icon == null) {
        icon = Material.GRASS_BLOCK;
      }

      List<Blueprint.BlockEntry> blocks = new ArrayList<>();
      if (root.has("blocks")) {
        JsonArray arr = root.getAsJsonArray("blocks");
        for (int i = 0; i < arr.size(); i++) {
          JsonObject b = arr.get(i).getAsJsonObject();
          int x = b.has("x") ? b.get("x").getAsInt() : 0;
          int y = b.has("y") ? b.get("y").getAsInt() : 0;
          int z = b.has("z") ? b.get("z").getAsInt() : 0;
          Material mat = Material.matchMaterial(b.has("material") ? b.get("material").getAsString() : "STONE");
          if (mat == null) {
            mat = Material.STONE;
          }
          blocks.add(new Blueprint.BlockEntry(x, y, z, mat));
        }
      }

      return new Blueprint(id, name, description, icon, blocks);
    } catch (IOException e) {
      logger.severe("[蓝图] 解析失败: " + file.getAbsolutePath() + " — " + e.getMessage());
      return createDefaultBlueprint();
    }
  }

  /**
   * 创建默认经典空岛蓝图（一棵树 + 小平台）。
   */
  public static Blueprint createDefaultBlueprint() {
    List<Blueprint.BlockEntry> blocks = new ArrayList<>();

    // 3x3 泥土平台
    for (int x = -1; x <= 1; x++) {
      for (int z = -1; z <= 1; z++) {
        blocks.add(new Blueprint.BlockEntry(x, 0, z, Material.GRASS_BLOCK));
      }
    }

    // 一棵树
    blocks.add(new Blueprint.BlockEntry(0, 1, 0, Material.OAK_LOG));
    blocks.add(new Blueprint.BlockEntry(0, 2, 0, Material.OAK_LOG));
    blocks.add(new Blueprint.BlockEntry(0, 3, 0, Material.OAK_LOG));
    blocks.add(new Blueprint.BlockEntry(0, 4, 0, Material.OAK_LEAVES));
    blocks.add(new Blueprint.BlockEntry(1, 3, 0, Material.OAK_LEAVES));
    blocks.add(new Blueprint.BlockEntry(-1, 3, 0, Material.OAK_LEAVES));
    blocks.add(new Blueprint.BlockEntry(0, 3, 1, Material.OAK_LEAVES));
    blocks.add(new Blueprint.BlockEntry(0, 3, -1, Material.OAK_LEAVES));

    // 箱子
    blocks.add(new Blueprint.BlockEntry(1, 1, 0, Material.CHEST));

    return new Blueprint("default", "经典空岛", "一棵树和一个箱子的经典开局", Material.GRASS_BLOCK, blocks);
  }
}

