package xuanmo.aubade.core.island;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.config.CoreConfig;
import xuanmo.aubade.core.lifecycle.AddonLifecycleManager;
import xuanmo.aubade.core.lifecycle.CoreLifecycleManager;
import xuanmo.aubade.core.player.PlayerManagerImpl;
import xuanmo.aubade.core.storage.JdbcIslandRepository;
import xuanmo.aubade.core.storage.JdbcPlayerRepository;
import xuanmo.aubade.core.storage.StorageManager;
import xuanmo.aubade.core.ui.UiManager;
import xuanmo.aubade.core.ui.packet.AdminUiPacketDispatcher;
import xuanmo.aubade.core.warp.IslandWarpHelper;
import xuanmo.arcartxsuite.api.aubade.addon.GameModeAddon;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.arcartxsuite.api.aubade.island.IslandMember;
import xuanmo.arcartxsuite.api.aubade.island.Role;
import xuanmo.arcartxsuite.api.aubade.player.SkyPlayer;
import xuanmo.arcartxsuite.api.bridge.PacketBridgeAPI;

class IslandTransferRoutingMockBukkitTest {

  private ServerMock server;
  private AubadeCore core;
  private TestIslandRepository islandRepository;
  private TestPlayerManager playerManager;
  private TestLifecycleManager lifecycleManager;
  private TestUiManager uiManager;
  private IslandManagerImpl islandManager;
  private AdminUiPacketDispatcher dispatcher;

