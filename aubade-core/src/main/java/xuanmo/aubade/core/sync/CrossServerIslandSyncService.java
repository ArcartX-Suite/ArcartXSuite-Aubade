package xuanmo.aubade.core.sync;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.island.IslandManagerImpl;
import xuanmo.arcartxsuite.api.crossserver.CrossServerAPI;
import xuanmo.arcartxsuite.api.crossserver.CrossServerChannel;
import xuanmo.arcartxsuite.api.crossserver.CrossServerChannelConfig;
import xuanmo.arcartxsuite.api.crossserver.CrossServerDelivery;

public final class CrossServerIslandSyncService implements IslandSyncService {

  private static final String DEFAULT_CHANNEL = "aubade:island";

  private final AubadeCore core;
  private final CrossServerAPI crossServer;
  private final String channelName;
  private final Logger logger;

  private CrossServerChannel channel;
  private String localNodeId;

  public CrossServerIslandSyncService(AubadeCore core, CrossServerAPI crossServer, String channelName) {
    this.core = core;
    this.crossServer = crossServer;
    this.channelName = channelName == null || channelName.isBlank() ? DEFAULT_CHANNEL : channelName;
    this.logger = core.getLogger();
  }

  @Override
  public synchronized void start() {
    if (channel != null) {
      return;
    }
    if (crossServer == null) {
      logger.fine("[跨服同步] CrossServerAPI 不可用，岛屿同步已禁用。");
      return;
    }
    try {
      this.localNodeId = crossServer.nodeId();
      this.channel = crossServer.openChannel(channelName, CrossServerChannelConfig.enabledDefault(), this::handleDelivery);
    } catch (Exception e) {
      logger.log(Level.WARNING, "[跨服同步] 打开跨服同步频道失败", e);
      this.channel = null;
    }
  }

  @Override
  public void publishUpsert(UUID islandId, UUID ownerId) {
    publish(EventType.UPSERT, islandId, ownerId);
  }

  @Override
  public void publishDelete(UUID islandId, UUID ownerId) {
    publish(EventType.DELETE, islandId, ownerId);
  }

  private void publish(EventType eventType, UUID islandId, UUID ownerId) {
    if (islandId == null) {
      return;
    }
    CrossServerChannel current = channel;
    if (current == null || !current.isActive()) {
      return;
    }
    try {
      current.publish(encode(eventType, islandId, ownerId));
    } catch (Exception e) {
      logger.log(Level.WARNING, "[跨服同步] 发布岛屿同步消息失败: " + islandId, e);
    }
  }

  private void handleDelivery(CrossServerDelivery delivery) {
    if (delivery == null || delivery.payload() == null || delivery.payload().isBlank()) {
      return;
    }
    String effectiveLocalNodeId = localNodeId != null ? localNodeId : safeNodeId();
    String deliveryNodeId = delivery.nodeId();
    if (deliveryNodeId != null && deliveryNodeId.equals(effectiveLocalNodeId)) {
      return;
    }
    SyncMessage message = decode(delivery.payload());
    if (message == null) {
      return;
    }
    Runnable task = () -> applyMessage(message);
    if (Bukkit.isPrimaryThread()) {
      task.run();
    } else {
      Bukkit.getScheduler().runTask(core.plugin(), task);
    }
  }

  private void applyMessage(SyncMessage message) {
    IslandManagerImpl islandManager = core.getIslandManager();
    if (islandManager == null) {
      return;
    }
    if (message.eventType == EventType.DELETE) {
      islandManager.applyRemoteDelete(message.islandId, message.ownerId);
    } else {
      islandManager.applyRemoteUpsert(message.islandId, message.ownerId);
    }
  }

  private String encode(EventType eventType, UUID islandId, UUID ownerId) {
    String sourceNodeId = localNodeId != null ? localNodeId : safeNodeId();
    String owner = ownerId != null ? ownerId.toString() : "";
    return eventType.name() + "|" + islandId + "|" + owner + "|" + sourceNodeId + "|" + System.currentTimeMillis();
  }

  private SyncMessage decode(String payload) {
    String[] parts = payload.split("\\|", -1);
    if (parts.length < 5) {
      logger.warning("[跨服同步] 收到无法解析的同步消息: " + payload);
      return null;
    }
    try {
      EventType eventType = EventType.valueOf(parts[0]);
      UUID islandId = UUID.fromString(parts[1]);
      UUID ownerId = parts[2].isBlank() ? null : UUID.fromString(parts[2]);
      String sourceNodeId = parts[3];
      long timestamp = Long.parseLong(parts[4]);
      return new SyncMessage(eventType, islandId, ownerId, sourceNodeId, timestamp);
    } catch (Exception e) {
      logger.log(Level.WARNING, "[跨服同步] 解析同步消息失败: " + payload, e);
      return null;
    }
  }

  private String safeNodeId() {
    try {
      return crossServer != null ? crossServer.nodeId() : "";
    } catch (Exception e) {
      return "";
    }
  }

  @Override
  public synchronized void close() {
    if (channel == null) {
      return;
    }
    try {
      channel.close();
    } catch (Exception e) {
      logger.log(Level.WARNING, "[跨服同步] 关闭跨服同步频道失败", e);
    } finally {
      channel = null;
    }
  }

  private enum EventType {
    UPSERT,
    DELETE
  }

  private static final class SyncMessage {
    private final EventType eventType;
    private final UUID islandId;
    private final UUID ownerId;
    private final String sourceNodeId;
    private final long timestamp;

    private SyncMessage(EventType eventType, UUID islandId, UUID ownerId, String sourceNodeId, long timestamp) {
      this.eventType = eventType;
      this.islandId = islandId;
      this.ownerId = ownerId;
      this.sourceNodeId = sourceNodeId;
      this.timestamp = timestamp;
    }
  }
}
