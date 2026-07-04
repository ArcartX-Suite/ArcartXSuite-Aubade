package xuanmo.aubade.core.integration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;
import xuanmo.arcartxsuite.api.aubade.capability.IslandEconomyCapable;
import xuanmo.arcartxsuite.api.aubade.capability.IslandPermissionCheckable;
import xuanmo.arcartxsuite.api.aubade.capability.IslandQueryable;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.arcartxsuite.api.aubade.island.IslandPermission;
import xuanmo.aubade.core.AubadeCore;

public class AubadeCapabilityProvider implements IslandQueryable, IslandEconomyCapable, IslandPermissionCheckable {

  private final AubadeCore core;

  public AubadeCapabilityProvider(AubadeCore core) {
    this.core = core;
  }

  @Override
  public Optional<Island> getPlayerIsland(UUID player) {
    return core.getIslandManager().getIslandByOwner(player);
  }

  @Override
  public Optional<Island> getIslandAt(Location location) {
    return core.getIslandManager().getIslandAt(location);
  }

  @Override
  public List<Island> getAllIslands() {
    return core.getIslandManager().getIslandsInWorld(null);
  }

  @Override
  public int getIslandCount() {
    return core.getIslandManager().getIslandCount();
  }

  @Override
  public double getBalance(UUID islandId) {
    Optional<Island> opt = core.getIslandManager().getIslandById(islandId);
    return opt.map(Island::getBankBalance).orElse(0.0);
  }

  @Override
  public boolean deposit(UUID islandId, double amount) {
    if (amount <= 0) {
      return false;
    }
    Optional<Island> opt = core.getIslandManager().getIslandById(islandId);
    if (opt.isEmpty()) {
      return false;
    }
    Island island = opt.get();
    island.setBankBalance(island.getBankBalance() + amount);
    core.getIslandManager().saveIsland(island);
    return true;
  }

  @Override
  public boolean withdraw(UUID islandId, double amount) {
    if (amount <= 0) {
      return false;
    }
    Optional<Island> opt = core.getIslandManager().getIslandById(islandId);
    if (opt.isEmpty()) {
      return false;
    }
    Island island = opt.get();
    if (island.getBankBalance() < amount) {
      return false;
    }
    island.setBankBalance(island.getBankBalance() - amount);
    core.getIslandManager().saveIsland(island);
    return true;
  }

  @Override
  public boolean transfer(UUID fromIslandId, UUID toIslandId, double amount) {
    return withdraw(fromIslandId, amount) && deposit(toIslandId, amount);
  }

  @Override
  public boolean hasPermission(UUID player, IslandPermission permission) {
    Optional<Island> opt = core.getIslandManager().getIslandByOwner(player);
    return opt.map(island -> island.hasPermission(player, permission)).orElse(false);
  }

  @Override
  public boolean canAccess(UUID player, UUID islandId) {
    Optional<Island> opt = core.getIslandManager().getIslandById(islandId);
    return opt.map(island -> !island.isLocked()).orElse(false);
  }

  @Override
  public boolean isMember(UUID player, UUID islandId) {
    Optional<Island> opt = core.getIslandManager().getIslandById(islandId);
    if (opt.isEmpty()) {
      return false;
    }
    Island island = opt.get();
    return island.getOwner().equals(player) || island.getMembers().containsKey(player);
  }

  @Override
  public boolean isOwner(UUID player, UUID islandId) {
    Optional<Island> opt = core.getIslandManager().getIslandById(islandId);
    return opt.map(island -> island.getOwner().equals(player)).orElse(false);
  }
}

