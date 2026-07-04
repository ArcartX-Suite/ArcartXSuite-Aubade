package xuanmo.aubade.api.addon;

import java.util.List;
import org.bukkit.generator.ChunkGenerator;
import xuanmo.aubade.api.command.CommandManager;
import xuanmo.aubade.api.world.WorldManager;
import xuanmo.aubade.api.world.WorldSettings;

/**
 * 游戏模式组件接口。
 * 每种游戏模式（如 SkyBlock、AcidIsland）需实现此接口。
 */
public interface GameModeAddon extends SkyAddon {

  /** 游戏模式唯一标识，如 "skyblock" */
  String getGameModeId();

  /** 显示名称，如 "经典空岛" */
  String getFriendlyName();

  /** 世界配置 */
  WorldSettings getWorldSettings();

  /** 注册世界 */
  void registerWorlds(WorldManager worldManager);

  /** Overworld 生成器 */
  ChunkGenerator getOverworldGenerator();

  /** Nether 生成器（可为 null） */
  default ChunkGenerator getNetherGenerator() {
    return null;
  }

  /** End 生成器（可为 null） */
  default ChunkGenerator getEndGenerator() {
    return null;
  }

  /** 注册命令 */
  void registerCommands(CommandManager commandManager);
}
