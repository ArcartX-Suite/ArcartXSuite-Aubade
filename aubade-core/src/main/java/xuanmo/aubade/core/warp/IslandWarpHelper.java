package xuanmo.aubade.core.warp;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import xuanmo.arcartxsuite.api.aubade.island.Island;

/**
 * 岛屿传送点辅助工具。
 * 在 Island.meta 中以 JSON 形式持久化传送点数据。
 */
public final class IslandWarpHelper {

  private static final String META_KEY = "warps";

  private IslandWarpHelper() {
  }

  /**
   * 获取岛屿所有传送点。
   */
  public static Map<String, Location> getWarps(Island island) {
    Map<String, Location> warps = new HashMap<>();
    String json = island.getMeta().get(META_KEY);
    if (json == null || json.isBlank()) {
      return warps;
    }
    try {
      // 简单解析: {"home":"world:0.0:64.0:0.0:0.0:0.0",...}
      String content = json.trim();
      if (content.startsWith("{") && content.endsWith("}")) {
        content = content.substring(1, content.length() - 1);
      }
      if (content.isBlank()) {
        return warps;
      }
      // 按逗号分割键值对
      String[] pairs = content.split(",");
      for (String pair : pairs) {
        int colon = pair.indexOf(':');
        if (colon < 0) continue;
        String key = unquote(pair.substring(0, colon).trim());
        String val = unquote(pair.substring(colon + 1).trim());
        Location loc = deserialize(val);
        if (loc != null) {
          warps.put(key, loc);
        }
      }
    } catch (Exception e) {
      // 解析失败返回空
    }
    return warps;
  }

  /**
   * 设置传送点。
   */
  public static void setWarp(Island island, String name, Location location) {
    Map<String, Location> warps = getWarps(island);
    warps.put(name, location);
    saveWarps(island, warps);
  }

  /**
   * 删除传送点。
   */
  public static void removeWarp(Island island, String name) {
    Map<String, Location> warps = getWarps(island);
    if (warps.remove(name) != null) {
      saveWarps(island, warps);
    }
  }

  /**
   * 保存传送点到 meta。
   */
  public static void saveWarps(Island island, Map<String, Location> warps) {
    StringBuilder sb = new StringBuilder("{");
    boolean first = true;
    for (Map.Entry<String, Location> entry : warps.entrySet()) {
      if (!first) sb.append(",");
      first = false;
      sb.append("\"").append(entry.getKey()).append("\":\"").append(serialize(entry.getValue())).append("\"");
    }
    sb.append("}");
    island.getMeta().put(META_KEY, sb.toString());
  }

  private static String serialize(Location loc) {
    return loc.getWorld().getName() + ":"
        + loc.getX() + ":"
        + loc.getY() + ":"
        + loc.getZ() + ":"
        + loc.getYaw() + ":"
        + loc.getPitch();
  }

  private static Location deserialize(String s) {
    String[] parts = s.split(":");
    if (parts.length < 4) return null;
    org.bukkit.World world = Bukkit.getWorld(parts[0]);
    if (world == null) return null;
    double x = Double.parseDouble(parts[1]);
    double y = Double.parseDouble(parts[2]);
    double z = Double.parseDouble(parts[3]);
    float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0f;
    float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0f;
    return new Location(world, x, y, z, yaw, pitch);
  }

  private static String unquote(String s) {
    if (s.startsWith("\"") && s.endsWith("\"")) {
      return s.substring(1, s.length() - 1);
    }
    return s;
  }
}