  @BeforeEach
  void setUp(@TempDir Path tempDir) {
    server = MockBukkit.mock();
    JavaPlugin plugin = MockBukkit.createMockPlugin();
    new WorldMock(Material.BEDROCK, 3);
    core = new AubadeCore(plugin, tempDir.toFile(), plugin.getClass().getClassLoader(), dummyPacketBridge());
    CoreConfig config = new CoreConfig(tempDir.resolve("config.yml").toFile(), plugin.getLogger());
    config.load();
    core.coreConfig(config);

    islandRepository = new TestIslandRepository();
    playerManager = new TestPlayerManager();
    islandManager = new IslandManagerImpl(core, new IslandGrid(200), islandRepository);
    lifecycleManager = new TestLifecycleManager(core, config, islandManager);
    core.lifecycleManager(lifecycleManager);
    core.playerManager(playerManager);
    uiManager = new TestUiManager(core);
    core.uiManager(uiManager);
    core.islandManager(islandManager);
    dispatcher = new AdminUiPacketDispatcher(core);
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  void transferIslandSwapsOwnerAndPersists() {
    PlayerMock owner = server.addPlayer("owner");
    PlayerMock target = server.addPlayer("target");
    Island island = createIsland(owner);
    playerManager.getPlayer(owner).setIslandId(island.getUniqueId());
    playerManager.getPlayer(target).setIslandId(island.getUniqueId());
    island.getMembers().put(target.getUniqueId(), new IslandMember(target.getUniqueId(), Role.MEMBER));
    islandManager.saveIsland(island);

    assertTrue(islandManager.transferIsland(island, target.getUniqueId()));

    assertEquals(target.getUniqueId(), island.getOwner());
    assertEquals(Role.MEMBER, island.getMembers().get(owner.getUniqueId()).getRole());
    assertEquals(Role.OWNER, island.getMembers().get(target.getUniqueId()).getRole());
    assertTrue(islandManager.getIslandByOwner(owner.getUniqueId()).isEmpty());
    assertTrue(islandManager.getIslandByOwner(target.getUniqueId()).isPresent());
    assertEquals(island.getUniqueId(), playerManager.getPlayer(owner).getIslandId());
    assertEquals(island.getUniqueId(), playerManager.getPlayer(target).getIslandId());
    assertEquals(2, islandRepository.saveCount);
    assertEquals(target.getUniqueId(), islandRepository.findById(island.getUniqueId()).orElseThrow().getOwner());
  }

  @Test
  void transferIslandRejectsNonMemberTarget() {
    PlayerMock owner = server.addPlayer("owner");
    PlayerMock target = server.addPlayer("target");
    Island island = createIsland(owner);
    playerManager.getPlayer(owner).setIslandId(island.getUniqueId());
    playerManager.getPlayer(target).setIslandId(island.getUniqueId());
    islandManager.saveIsland(island);

    assertFalse(islandManager.transferIsland(island, target.getUniqueId()));
    assertEquals(owner.getUniqueId(), island.getOwner());
    assertTrue(islandManager.getIslandByOwner(owner.getUniqueId()).isPresent());
    assertTrue(islandManager.getIslandByOwner(target.getUniqueId()).isEmpty());
    assertEquals(1, islandRepository.saveCount);
  }

  @Test
  void transferIslandRejectsCurrentOwnerTarget() {
    PlayerMock owner = server.addPlayer("owner");
    Island island = createIsland(owner);
    playerManager.getPlayer(owner).setIslandId(island.getUniqueId());
    islandManager.saveIsland(island);

    assertFalse(islandManager.transferIsland(island, owner.getUniqueId()));
    assertEquals(owner.getUniqueId(), island.getOwner());
    assertEquals(1, islandRepository.saveCount);
  }

  @Test
  void transferIslandRejectsNullInputs() {
    PlayerMock owner = server.addPlayer("owner");
    Island island = createIsland(owner);
    islandManager.saveIsland(island);

    assertFalse(islandManager.transferIsland(null, UUID.randomUUID()));
    assertFalse(islandManager.transferIsland(island, null));
    assertEquals(1, islandRepository.saveCount);
  }

  @Test
  void renameConfirmRoutesThroughDispatcher() {
    PlayerMock owner = server.addPlayer("owner");
    Island island = createIsland(owner);
    playerManager.getPlayer(owner).setIslandId(island.getUniqueId());
    islandManager.saveIsland(island);

    assertTrue(dispatcher.handleClientPacket(owner, "SKYDREAM_TEAM_RENAME_CONFIRM", List.of("name", "新的岛名")));
    assertEquals("新的岛名", island.getName());
    assertEquals(2, islandRepository.saveCount);
    assertTrue(uiManager.sentPackets.contains("aubade_main:update"));
    assertTrue(uiManager.sentPackets.contains("team_settings:init"));
  }

  @Test
  void warpCreateConfirmRoutesThroughDispatcher() {
    PlayerMock owner = server.addPlayer("owner");
    Island island = createIsland(owner);
    owner.teleport(island.getCenter().clone());
    playerManager.getPlayer(owner).setIslandId(island.getUniqueId());
    islandManager.saveIsland(island);

    assertTrue(dispatcher.handleClientPacket(owner, "SKYDREAM_WARP_CREATE_CONFIRM", List.of("name", "spawn")));
    assertTrue(IslandWarpHelper.getWarps(island).containsKey("spawn"));
    assertEquals(2, islandRepository.saveCount);
    assertTrue(uiManager.sentPackets.contains("warp_board:init"));
  }

  @Test
  void transferConfirmRoutesThroughDispatcher() {
    PlayerMock owner = server.addPlayer("owner");
    PlayerMock target = server.addPlayer("target");
    Island island = createIsland(owner);
    playerManager.getPlayer(owner).setIslandId(island.getUniqueId());
    playerManager.getPlayer(target).setIslandId(island.getUniqueId());
    island.getMembers().put(target.getUniqueId(), new IslandMember(target.getUniqueId(), Role.MEMBER));
    islandManager.saveIsland(island);

    assertTrue(dispatcher.handleClientPacket(owner, "SKYDREAM_TEAM_TRANSFER_CONFIRM", List.of("target", target.getUniqueId().toString())));
    assertEquals(target.getUniqueId(), island.getOwner());
    assertEquals(Role.OWNER, island.getMembers().get(target.getUniqueId()).getRole());
    assertEquals(Role.MEMBER, island.getMembers().get(owner.getUniqueId()).getRole());
    assertEquals(2, islandRepository.saveCount);
    assertTrue(uiManager.sentPackets.contains("aubade_main:update"));
  }

  private Island createIsland(PlayerMock owner) {
    IslandFactory factory = new IslandFactory(new IslandGrid(200));
    Island island = factory.create(owner.getUniqueId(), server.getWorlds().get(0), mockGameMode());
    assertNotNull(island);
    return island;
  }

  private static GameModeAddon mockGameMode() {
    InvocationHandler worldSettingsHandler =
        (proxy, method, args) -> switch (method.getName()) {
          case "getDefaultProtectionRange" -> 50;
          case "getMaxIslandSize" -> 200;
          default -> method.getReturnType().isPrimitive() ? 0 : null;
        };
    InvocationHandler gameModeHandler =
        (proxy, method, args) -> switch (method.getName()) {
          case "getWorldSettings" ->
              Proxy.newProxyInstance(
                  IslandTransferRoutingMockBukkitTest.class.getClassLoader(),
                  new Class<?>[] {method.getReturnType()},
                  worldSettingsHandler);
          case "getId" -> "skyblock";
          default -> method.getReturnType().isPrimitive() ? 0 : null;
        };
    return (GameModeAddon)
        Proxy.newProxyInstance(
            IslandTransferRoutingMockBukkitTest.class.getClassLoader(),
            new Class<?>[] {GameModeAddon.class},
            gameModeHandler);
  }

  private static DataSource dummyDataSource() {
    return new DataSource() {
      @Override
      public Connection getConnection() throws SQLException {
        throw new SQLFeatureNotSupportedException("dummy");
      }

      @Override
      public Connection getConnection(String username, String password) throws SQLException {
        throw new SQLFeatureNotSupportedException("dummy");
      }

      @Override
      public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException("dummy");
      }

      @Override
      public boolean isWrapperFor(Class<?> iface) {
        return false;
      }

      @Override
      public java.io.PrintWriter getLogWriter() {
        return null;
      }

      @Override
      public void setLogWriter(java.io.PrintWriter out) {
      }

      @Override
      public void setLoginTimeout(int seconds) {
      }

      @Override
      public int getLoginTimeout() {
        return 0;
      }

      @Override
      public Logger getParentLogger() {
        return Logger.getLogger("Aubade");
      }
    };
  }

