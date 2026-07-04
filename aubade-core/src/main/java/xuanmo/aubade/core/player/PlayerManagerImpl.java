package xuanmo.aubade.core.player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.aubade.player.PlayerManager;
import xuanmo.arcartxsuite.api.aubade.player.SkyPlayer;
import xuanmo.aubade.core.storage.JdbcPlayerRepository;

/**
 * 玩家管理器实现。
 */
public class PlayerManagerImpl implements PlayerManager {

  private final Map<UUID, SkyPlayer> onlineCache = new ConcurrentHashMap<>();
  private final JdbcPlayerRepository repository;

  public PlayerManagerImpl(JdbcPlayerRepository repository) {
    this.repository = repository;
  }

  @Override
  public SkyPlayer getPlayer(Player player) {
    UUID uuid = player.getUniqueId();
    SkyPlayer sp = onlineCache.get(uuid);
    if (sp != null) {
      return sp;
    }
    Optional<SkyPlayer> fromDb = repository.findById(uuid);
    if (fromDb.isPresent()) {
      sp = fromDb.get();
    } else {
      sp = new SkyPlayer(uuid);
      repository.save(sp);
    }
    onlineCache.put(uuid, sp);
    return sp;
  }

  @Override
  public Optional<SkyPlayer> getPlayer(UUID uuid) {
    SkyPlayer sp = onlineCache.get(uuid);
    if (sp != null) {
      return Optional.of(sp);
    }
    return repository.findById(uuid);
  }

  @Override
  public void savePlayer(SkyPlayer player) {
    repository.save(player);
  }

  @Override
  public void unloadPlayer(UUID uuid) {
    SkyPlayer sp = onlineCache.remove(uuid);
    if (sp != null) {
      repository.save(sp);
    }
  }
}

