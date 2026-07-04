package xuanmo.aubade.core.features.level;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

/**
 * 方块价值表。
 * 按 Material 名称映射到等级分值，支持通配符。
 */
public class BlockValues {

  private final Map<String, Long> exactValues = new HashMap<>();
  private final Map<String, Long> wildcardValues = new HashMap<>();
  private final long defaultValue;
  private final Logger logger;

  public BlockValues(ConfigurationSection section, long defaultValue, Logger logger) {
    this.defaultValue = defaultValue;
    this.logger = logger;
    if (section != null) {
      for (String key : section.getKeys(false)) {
        long value = section.getLong(key, defaultValue);
        if (key.contains("*")) {
          wildcardValues.put(key.toLowerCase(), value);
        } else {
          exactValues.put(key.toLowerCase(), value);
        }
      }
    }
  }

  /**
   * 获取指定 Material 的等级分值。
   */
  public long getValue(Material material) {
    if (material == null) {
      return defaultValue;
    }
    String name = material.getKey().toString().toLowerCase(); // minecraft:stone
    Long exact = exactValues.get(name);
    if (exact != null) {
      return exact;
    }
    // 通配符匹配：如 minecraft:*_ore
    for (Map.Entry<String, Long> entry : wildcardValues.entrySet()) {
      String pattern = entry.getKey().replace("*", ".*");
      if (name.matches(pattern)) {
        return entry.getValue();
      }
    }
    return defaultValue;
  }

  public Map<String, Long> getAllExactValues() {
    return Map.copyOf(exactValues);
  }

  public long getDefaultValue() {
    return defaultValue;
  }
}

