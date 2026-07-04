package xuanmo.aubade.core.config;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import xuanmo.arcartxsuite.api.storage.StorageDescriptor;

/**
 * 核心配置 POJO。
 */
public class CoreConfig {

  private final File configFile;
  private final Logger logger;
  private YamlConfiguration yaml;

  private String storageType = "sqlite";
  private String mysqlHost = "localhost";
  private int mysqlPort = 3306;
  private String mysqlDatabase = "aubade";
  private String mysqlUsername = "root";
  private String mysqlPassword = "";
  private int poolSize = 10;
  private String sqliteFileName = "aubade.db";
  private String tablePrefix = "aubade";
  private String defaultGameMode = "skyblock";
  private int maxIslandsPerPlayer = 1;
  private boolean debug = false;
  private String globalSpawnWorld = "";
  private double globalSpawnX = 0.0;
  private double globalSpawnY = 0.0;
  private double globalSpawnZ = 0.0;
  private float globalSpawnYaw = 0.0f;
  private float globalSpawnPitch = 0.0f;

  public CoreConfig(File configFile, Logger logger) {
    this.configFile = configFile;
    this.logger = logger;
  }

  public void load() {
    if (configFile.getParentFile() != null && !configFile.getParentFile().exists()) {
      configFile.getParentFile().mkdirs();
    }
    this.yaml = YamlConfiguration.loadConfiguration(configFile);

    this.storageType = yaml.getString("storage.type", "sqlite").toLowerCase(Locale.ROOT);
    this.mysqlHost = yaml.getString("storage.mysql.host", "localhost");
    this.mysqlPort = yaml.getInt("storage.mysql.port", 3306);
    this.mysqlDatabase = yaml.getString("storage.mysql.database", "aubade");
    this.mysqlUsername = yaml.getString("storage.mysql.username", "root");
    this.mysqlPassword = yaml.getString("storage.mysql.password", "");
    this.poolSize = yaml.getInt("storage.mysql.pool-size", 10);
    this.sqliteFileName = yaml.getString("storage.sqlite.file-name", "aubade.db");
    this.tablePrefix = yaml.getString("storage.table-prefix", "aubade");
    this.defaultGameMode = yaml.getString("general.default-gamemode", "skyblock");
    this.maxIslandsPerPlayer = yaml.getInt("general.max-islands-per-player", 1);
    this.debug = yaml.getBoolean("general.debug", false);
    this.globalSpawnWorld = yaml.getString("world.spawn.world", "");
    this.globalSpawnX = yaml.getDouble("world.spawn.x", 0.0);
    this.globalSpawnY = yaml.getDouble("world.spawn.y", 0.0);
    this.globalSpawnZ = yaml.getDouble("world.spawn.z", 0.0);
    this.globalSpawnYaw = (float) yaml.getDouble("world.spawn.yaw", 0.0);
    this.globalSpawnPitch = (float) yaml.getDouble("world.spawn.pitch", 0.0);
  }

  public void save() {
    try {
      yaml.save(configFile);
    } catch (IOException e) {
      logger.severe("[配置] 保存 config.yml 失败: " + e.getMessage());
    }
  }

  public StorageDescriptor getStorageDescriptor() {
    if ("mysql".equals(storageType)) {
      return StorageDescriptor.mysql(mysqlHost, mysqlPort, mysqlDatabase, mysqlUsername, mysqlPassword, poolSize, tablePrefix);
    }
    return new StorageDescriptor(false, "", 0, "", "", "", 1, sqliteFileName, tablePrefix);
  }

  public String getStorageType() {
    return storageType;
  }

  public String getDefaultGameMode() {
    return defaultGameMode;
  }

  public int getMaxIslandsPerPlayer() {
    return maxIslandsPerPlayer;
  }

  public boolean isDebug() {
    return debug;
  }

  public Location getGlobalSpawn() {
    if (globalSpawnWorld == null || globalSpawnWorld.isBlank()) {
      return null;
    }
    var world = Bukkit.getWorld(globalSpawnWorld);
    if (world == null) {
      return null;
    }
    return new Location(world, globalSpawnX, globalSpawnY, globalSpawnZ, globalSpawnYaw, globalSpawnPitch);
  }

  public void setGlobalSpawn(Location location) {
    if (location == null || location.getWorld() == null) {
      this.globalSpawnWorld = "";
      this.globalSpawnX = 0.0;
      this.globalSpawnY = 0.0;
      this.globalSpawnZ = 0.0;
      this.globalSpawnYaw = 0.0f;
      this.globalSpawnPitch = 0.0f;
      yaml.set("world.spawn.world", null);
      yaml.set("world.spawn.x", null);
      yaml.set("world.spawn.y", null);
      yaml.set("world.spawn.z", null);
      yaml.set("world.spawn.yaw", null);
      yaml.set("world.spawn.pitch", null);
      return;
    }
    this.globalSpawnWorld = location.getWorld().getName();
    this.globalSpawnX = location.getX();
    this.globalSpawnY = location.getY();
    this.globalSpawnZ = location.getZ();
    this.globalSpawnYaw = location.getYaw();
    this.globalSpawnPitch = location.getPitch();
    yaml.set("world.spawn.world", globalSpawnWorld);
    yaml.set("world.spawn.x", globalSpawnX);
    yaml.set("world.spawn.y", globalSpawnY);
    yaml.set("world.spawn.z", globalSpawnZ);
    yaml.set("world.spawn.yaw", globalSpawnYaw);
    yaml.set("world.spawn.pitch", globalSpawnPitch);
  }

  public YamlConfiguration getRaw() {
    return yaml;
  }

  public File getConfigFile() {
    return configFile;
  }
}