  private static PacketBridgeAPI dummyPacketBridge() {
    InvocationHandler handler =
        (proxy, method, args) -> {
          if ("isAvailable".equals(method.getName())) {
            return true;
          }
          return method.getReturnType().isPrimitive() ? false : null;
        };
    return (PacketBridgeAPI)
        Proxy.newProxyInstance(
            IslandTransferRoutingMockBukkitTest.class.getClassLoader(),
            new Class<?>[] {PacketBridgeAPI.class},
            handler);
  }

  private static final class TestLifecycleManager extends CoreLifecycleManager {
    private final CoreConfig config;
    private final IslandManagerImpl islandManager;

    private TestLifecycleManager(AubadeCore core, CoreConfig config, IslandManagerImpl islandManager) {
      super(core);
      this.config = config;
      this.islandManager = islandManager;
    }

    @Override
    public CoreConfig getCoreConfig() {
      return config;
    }

    @Override
    public AddonLifecycleManager getAddonLifecycleManager() {
      return null;
    }

    @Override
    public IslandManagerImpl getIslandManager() {
      return islandManager;
    }
  }

  private static final class TestPlayerManager extends PlayerManagerImpl {
    private final Map<UUID, SkyPlayer> players = new HashMap<>();

    private TestPlayerManager() {
      super(new JdbcPlayerRepository(dummyDataSource()));
    }

    @Override
    public SkyPlayer getPlayer(Player player) {
      return players.computeIfAbsent(player.getUniqueId(), SkyPlayer::new);
    }

    @Override
    public Optional<SkyPlayer> getPlayer(UUID uuid) {
      return Optional.ofNullable(players.get(uuid));
    }

    @Override
    public void savePlayer(SkyPlayer player) {
      players.put(player.getUuid(), player);
    }

    @Override
    public void unloadPlayer(UUID uuid) {
    }
  }

  private static final class TestIslandRepository extends JdbcIslandRepository {
    private final Map<UUID, Island> islands = new HashMap<>();
    private final java.util.Set<UUID> deletedIds = new java.util.HashSet<>();
    private int saveCount;

    private TestIslandRepository() {
      super(dummyDataSource());
    }

    @Override
    public void save(Island island) {
      saveCount++;
      islands.put(island.getUniqueId(), island);
    }

    @Override
    public void delete(Island island) {
      islands.remove(island.getUniqueId());
      deletedIds.add(island.getUniqueId());
    }

    @Override
    public Optional<Island> findById(UUID id) {
      return Optional.ofNullable(islands.get(id));
    }

    @Override
    public Optional<Island> findByOwner(UUID owner) {
      return islands.values().stream().filter(island -> owner.equals(island.getOwner())).findFirst();
    }
  }

  private static final class TestUiManager extends UiManager {
    private final List<String> sentPackets = new ArrayList<>();

    private TestUiManager(AubadeCore core) {
      super(core);
    }

    @Override
    public boolean openUi(Player player, String uiId) {
      return true;
    }

    @Override
    public boolean sendPacket(Player player, String uiId, String handler, Object payload) {
      sentPackets.add(uiId + ":" + handler);
      return true;
    }
  }
}
