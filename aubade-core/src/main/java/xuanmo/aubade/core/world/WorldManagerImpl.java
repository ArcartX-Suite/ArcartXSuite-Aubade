package xuanmo.aubade.core.world;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;
import xuanmo.arcartxsuite.api.aubade.world.WorldManager;
import xuanmo.arcartxsuite.api.aubade.world.WorldSettings;

/**
 * 世界管理器实现。
 */
public class WorldManagerImpl implements WorldManager {

  private final Logger logger = Logger.getLogger("Aubade");
  private final Map<String, World> worlds = new HashMap<>();

  @Override
  public World createWorld(WorldSettings settings) {
    String worldName = settings.getWorldName();
    World existing = org.bukkit.Bukkit.getWorld(worldName);
    if (existing != null) {
      logger.info("[世界] 世界已存在，直接返回: " + worldName);
      worlds.put(worldName, existing);
      return existing;
    }

    WorldCreator creator = new WorldCreator(worldName);
    creator.environment(World.Environment.NORMAL);
    // 空岛生成器（简化版，后续由游戏模式覆盖）
    creator.generator(new SkyBlockChunkGenerator());

    World world = creator.createWorld();
    if (world != null) {
      worlds.put(worldName, world);
      logger.info("[世界] 已创建世界: " + worldName);
    } else {
      logger.severe("[世界] 创建世界失败: " + worldName);
    }
    return world;
  }

  public World getWorld(String name) {
    return worlds.get(name);
  }
}

