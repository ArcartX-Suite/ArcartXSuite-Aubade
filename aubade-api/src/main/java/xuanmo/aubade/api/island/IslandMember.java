package xuanmo.aubade.api.island;

import java.util.UUID;

/**
 * 岛屿成员。
 */
public class IslandMember {

  private final UUID playerUUID;
  private Role role;
  private long joinedTime;
  private int trustLevel;

  public IslandMember(UUID playerUUID, Role role) {
    this.playerUUID = playerUUID;
    this.role = role;
    this.joinedTime = System.currentTimeMillis();
    this.trustLevel = 0;
  }

  public UUID getPlayerUUID() {
    return playerUUID;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public long getJoinedTime() {
    return joinedTime;
  }

  public int getTrustLevel() {
    return trustLevel;
  }

  public void setTrustLevel(int trustLevel) {
    this.trustLevel = trustLevel;
  }

  public boolean canManage() {
    return role == Role.OWNER || role == Role.SUB_OWNER;
  }
}
