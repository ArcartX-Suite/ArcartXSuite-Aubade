package xuanmo.aubade.core.blueprint;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 蓝图注册表。
 * 负责从 blueprints/ 目录加载和管理所有可用蓝图。
 */
public class BlueprintRegistry {

  private final Logger logger;
  private final Map<String, Blueprint> blueprints = new HashMap<>();
  private final File blueprintsDir;

  public BlueprintRegistry(Logger logger, File dataFolder) {
    this.logger = logger;
    this.blueprintsDir = new File(dataFolder, "blueprints");
    if (!blueprintsDir.exists()) {
      blueprintsDir.mkdirs();
    }
  }

  /**
   * 同步加载所有蓝图文件。适合启动时调用。
   */
  public void loadAll() {
    blueprints.clear();

    Blueprint defaultBp = BlueprintParser.createDefaultBlueprint();
    blueprints.put(defaultBp.getId(), defaultBp);

    File[] files = blueprintsDir.listFiles((dir, name)
        -> name.endsWith(".json") || name.endsWith(".yml") || name.endsWith(".yaml"));
    if (files == null || files.length == 0) {
      logger.info("[蓝图] 未发现自定义蓝图文件，仅使用默认蓝图。");
      return;
    }

    for (File file : files) {
      Blueprint bp = loadFromFile(file);
      if (bp != null) {
        blueprints.put(bp.getId(), bp);
        logger.info("[蓝图] 已加载蓝图: " + bp.getId() + " (" + bp.getName() + ", " + bp.getBlocks().size() + " 方块)");
      }
    }
    logger.info("[蓝图] 共加载 " + blueprints.size() + " 个蓝图。");
  }

  /**
   * 异步加载所有蓝图文件。适合重载或运行时调用，避免阻塞主线程。
   */
  public CompletableFuture<Void> loadAllAsync(JavaPlugin plugin) {
    return CompletableFuture.runAsync(() -> {
      blueprints.clear();

      Blueprint defaultBp = BlueprintParser.createDefaultBlueprint();
      blueprints.put(defaultBp.getId(), defaultBp);

      File[] files = blueprintsDir.listFiles((dir, name)
          -> name.endsWith(".json") || name.endsWith(".yml") || name.endsWith(".yaml"));
      if (files == null || files.length == 0) {
        Bukkit.getScheduler().runTask(plugin, () ->
            logger.info("[蓝图] 未发现自定义蓝图文件，仅使用默认蓝图。"));
        return;
      }

      int success = 0;
      for (File file : files) {
        Blueprint bp = loadFromFile(file);
        if (bp != null) {
          blueprints.put(bp.getId(), bp);
          success++;
        }
      }
      final int finalSuccess = success;
      Bukkit.getScheduler().runTask(plugin, () ->
          logger.info("[蓝图] 异步加载完成，共 " + blueprints.size() + " 个蓝图（成功 " + finalSuccess + " 个）。"));
    });
  }

  private Blueprint loadFromFile(File file) {
    String name = file.getName().toLowerCase();
    try {
      if (name.endsWith(".json")) {
        return BlueprintParser.parse(file);
      } else {
        return parseYaml(file);
      }
    } catch (Exception e) {
      logger.warning("[蓝图] 加载失败: " + file.getName() + " — " + e.getMessage());
      return null;
    }
  }

  private Blueprint parseYaml(File file) {
    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
    String id = config.getString("id", file.getName().replaceAll("\\.(yml|yaml)$", ""));
    String displayName = config.getString("name", id);
    String description = config.getString("description", "");
    Material icon = Material.matchMaterial(config.getString("icon", "GRASS_BLOCK"));
    if (icon == null) {
      icon = Material.GRASS_BLOCK;
    }

    List<Blueprint.BlockEntry> blocks = new ArrayList<>();
    List<Map<?, ?>> blockList = config.getMapList("blocks");
    for (Map<?, ?> map : blockList) {
      int x = getInt(map, "x");
      int y = getInt(map, "y");
      int z = getInt(map, "z");
      Material mat = Material.matchMaterial(String.valueOf(map.get("material")));
      if (mat == null) {
        mat = Material.STONE;
      }
      blocks.add(new Blueprint.BlockEntry(x, y, z, mat));
    }

    return new Blueprint(id, displayName, description, icon, blocks);
  }

  private int getInt(Map<?, ?> map, String key) {
    Object val = map.get(key);
    if (val instanceof Number n) {
      return n.intValue();
    }
    try {
      return Integer.parseInt(String.valueOf(val));
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  public Blueprint getBlueprint(String id) {
    return blueprints.get(id);
  }

  public List<Blueprint> getAllBlueprints() {
    return new ArrayList<>(blueprints.values());
  }

  public List<String> getBlueprintIds() {
    return new ArrayList<>(blueprints.keySet());
  }

  public boolean hasBlueprint(String id) {
    return blueprints.containsKey(id);
  }

  public File getBlueprintsDir() {
    return blueprintsDir;
  }
}

