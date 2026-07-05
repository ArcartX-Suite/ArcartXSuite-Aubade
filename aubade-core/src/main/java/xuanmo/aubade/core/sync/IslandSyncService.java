package xuanmo.aubade.core.sync;

import java.util.UUID;

public interface IslandSyncService extends AutoCloseable {

  void start();

  void publishUpsert(UUID islandId, UUID ownerId);

  void publishDelete(UUID islandId, UUID ownerId);

  @Override
  void close();
}
