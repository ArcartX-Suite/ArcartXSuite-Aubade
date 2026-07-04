package xuanmo.aubade.core.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javax.sql.DataSource;
import xuanmo.arcartxsuite.api.aubade.world.WorldSettings;

/**
 * 世界配置表 JDBC 仓库。
 */
public class JdbcWorldRepository {

  private final DataSource dataSource;
  private final Logger logger = Logger.getLogger("Aubade");

  public JdbcWorldRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void save(WorldSettings settings) {
    String sql = """
        INSERT INTO aubade_worlds
        (name, friendly_name, game_mode, nether_enabled, end_enabled, max_size, default_range, spacing, sea_level, settings)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(name) DO UPDATE SET
        friendly_name=excluded.friendly_name, game_mode=excluded.game_mode,
        nether_enabled=excluded.nether_enabled, end_enabled=excluded.end_enabled,
        max_size=excluded.max_size, default_range=excluded.default_range,
        spacing=excluded.spacing, sea_level=excluded.sea_level, settings=excluded.settings;
        """;
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, settings.getWorldName());
      ps.setString(2, settings.getFriendlyName());
      ps.setString(3, "unknown"); // TODO: game_mode
      ps.setBoolean(4, settings.isNetherEnabled());
      ps.setBoolean(5, settings.isEndEnabled());
      ps.setInt(6, settings.getMaxIslandSize());
      ps.setInt(7, settings.getDefaultProtectionRange());
      ps.setInt(8, settings.getIslandSpacing());
      ps.setInt(9, settings.getSeaLevel());
      ps.setString(10, "{}"); // TODO: settings JSON
      ps.executeUpdate();
    } catch (SQLException e) {
      logger.severe("[存储] 保存世界配置失败: " + e.getMessage());
    }
  }

  public void delete(String worldName) {
    String sql = "DELETE FROM aubade_worlds WHERE name = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, worldName);
      ps.executeUpdate();
    } catch (SQLException e) {
      logger.severe("[存储] 删除世界配置失败: " + e.getMessage());
    }
  }

  public Optional<WorldSettings> findByName(String worldName) {
    String sql = "SELECT * FROM aubade_worlds WHERE name = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, worldName);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return Optional.of(mapRow(rs));
      }
    } catch (SQLException e) {
      logger.severe("[存储] 查询世界配置失败: " + e.getMessage());
    }
    return Optional.empty();
  }

  public List<WorldSettings> findAll() {
    List<WorldSettings> list = new ArrayList<>();
    String sql = "SELECT * FROM aubade_worlds";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        list.add(mapRow(rs));
      }
    } catch (SQLException e) {
      logger.severe("[存储] 查询所有世界配置失败: " + e.getMessage());
    }
    return list;
  }

  private WorldSettings mapRow(ResultSet rs) throws SQLException {
    return new WorldSettings() {
      @Override
      public String getFriendlyName() {
        try {
          return rs.getString("friendly_name");
        } catch (SQLException e) {
          return "";
        }
      }

      @Override
      public String getWorldName() {
        try {
          return rs.getString("name");
        } catch (SQLException e) {
          return "";
        }
      }

      @Override
      public boolean isNetherEnabled() {
        try {
          return rs.getBoolean("nether_enabled");
        } catch (SQLException e) {
          return true;
        }
      }

      @Override
      public boolean isEndEnabled() {
        try {
          return rs.getBoolean("end_enabled");
        } catch (SQLException e) {
          return true;
        }
      }

      @Override
      public int getMaxIslandSize() {
        try {
          return rs.getInt("max_size");
        } catch (SQLException e) {
          return 200;
        }
      }

      @Override
      public int getDefaultProtectionRange() {
        try {
          return rs.getInt("default_range");
        } catch (SQLException e) {
          return 50;
        }
      }

      @Override
      public int getIslandSpacing() {
        try {
          return rs.getInt("spacing");
        } catch (SQLException e) {
          return 200;
        }
      }

      @Override
      public int getSeaLevel() {
        try {
          return rs.getInt("sea_level");
        } catch (SQLException e) {
          return 0;
        }
      }
    };
  }
}

