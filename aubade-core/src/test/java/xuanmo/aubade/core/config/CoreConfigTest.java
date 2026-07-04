package xuanmo.aubade.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CoreConfigTest {

  @TempDir
  Path tempDir;

  @Test
  void persistsGlobalSpawnCoordinates() throws Exception {
    Path configFile = tempDir.resolve("config.yml");
    Files.writeString(configFile, """
        storage:
          type: sqlite
        general:
          default-gamemode: skyblock
        """, StandardCharsets.UTF_8);

    CoreConfig config = new CoreConfig(configFile.toFile(), java.util.logging.Logger.getLogger("test"));
    config.load();
    config.setGlobalSpawn(new Location(world("spawn_world"), 123.5, 64.0, -45.25, 90.0f, 30.0f));
    config.save();

    YamlConfiguration reloaded = YamlConfiguration.loadConfiguration(configFile.toFile());
    assertEquals("spawn_world", reloaded.getString("world.spawn.world"));
    assertEquals(123.5d, reloaded.getDouble("world.spawn.x"));
    assertEquals(64.0d, reloaded.getDouble("world.spawn.y"));
    assertEquals(-45.25d, reloaded.getDouble("world.spawn.z"));
    assertEquals(90.0d, reloaded.getDouble("world.spawn.yaw"));
    assertEquals(30.0d, reloaded.getDouble("world.spawn.pitch"));
  }

  @Test
  void loadsDefaultValuesFromYaml() throws Exception {
    Path configFile = tempDir.resolve("config.yml");
    Files.writeString(configFile, """
        storage:
          type: mysql
          mysql:
            host: 127.0.0.1
            port: 3307
            database: aubade_test
            username: test
            password: secret
            pool-size: 3
          table-prefix: test_
        general:
          default-gamemode: skyblock
          max-islands-per-player: 7
          debug: true
        """, StandardCharsets.UTF_8);

    CoreConfig config = new CoreConfig(configFile.toFile(), java.util.logging.Logger.getLogger("test"));
    config.load();

    assertEquals("mysql", config.getStorageType());
    assertNotNull(config.getStorageDescriptor());
    assertEquals(7, config.getMaxIslandsPerPlayer());
    assertEquals(true, config.isDebug());
  }

  private static World world(String name) {
    InvocationHandler handler = (Object proxy, Method method, Object[] args) -> {
      return switch (method.getName()) {
        case "getName" -> name;
        case "toString" -> "World[" + name + "]";
        case "hashCode" -> name.hashCode();
        case "equals" -> proxy == args[0];
        default -> throw new UnsupportedOperationException("未实现的 World 方法: " + method.getName());
      };
    };
    return (World) Proxy.newProxyInstance(World.class.getClassLoader(), new Class<?>[] {World.class}, handler);
  }
}
