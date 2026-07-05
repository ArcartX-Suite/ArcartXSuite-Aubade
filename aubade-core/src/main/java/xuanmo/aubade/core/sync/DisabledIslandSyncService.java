package xuanmo.aubade.core.sync;

import java.util.UUID;

public final class DisabledIslandSyncService implements IslandSyncService {

  public static final DisabledIslandSyncService INSTANCE = new DisabledIslandSyncService();

  private DisabledIslandSyncService() {
  }

  @Override
  public void start() {
  }

  @Override
  public void publishUpsert(UUID islandId, UUID ownerId) {
  }

  @Override
  public void publishDelete(UUID islandId, UUID ownerId) {
  }

  @Override
  public void close() {
  }
}
