package xuanmo.aubade.game.skyblock;

import java.util.List;
import org.bukkit.generator.ChunkGenerator;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.arcartxsuite.api.aubade.addon.GameModeAddon;
import xuanmo.arcartxsuite.api.aubade.command.CommandManager;
import xuanmo.arcartxsuite.api.aubade.world.WorldManager;
import xuanmo.arcartxsuite.api.aubade.world.WorldSettings;
import xuanmo.aubade.game.skyblock.command.SkyBlockCommand;

/**
 * 经典空岛游戏模式组件。
 */
public class SkyBlockAddon implements GameModeAddon {

  private boolean enabled = false;
  private final SkyBlockWorldSettings worldSettings = new SkyBlockWorldSettings();

  @Override
  public AddonDescriptor descriptor() {
    return AddonDescriptor.builder("skyblock")
        .name("经典空岛")
        .version("1.0.0")
        .mainClass(getClass().getName())
        .depends(List.of())
        .softDepends(List.of())
        .pluginDepends(List.of("Aubade"))
        .build();
  }

  @Override
  public void onLoad() {
  }

  @Override
  public void onEnable() {
    enabled = true;
  }

  @Override
  public void onDisable() {
    enabled = false;
  }

  @Override
  public void onReload() {
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public String getGameModeId() {
    return "skyblock";
  }

  @Override
  public String getFriendlyName() {
    return "经典空岛";
  }

  @Override
  public WorldSettings getWorldSettings() {
    return worldSettings;
  }

  @Override
  public void registerWorlds(WorldManager worldManager) {
    worldManager.createWorld(worldSettings);
  }

  @Override
  public ChunkGenerator getOverworldGenerator() {
    return new ChunkGenerator() {
      @Override
      public void generateNoise(org.bukkit.generator.WorldInfo worldInfo, java.util.Random random, int chunkX, int chunkZ, org.bukkit.generator.ChunkGenerator.ChunkData chunkData) {
        // 虚空世界，不生成噪声
      }

      @Override
      public org.bukkit.Location getFixedSpawnLocation(org.bukkit.World world, java.util.Random random) {
        return new org.bukkit.Location(world, 0.5, 128, 0.5);
      }
    };
  }

  @Override
  public ChunkGenerator getNetherGenerator() {
    return null;
  }

  @Override
  public ChunkGenerator getEndGenerator() {
    return null;
  }

  @Override
  public void registerCommands(CommandManager commandManager) {
    commandManager.registerSubCommand("island", new SkyBlockCommand(this));
  }
}
