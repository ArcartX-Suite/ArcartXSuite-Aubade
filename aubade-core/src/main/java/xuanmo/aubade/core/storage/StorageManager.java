package xuanmo.aubade.core.storage;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Logger;
import javax.sql.DataSource;
import xuanmo.arcartxsuite.api.storage.AbstractModuleRepository;
import xuanmo.arcartxsuite.api.storage.StorageDescriptor;

public class StorageManager extends AbstractModuleRepository {

  public StorageManager(java.io.File dataFolder, StorageDescriptor descriptor, Logger logger) {
    super("AXS-Aubade", dataFolder, descriptor, logger);
  }

  public void init() {
    try {
      initialize();
    } catch (SQLException e) {
      throw new IllegalStateException("[存储] 初始化失败: " + e.getMessage(), e);
    }
  }

  @Override
  protected void onInitialize(Connection connection) throws SQLException {
    try (Statement stmt = connection.createStatement()) {
      stmt.execute("CREATE TABLE IF NOT EXISTS aubade_schema_version (version INT PRIMARY KEY, applied_at BIGINT)");
      stmt.execute("CREATE TABLE IF NOT EXISTS aubade_islands (id VARCHAR(36) PRIMARY KEY, owner VARCHAR(36) NOT NULL, name VARCHAR(64), description VARCHAR(256), world VARCHAR(64) NOT NULL, center_x INT NOT NULL, center_y INT NOT NULL, center_z INT NOT NULL, protection_range INT DEFAULT 50, range INT DEFAULT 100, game_mode VARCHAR(32) NOT NULL, locked BOOLEAN DEFAULT FALSE, purge_protected BOOLEAN DEFAULT FALSE, flags TEXT, meta TEXT, created_at BIGINT, last_login BIGINT)");
      stmt.execute("CREATE TABLE IF NOT EXISTS aubade_members (island_id VARCHAR(36) NOT NULL, player_uuid VARCHAR(36) NOT NULL, role VARCHAR(16) DEFAULT 'MEMBER', trust_level INT DEFAULT 0, joined_at BIGINT, PRIMARY KEY (island_id, player_uuid))");
      stmt.execute("CREATE TABLE IF NOT EXISTS aubade_bans (island_id VARCHAR(36) NOT NULL, player_uuid VARCHAR(36) NOT NULL, banned_by VARCHAR(36), reason VARCHAR(256), banned_at BIGINT, PRIMARY KEY (island_id, player_uuid))");
      stmt.execute("CREATE TABLE IF NOT EXISTS aubade_trusts (island_id VARCHAR(36) NOT NULL, player_uuid VARCHAR(36) NOT NULL, trusted_at BIGINT, PRIMARY KEY (island_id, player_uuid))");
      stmt.execute("CREATE TABLE IF NOT EXISTS aubade_players (uuid VARCHAR(36) PRIMARY KEY, island_id VARCHAR(36), last_island VARCHAR(36), deaths INT DEFAULT 0, resets INT DEFAULT 0, total_online BIGINT DEFAULT 0, last_login BIGINT, last_logout BIGINT, auto_pickup BOOLEAN DEFAULT FALSE, locale VARCHAR(16) DEFAULT 'zh_cn', addon_data TEXT)");
      stmt.execute("CREATE TABLE IF NOT EXISTS aubade_worlds (name VARCHAR(64) PRIMARY KEY, friendly_name VARCHAR(64), game_mode VARCHAR(32), nether_enabled BOOLEAN DEFAULT TRUE, end_enabled BOOLEAN DEFAULT TRUE, max_size INT DEFAULT 200, default_range INT DEFAULT 50, spacing INT DEFAULT 200, sea_level INT DEFAULT 0, settings TEXT)");
      stmt.execute("CREATE INDEX IF NOT EXISTS idx_islands_owner ON aubade_islands(owner)");
      stmt.execute("CREATE INDEX IF NOT EXISTS idx_islands_world ON aubade_islands(world)");
      stmt.execute("CREATE INDEX IF NOT EXISTS idx_members_player ON aubade_members(player_uuid)");
      stmt.execute("CREATE INDEX IF NOT EXISTS idx_players_island ON aubade_players(island_id)");
    }
  }

  @Override
  protected List<String> playerDataTables() {
    return List.of("aubade_players");
  }

  @Override
  protected String playerUuidColumn() {
    return "uuid";
  }

  public DataSource getDataSource() {
    return dataSource();
  }

  public void execute(String sql) {
    try (Connection conn = getConnection();
         Statement stmt = conn.createStatement()) {
      stmt.execute(sql);
    } catch (SQLException e) {
      logger.severe("[存储] SQL 执行失败: " + e.getMessage() + " | SQL: " + sql);
    }
  }
}
