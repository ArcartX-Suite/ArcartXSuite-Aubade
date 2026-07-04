package xuanmo.aubade.api.island;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.World;
import xuanmo.aubade.api.addon.GameModeAddon;
import xuanmo.aubade.api.storage.DataObject;

/**
 * 岛屿数据对象。
 */
public class Island implements DataObject {

  private final UUID uniqueId;
  private UUID owner;
  private String name;
  private String description;
  private Location center;
  private int protectionRange;
  private int range;
  private World world;
  private GameModeAddon gameMode;

  private Map<UUID, IslandMember> members = new ConcurrentHashMap<>();
  private Set<UUID> bannedPlayers = ConcurrentHashMap.newKeySet();
  private Set<UUID> trustedPlayers = ConcurrentHashMap.newKeySet();

  private boolean locked;
  private boolean purgeProtected;
  private long createdTime;
  private long lastLoginTime;

  private volatile long level;
  private volatile double bankBalance;
  private volatile int likes;

  private Map<String, Boolean> flags = new ConcurrentHashMap<>();
  private Map<String, String> meta = new ConcurrentHashMap<>();

  public Island(UUID uniqueId, UUID owner, Location center, int protectionRange, int range, World world, GameModeAddon gameMode) {
    this.uniqueId = uniqueId;
    this.owner = owner;
    this.center = center;
    this.protectionRange = protectionRange;
    this.range = range;
    this.world = world;
    this.gameMode = gameMode;
    this.createdTime = System.currentTimeMillis();
    this.locked = false;
    this.purgeProtected = false;
    this.level = 0;
    this.bankBalance = 0.0;
    this.likes = 0;
  }

  public UUID getUniqueId() {
    return uniqueId;
  }

  public UUID getOwner() {
    return owner;
  }

  public void setOwner(UUID owner) {
    this.owner = owner;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Location getCenter() {
    return center;
  }

  public void setCenter(Location center) {
    this.center = center;
  }

  public int getProtectionRange() {
    return protectionRange;
  }

  public void setProtectionRange(int protectionRange) {
    this.protectionRange = protectionRange;
  }

  public int getRange() {
    return range;
  }

  public void setRange(int range) {
    this.range = range;
  }

  public World getWorld() {
    return world;
  }

  public void setWorld(World world) {
    this.world = world;
  }

  public GameModeAddon getGameMode() {
    return gameMode;
  }

  public void setGameMode(GameModeAddon gameMode) {
    this.gameMode = gameMode;
  }

  public Map<UUID, IslandMember> getMembers() {
    return members;
  }

  public Set<UUID> getBannedPlayers() {
    return bannedPlayers;
  }

  public Set<UUID> getTrustedPlayers() {
    return trustedPlayers;
  }

  public boolean isLocked() {
    return locked;
  }

  public void setLocked(boolean locked) {
    this.locked = locked;
  }

  public boolean isPurgeProtected() {
    return purgeProtected;
  }

  public void setPurgeProtected(boolean purgeProtected) {
    this.purgeProtected = purgeProtected;
  }

  public long getCreatedTime() {
    return createdTime;
  }

  public long getLastLoginTime() {
    return lastLoginTime;
  }

  public void setLastLoginTime(long lastLoginTime) {
    this.lastLoginTime = lastLoginTime;
  }

  public long getLevel() {
    return level;
  }

  public void setLevel(long level) {
    this.level = level;
  }

  public double getBankBalance() {
    return bankBalance;
  }

  public void setBankBalance(double bankBalance) {
    this.bankBalance = bankBalance;
  }

  public int getLikes() {
    return likes;
  }

  public void setLikes(int likes) {
    this.likes = likes;
  }

  public Map<String, Boolean> getFlags() {
    return flags;
  }

  public Map<String, String> getMeta() {
    return meta;
  }

  public boolean hasPermission(UUID player, IslandPermission perm) {
    if (player.equals(owner)) {
      return true;
    }
    IslandMember member = members.get(player);
    if (member == null) {
      return false;
    }
    return member.getRole().hasPermission(perm);
  }

  public boolean inProtectionRange(Location loc) {
    if (center == null || loc == null) {
      return false;
    }
    if (!center.getWorld().equals(loc.getWorld())) {
      return false;
    }
    return center.distanceSquared(loc) <= protectionRange * protectionRange;
  }
}
