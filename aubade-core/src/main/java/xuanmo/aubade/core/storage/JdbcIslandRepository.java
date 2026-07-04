package xuanmo.aubade.core.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.arcartxsuite.api.aubade.storage.Repository;

/**
 * 岛屿表 JDBC 仓库。
 */
public class JdbcIslandRepository implements Repository<Island> {

  private final DataSource dataSource;
  private final Logger logger = Logger.getLogger("Aubade");

  public JdbcIslandRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public void save(Island island) {
    String sql = """
        INSERT INTO aubade_islands
        (id, owner, name, description, world, center_x, center_y, center_z,
         protection_range, range, game_mode, locked, purge_protected, flags, meta, created_at, last_login)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(id) DO UPDATE SET
        owner=excluded.owner, name=excluded.name, description=excluded.description,
        world=excluded.world, center_x=excluded.center_x, center_y=excluded.center_y, center_z=excluded.center_z,
        protection_range=excluded.protection_range, range=excluded.range, game_mode=excluded.game_mode,
        locked=excluded.locked, purge_protected=excluded.purge_protected,
        flags=excluded.flags, meta=excluded.meta, last_login=excluded.last_login;
        """;
    Location center = island.getCenter();
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, island.getUniqueId().toString());
      ps.setString(2, island.getOwner().toString());
      ps.setString(3, island.getName());
      ps.setString(4, island.getDescription());
      ps.setString(5, center != null && center.getWorld() != null ? center.getWorld().getName() : "");
      ps.setInt(6, center != null ? center.getBlockX() : 0);
      ps.setInt(7, center != null ? center.getBlockY() : 0);
      ps.setInt(8, center != null ? center.getBlockZ() : 0);
      ps.setInt(9, island.getProtectionRange());
      ps.setInt(10, island.getRange());
      ps.setString(11, island.getGameMode() != null ? island.getGameMode().getGameModeId() : "");
      ps.setBoolean(12, island.isLocked());
      ps.setBoolean(13, island.isPurgeProtected());
      ps.setString(14, "{}"); // TODO: flags JSON
      ps.setString(15, "{}"); // TODO: meta JSON
      ps.setLong(16, island.getCreatedTime());
      ps.setLong(17, island.getLastLoginTime());
      ps.executeUpdate();
    } catch (SQLException e) {
      logger.severe("[存储] 保存岛屿失败: " + e.getMessage());
    }
  }

  @Override
  public void delete(Island island) {
    String sql = "DELETE FROM aubade_islands WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, island.getUniqueId().toString());
      ps.executeUpdate();
    } catch (SQLException e) {
      logger.severe("[存储] 删除岛屿失败: " + e.getMessage());
    }
  }

  public Optional<Island> findById(UUID id) {
    String sql = "SELECT * FROM aubade_islands WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, id.toString());
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return Optional.of(mapRow(rs));
      }
    } catch (SQLException e) {
      logger.severe("[存储] 查询岛屿失败: " + e.getMessage());
    }
    return Optional.empty();
  }

  public Optional<Island> findByOwner(UUID owner) {
    String sql = "SELECT * FROM aubade_islands WHERE owner = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, owner.toString());
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return Optional.of(mapRow(rs));
      }
    } catch (SQLException e) {
      logger.severe("[存储] 按所有者查询岛屿失败: " + e.getMessage());
    }
    return Optional.empty();
  }

  private Island mapRow(ResultSet rs) throws SQLException {
    UUID id = UUID.fromString(rs.getString("id"));
    UUID owner = UUID.fromString(rs.getString("owner"));
    String worldName = rs.getString("world");
    World world = Bukkit.getWorld(worldName);
    int cx = rs.getInt("center_x");
    int cy = rs.getInt("center_y");
    int cz = rs.getInt("center_z");
    Location center = world != null ? new Location(world, cx, cy, cz) : null;
    int protectionRange = rs.getInt("protection_range");
    int range = rs.getInt("range");

    // 注意：gameMode 在这里先传 null，上层缓存会回填
    Island island = new Island(id, owner, center, protectionRange, range, world, null);
    island.setName(rs.getString("name"));
    island.setDescription(rs.getString("description"));
    island.setLocked(rs.getBoolean("locked"));
    island.setPurgeProtected(rs.getBoolean("purge_protected"));
    return island;
  }
}

