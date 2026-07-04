package xuanmo.aubade.core.features.warps;

import java.util.UUID;
import org.bukkit.Location;

/**
 * 传送牌数据对象。
 */
public class Warp {

  private final UUID id;
  private final UUID islandId;
  private String name;
  private String displayName;
  private Location location;
  private boolean publicWarp;
  private String icon;

  public Warp(UUID id, UUID islandId, String name, Location location) {
    this.id = id;
    this.islandId = islandId;
    this.name = name;
    this.displayName = name;
    this.location = location;
    this.publicWarp = false;
  }

  public UUID getId() {
    return id;
  }

  public UUID getIslandId() {
    return islandId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public boolean isPublicWarp() {
    return publicWarp;
  }

  public void setPublicWarp(boolean publicWarp) {
    this.publicWarp = publicWarp;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }
}

