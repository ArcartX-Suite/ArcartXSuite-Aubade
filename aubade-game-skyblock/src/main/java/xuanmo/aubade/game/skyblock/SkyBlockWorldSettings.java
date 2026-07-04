package xuanmo.aubade.game.skyblock;

import org.bukkit.block.Biome;
import xuanmo.arcartxsuite.api.aubade.world.WorldSettings;

/**
 * 经典空岛世界配置。
 */
public class SkyBlockWorldSettings implements WorldSettings {

  @Override
  public String getFriendlyName() {
    return "经典空岛";
  }

  @Override
  public String getWorldName() {
    return "aubade_skyblock";
  }

  @Override
  public boolean isNetherEnabled() {
    return true;
  }

  @Override
  public boolean isEndEnabled() {
    return true;
  }

  @Override
  public int getMaxIslandSize() {
    return 200;
  }

  @Override
  public int getDefaultProtectionRange() {
    return 50;
  }

  @Override
  public int getIslandSpacing() {
    return 200;
  }

  @Override
  public int getSeaLevel() {
    return 0;
  }

  @Override
  public Biome getDefaultBiome() {
    return Biome.PLAINS;
  }
}
