package xuanmo.aubade.api.player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import xuanmo.aubade.api.storage.DataObject;

/**
 * 玩家数据对象。
 */
public class SkyPlayer implements DataObject {

  private final UUID uuid;
  private UUID islandId;
  private UUID lastIsland;
  private int deaths;
  private int resets;
  private long totalOnlineTime;
  private long lastLogin;
  private long lastLogout;
  private boolean autoPickup;
  private String locale = "zh_cn";
  private Map<String, String> addonData = new HashMap<>();

  public SkyPlayer(UUID uuid) {
    this.uuid = uuid;
    this.deaths = 0;
    this.resets = 0;
    this.totalOnlineTime = 0;
    this.autoPickup = false;
  }

  public UUID getUuid() {
    return uuid;
  }

  public UUID getIslandId() {
    return islandId;
  }

  public void setIslandId(UUID islandId) {
    this.islandId = islandId;
  }

  public UUID getLastIsland() {
    return lastIsland;
  }

  public void setLastIsland(UUID lastIsland) {
    this.lastIsland = lastIsland;
  }

  public int getDeaths() {
    return deaths;
  }

  public void setDeaths(int deaths) {
    this.deaths = deaths;
  }

  public int getResets() {
    return resets;
  }

  public void setResets(int resets) {
    this.resets = resets;
  }

  public long getTotalOnlineTime() {
    return totalOnlineTime;
  }

  public void setTotalOnlineTime(long totalOnlineTime) {
    this.totalOnlineTime = totalOnlineTime;
  }

  public long getLastLogin() {
    return lastLogin;
  }

  public void setLastLogin(long lastLogin) {
    this.lastLogin = lastLogin;
  }

  public long getLastLogout() {
    return lastLogout;
  }

  public void setLastLogout(long lastLogout) {
    this.lastLogout = lastLogout;
  }

  public boolean isAutoPickup() {
    return autoPickup;
  }

  public void setAutoPickup(boolean autoPickup) {
    this.autoPickup = autoPickup;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public Map<String, String> getAddonData() {
    return addonData;
  }

  public void setAddonData(Map<String, String> addonData) {
    this.addonData = addonData;
  }

  public <T> Optional<T> getAddonData(String addonId, Class<T> clazz) {
    String json = addonData.get(addonId);
    if (json == null) {
      return Optional.empty();
    }
    // TODO: Gson 反序列化
    return Optional.empty();
  }

  public void setAddonData(String addonId, Object data) {
    // TODO: Gson 序列化
    addonData.put(addonId, data.toString());
  }
}
