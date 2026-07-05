package xuanmo.aubade.core.sync;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.config.CoreConfig;
import xuanmo.aubade.core.island.IslandFactory;
import xuanmo.aubade.core.island.IslandGrid;
import xuanmo.aubade.core.island.IslandManagerImpl;
import xuanmo.aubade.core.storage.JdbcIslandRepository;
import xuanmo.arcartxsuite.api.aubade.addon.GameModeAddon;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.arcartxsuite.api.aubade.island.IslandMember;
import xuanmo.arcartxsuite.api.aubade.island.Role;
import xuanmo.arcartxsuite.api.crossserver.CrossServerAPI;
import xuanmo.arcartxsuite.api.crossserver.CrossServerChannel;
import xuanmo.arcartxsuite.api.crossserver.CrossServerChannelConfig;
import xuanmo.arcartxsuite.api.crossserver.CrossServerDelivery;

class IslandCrossServerSyncMockBukkitTest {

  private ServerMock server;
  private World world;
  private FakeCrossServerBus bus;
  private SharedIslandRepository sharedRepository;

  @BeforeEach
  void setUp() {
    server = MockBukkit.mock();
    world = new WorldMock(Material.BEDROCK, 3);
    bus = new FakeCrossServerBus();
    sharedRepository = new SharedIslandRepository();
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  void upsertPropagatesAndReloadsStaleCache() throws Exception {
    NodeContext nodeA = createNode("node-a");
    NodeContext nodeB = createNode("node-b");
    PlayerMock owner = server.addPlayer("owner");
    Island island = createIsland(owner.getUniqueId(), "original");
    sharedRepository.save(island);
    assertEquals("original", nodeB.manager.getIslandById(island.getUniqueId()).orElseThrow().getName());

    island.setName("renamed");
    nodeA.manager.saveIsland(island);

    assertEquals(1, bus.publishCount());
    assertEquals("renamed", nodeB.manager.getIslandById(island.getUniqueId()).orElseThrow().getName());
    assertTrue(nodeB.manager.getIslandByOwner(owner.getUniqueId()).isPresent());
  }

  @Test
  void deletePropagatesAndClearsCache() throws Exception {
    NodeContext nodeA = createNode("node-a");
    NodeContext nodeB = createNode("node-b");
    PlayerMock owner = server.addPlayer("owner");
    Island island = createIsland(owner.getUniqueId(), "to-delete");
    sharedRepository.save(island);
    assertTrue(nodeB.manager.getIslandByOwner(owner.getUniqueId()).isPresent());

    nodeA.manager.deleteIsland(island);

    assertEquals(1, bus.publishCount());
    assertTrue(nodeB.manager.getIslandById(island.getUniqueId()).isEmpty());
    assertTrue(nodeB.manager.getIslandByOwner(owner.getUniqueId()).isEmpty());
  }

  @Test
  void ignoreSelfPreventsLoopAndExtraPublish() throws Exception {
    NodeContext nodeA = createNode("node-a");
    NodeContext nodeB = createNode("node-b");
    PlayerMock owner = server.addPlayer("owner");
    Island island = createIsland(owner.getUniqueId(), "loop-check");
    sharedRepository.save(island);
    nodeB.manager.getIslandById(island.getUniqueId()).orElseThrow();

    island.setName("loop-check-updated");
    nodeA.manager.saveIsland(island);

    assertEquals(1, bus.publishCount());
    assertEquals("loop-check-updated", nodeB.manager.getIslandById(island.getUniqueId()).orElseThrow().getName());
  }

  @Test
  void disabledServiceIsNoOp() throws Exception {
    NodeContext node = createDisabledNode("node-disabled");
    PlayerMock owner = server.addPlayer("owner");
    Island island = createIsland(owner.getUniqueId(), "disabled");
    assertDoesNotThrow(() -> node.manager.saveIsland(island));
    assertEquals(0, bus.publishCount());
    assertDoesNotThrow(() -> node.manager.deleteIsland(island));
    assertEquals(0, bus.publishCount());
  }

  private NodeContext createNode(String nodeId) throws Exception {
    Path nodeDir = Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir")).resolve("aubade-sync-" + nodeId));
    JavaPlugin plugin = MockBukkit.createMockPlugin();
    AubadeCore core = new AubadeCore(plugin, nodeDir.toFile(), plugin.getClass().getClassLoader(), null);
    CoreConfig config = createConfig(nodeDir, true);
    core.coreConfig(config);
    IslandManagerImpl manager = new IslandManagerImpl(core, new IslandGrid(200), sharedRepository);
    core.islandManager(manager);
    CrossServerIslandSyncService syncService = new CrossServerIslandSyncService(core, new FakeCrossServerAPI(nodeId), config.getSyncChannel());
    core.islandSyncService(syncService);
    manager.setIslandSyncService(syncService);
    syncService.start();
    return new NodeContext(core, manager, syncService, plugin);
  }

  private NodeContext createDisabledNode(String nodeId) throws Exception {
    Path nodeDir = Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir")).resolve("aubade-sync-disabled-" + nodeId));
    JavaPlugin plugin = MockBukkit.createMockPlugin();
    AubadeCore core = new AubadeCore(plugin, nodeDir.toFile(), plugin.getClass().getClassLoader(), null);
    CoreConfig config = createConfig(nodeDir, false);
    core.coreConfig(config);
    IslandManagerImpl manager = new IslandManagerImpl(core, new IslandGrid(200), sharedRepository);
    core.islandManager(manager);
    DisabledIslandSyncService disabled = DisabledIslandSyncService.INSTANCE;
    core.islandSyncService(disabled);
    manager.setIslandSyncService(disabled);
    return new NodeContext(core, manager, disabled, plugin);
  }

  private CoreConfig createConfig(Path dir, boolean syncEnabled) throws Exception {
    Path configFile = dir.resolve("config.yml");
    YamlConfiguration yaml = new YamlConfiguration();
    yaml.set("storage.type", "sqlite");
    yaml.set("sync.enabled", syncEnabled);
    yaml.set("sync.channel", "aubade:island");
    yaml.save(configFile.toFile());
    CoreConfig config = new CoreConfig(configFile.toFile(), Bukkit.getLogger());
    config.load();
    return config;
  }

  private Island createIsland(UUID owner, String name) {
    IslandFactory factory = new IslandFactory(new IslandGrid(200));
    Island island = factory.create(owner, world, mockGameMode());
    island.setName(name);
    island.getMembers().put(owner, new IslandMember(owner, Role.OWNER));
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
                  IslandCrossServerSyncMockBukkitTest.class.getClassLoader(),
                  new Class<?>[] {method.getReturnType()},
                  worldSettingsHandler);
          case "getId" -> "skyblock";
          default -> method.getReturnType().isPrimitive() ? 0 : null;
        };
    return (GameModeAddon)
        Proxy.newProxyInstance(
            IslandCrossServerSyncMockBukkitTest.class.getClassLoader(),
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
      public java.util.logging.Logger getParentLogger() {
        return java.util.logging.Logger.getAnonymousLogger();
      }
    };
  }

  private final class NodeContext {
    private final AubadeCore core;
    private final IslandManagerImpl manager;
    private final IslandSyncService syncService;
    private final JavaPlugin plugin;

    private NodeContext(AubadeCore core, IslandManagerImpl manager, IslandSyncService syncService, JavaPlugin plugin) {
      this.core = core;
      this.manager = manager;
      this.syncService = syncService;
      this.plugin = plugin;
    }
  }

  private final class SharedIslandRepository extends JdbcIslandRepository {
    private final Map<UUID, Island> islands = new HashMap<>();
    private final List<UUID> deletedIds = new ArrayList<>();

    private SharedIslandRepository() {
      super(dummyDataSource());
    }

    @Override
    public void save(Island island) {
      islands.put(island.getUniqueId(), copy(island));
    }

    @Override
    public void delete(Island island) {
      islands.remove(island.getUniqueId());
      deletedIds.add(island.getUniqueId());
    }

    @Override
    public Optional<Island> findById(UUID id) {
      Island island = islands.get(id);
      return island == null ? Optional.empty() : Optional.of(copy(island));
    }

    @Override
    public Optional<Island> findByOwner(UUID owner) {
      return islands.values().stream()
          .filter(island -> owner.equals(island.getOwner()))
          .findFirst()
          .map(this::copy);
    }

    private Island copy(Island island) {
      org.bukkit.Location center = island.getCenter() == null ? null : island.getCenter().clone();
      Island copy = new Island(
          island.getUniqueId(),
          island.getOwner(),
          center,
          island.getProtectionRange(),
          island.getRange(),
          island.getWorld(),
          null);
      copy.setName(island.getName());
      copy.setDescription(island.getDescription());
      copy.getMembers().putAll(island.getMembers());
      copy.getMeta().putAll(island.getMeta());
      return copy;
    }
  }

  private final class FakeCrossServerAPI implements CrossServerAPI {
    private final String nodeId;

    private FakeCrossServerAPI(String nodeId) {
      this.nodeId = nodeId;
    }

    @Override
    public String nodeId() {
      return nodeId;
    }

    @Override
    public CrossServerChannel openChannel(String channel, CrossServerChannelConfig config, Consumer<CrossServerDelivery> consumer) {
      return bus.open(channel, nodeId, consumer);
    }
  }

  private final class FakeCrossServerBus {
    private final Map<String, List<FakeSubscription>> subscriptions = new HashMap<>();
    private final List<String> publishedPayloads = new CopyOnWriteArrayList<>();

    private CrossServerChannel open(String channel, String nodeId, Consumer<CrossServerDelivery> consumer) {
      FakeSubscription subscription = new FakeSubscription(channel, nodeId, consumer);
      subscriptions.computeIfAbsent(channel, key -> new ArrayList<>()).add(subscription);
      return new FakeChannel(subscription);
    }

    private void publish(String channel, String nodeId, String payload) {
      publishedPayloads.add(payload);
      List<FakeSubscription> channelSubscriptions = subscriptions.get(channel);
      if (channelSubscriptions == null) {
        return;
      }
      for (FakeSubscription subscription : new ArrayList<>(channelSubscriptions)) {
        if (!subscription.active) {
          continue;
        }
        subscription.consumer.accept(new CrossServerDelivery(channel, nodeId, UUID.randomUUID().toString(), payload));
      }
    }

    private int publishCount() {
      return publishedPayloads.size();
    }

    private final class FakeSubscription {
      private final String channel;
      private final String nodeId;
      private final Consumer<CrossServerDelivery> consumer;
      private boolean active = true;

      private FakeSubscription(String channel, String nodeId, Consumer<CrossServerDelivery> consumer) {
        this.channel = channel;
        this.nodeId = nodeId;
        this.consumer = consumer;
      }
    }

    private final class FakeChannel implements CrossServerChannel {
      private final FakeSubscription subscription;

      private FakeChannel(FakeSubscription subscription) {
        this.subscription = subscription;
      }

      @Override
      public String moduleId() {
        return subscription.channel;
      }

      @Override
      public boolean isActive() {
        return subscription.active;
      }

      @Override
      public void publish(String message) {
        if (!subscription.active) {
          return;
        }
        bus.publish(subscription.channel, subscription.nodeId, message);
      }

      @Override
      public void close() {
        subscription.active = false;
      }
    }
  }
}
