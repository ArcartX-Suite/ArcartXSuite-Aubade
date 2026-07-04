package xuanmo.aubade.core.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import javax.sql.DataSource;
import xuanmo.arcartxsuite.api.aubade.player.SkyPlayer;
import xuanmo.arcartxsuite.api.aubade.storage.Repository;

/**
 * 玩家表 JDBC 仓库。
 */
public class JdbcPlayerRepository implements Repository<SkyPlayer> {

  private final DataSource dataSource;
  private final Logger logger = Logger.getLogger("Aubade");

  public JdbcPlayerRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public void save(SkyPlayer player) {
    String sql = """
        INSERT INTO aubade_players
        (uuid, island_id, last_island, deaths, resets, total_online, last_login, last_logout, auto_pickup, locale, addon_data)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(uuid) DO UPDATE SET
        island_id=excluded.island_id, last_island=excluded.last_island,
        deaths=excluded.deaths, resets=excluded.resets, total_online=excluded.total_online,
        last_login=excluded.last_login, last_logout=excluded.last_logout,
        auto_pickup=excluded.auto_pickup, locale=excluded.locale, addon_data=excluded.addon_data;
        """;
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, player.getUuid().toString());
      ps.setString(2, player.getIslandId() != null ? player.getIslandId().toString() : null);
      ps.setString(3, player.getLastIsland() != null ? player.getLastIsland().toString() : null);
      ps.setInt(4, player.getDeaths());
      ps.setInt(5, player.getResets());
      ps.setLong(6, player.getTotalOnlineTime());
      ps.setLong(7, player.getLastLogin());
      ps.setLong(8, player.getLastLogout());
      ps.setBoolean(9, player.isAutoPickup());
      ps.setString(10, player.getLocale());
      ps.setString(11, "{}"); // TODO: addon_data JSON
      ps.executeUpdate();
    } catch (SQLException e) {
      logger.severe("[存储] 保存玩家失败: " + e.getMessage());
    }
  }

  @Override
  public void delete(SkyPlayer player) {
    String sql = "DELETE FROM aubade_players WHERE uuid = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, player.getUuid().toString());
      ps.executeUpdate();
    } catch (SQLException e) {
      logger.severe("[存储] 删除玩家失败: " + e.getMessage());
    }
  }

  public Optional<SkyPlayer> findById(UUID uuid) {
    String sql = "SELECT * FROM aubade_players WHERE uuid = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, uuid.toString());
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return Optional.of(mapRow(rs));
      }
    } catch (SQLException e) {
      logger.severe("[存储] 查询玩家失败: " + e.getMessage());
    }
    return Optional.empty();
  }

  private SkyPlayer mapRow(ResultSet rs) throws SQLException {
    UUID uuid = UUID.fromString(rs.getString("uuid"));
    SkyPlayer player = new SkyPlayer(uuid);
    String islandId = rs.getString("island_id");
    if (islandId != null) {
      player.setIslandId(UUID.fromString(islandId));
    }
    String lastIsland = rs.getString("last_island");
    if (lastIsland != null) {
      player.setLastIsland(UUID.fromString(lastIsland));
    }
    player.setDeaths(rs.getInt("deaths"));
    player.setResets(rs.getInt("resets"));
    player.setTotalOnlineTime(rs.getLong("total_online"));
    player.setLastLogin(rs.getLong("last_login"));
    player.setLastLogout(rs.getLong("last_logout"));
    player.setAutoPickup(rs.getBoolean("auto_pickup"));
    player.setLocale(rs.getString("locale"));
    return player;
  }
}

