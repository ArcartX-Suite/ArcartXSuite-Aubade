package xuanmo.aubade.core.storage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * 数据库迁移管理器。
 * 管理表结构版本控制与自动升级。
 */
public class MigrationManager {

  private static final int CURRENT_VERSION = 1;
  private static final Logger logger = Logger.getLogger("Aubade");

  public void migrate(DataSource ds) {
    try (Connection conn = ds.getConnection()) {
      ensureVersionTable(conn);
      int current = getCurrentVersion(conn);
      while (current < CURRENT_VERSION) {
        current++;
        runMigration(conn, current);
        setVersion(conn, current);
      }
      if (current == CURRENT_VERSION) {
        logger.info("[迁移] 数据库已是最新版本 (v" + CURRENT_VERSION + ")");
      }
    } catch (SQLException e) {
      logger.severe("[迁移] 数据库迁移失败: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void ensureVersionTable(Connection conn) throws SQLException {
    String sql = """
        CREATE TABLE IF NOT EXISTS aubade_schema_version (
            version INT PRIMARY KEY,
            applied_at BIGINT
        );
        """;
    try (Statement stmt = conn.createStatement()) {
      stmt.execute(sql);
    }
  }

  private int getCurrentVersion(Connection conn) throws SQLException {
    String sql = "SELECT MAX(version) as v FROM aubade_schema_version";
    try (Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      if (rs.next()) {
        int v = rs.getInt("v");
        return rs.wasNull() ? 0 : v;
      }
      return 0;
    }
  }

  private void setVersion(Connection conn, int version) throws SQLException {
    String sql = "INSERT OR REPLACE INTO aubade_schema_version (version, applied_at) VALUES (" + version + ", "
        + System.currentTimeMillis() + ")";
    try (Statement stmt = conn.createStatement()) {
      stmt.execute(sql);
    }
  }

  private void runMigration(Connection conn, int version) throws SQLException {
    String resourcePath = "/migrations/V" + version + "__initial_schema.sql";
    InputStream is = getClass().getResourceAsStream(resourcePath);
    if (is == null) {
      logger.warning("[迁移] 未找到迁移文件: " + resourcePath + "，将使用内联建表逻辑。");
      runInlineV1(conn);
      return;
    }

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8));
        Statement stmt = conn.createStatement()) {
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("--")) {
          continue;
        }
        sb.append(line).append("\n");
        if (line.endsWith(";")) {
          stmt.execute(sb.toString());
          sb.setLength(0);
        }
      }
    } catch (java.io.IOException e) {
      logger.severe("[迁移] 读取迁移文件失败: " + e.getMessage());
    }
    logger.info("[迁移] 已应用迁移 V" + version);
  }

  private void runInlineV1(Connection conn) throws SQLException {
    // 内联建表：岛屿主表
    String[] sqls = {
        """
            CREATE TABLE IF NOT EXISTS aubade_islands (
                id              VARCHAR(36) PRIMARY KEY,
                owner           VARCHAR(36) NOT NULL,
                name            VARCHAR(64),
                description     VARCHAR(256),
                world           VARCHAR(64) NOT NULL,
                center_x        INT NOT NULL,
                center_y        INT NOT NULL,
                center_z        INT NOT NULL,
                protection_range INT DEFAULT 50,
                range           INT DEFAULT 100,
                game_mode       VARCHAR(32) NOT NULL,
                locked          BOOLEAN DEFAULT FALSE,
                purge_protected BOOLEAN DEFAULT FALSE,
                flags           TEXT,
                meta            TEXT,
                created_at      BIGINT,
                last_login      BIGINT
            );
            """,
        """
            CREATE TABLE IF NOT EXISTS aubade_members (
                island_id       VARCHAR(36) NOT NULL,
                player_uuid     VARCHAR(36) NOT NULL,
                role            VARCHAR(16) DEFAULT 'MEMBER',
                trust_level     INT DEFAULT 0,
                joined_at       BIGINT,
                PRIMARY KEY (island_id, player_uuid)
            );
            """,
        """
            CREATE TABLE IF NOT EXISTS aubade_bans (
                island_id       VARCHAR(36) NOT NULL,
                player_uuid     VARCHAR(36) NOT NULL,
                banned_by       VARCHAR(36),
                reason          VARCHAR(256),
                banned_at       BIGINT,
                PRIMARY KEY (island_id, player_uuid)
            );
            """,
        """
            CREATE TABLE IF NOT EXISTS aubade_trusts (
                island_id       VARCHAR(36) NOT NULL,
                player_uuid     VARCHAR(36) NOT NULL,
                trusted_at      BIGINT,
                PRIMARY KEY (island_id, player_uuid)
            );
            """,
        """
            CREATE TABLE IF NOT EXISTS aubade_players (
                uuid            VARCHAR(36) PRIMARY KEY,
                island_id       VARCHAR(36),
                last_island     VARCHAR(36),
                deaths          INT DEFAULT 0,
                resets          INT DEFAULT 0,
                total_online    BIGINT DEFAULT 0,
                last_login      BIGINT,
                last_logout     BIGINT,
                auto_pickup     BOOLEAN DEFAULT FALSE,
                locale          VARCHAR(16) DEFAULT 'zh_cn',
                addon_data      TEXT
            );
            """,
        """
            CREATE TABLE IF NOT EXISTS aubade_worlds (
                name            VARCHAR(64) PRIMARY KEY,
                friendly_name   VARCHAR(64),
                game_mode       VARCHAR(32),
                nether_enabled  BOOLEAN DEFAULT TRUE,
                end_enabled     BOOLEAN DEFAULT TRUE,
                max_size        INT DEFAULT 200,
                default_range   INT DEFAULT 50,
                spacing         INT DEFAULT 200,
                sea_level       INT DEFAULT 0,
                settings        TEXT
            );
            """,
        "CREATE INDEX IF NOT EXISTS idx_islands_owner ON aubade_islands(owner);",
        "CREATE INDEX IF NOT EXISTS idx_islands_world ON aubade_islands(world);",
        "CREATE INDEX IF NOT EXISTS idx_members_player ON aubade_members(player_uuid);",
        "CREATE INDEX IF NOT EXISTS idx_players_island ON aubade_players(island_id);"
    };

    try (Statement stmt = conn.createStatement()) {
      for (String sql : sqls) {
        stmt.execute(sql);
      }
    }
    logger.info("[迁移] 已执行内联建表 V1");
  }
}

