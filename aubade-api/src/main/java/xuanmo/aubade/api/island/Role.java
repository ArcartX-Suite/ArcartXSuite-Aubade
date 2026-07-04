package xuanmo.aubade.api.island;

import java.util.Set;

/**
 * 岛屿角色枚举。
 */
public enum Role {
  OWNER("岛主", 100, Set.of(IslandPermission.values())),
  SUB_OWNER("副岛主", 80, Set.of(
      IslandPermission.BREAK, IslandPermission.PLACE, IslandPermission.INTERACT,
      IslandPermission.USE_CONTAINER, IslandPermission.USE_REDSTONE,
      IslandPermission.SPAWN_MOBS, IslandPermission.PVP, IslandPermission.FLY,
      IslandPermission.ANIMAL_SPAWN, IslandPermission.MONSTER_SPAWN
  )),
  MEMBER("成员", 50, Set.of(
      IslandPermission.BREAK, IslandPermission.PLACE, IslandPermission.INTERACT,
      IslandPermission.USE_CONTAINER, IslandPermission.FLY
  )),
  VISITOR("访客", 0, Set.of(IslandPermission.INTERACT));

  private final String displayName;
  private final int priority;
  private final Set<IslandPermission> permissions;

  Role(String displayName, int priority, Set<IslandPermission> permissions) {
    this.displayName = displayName;
    this.priority = priority;
    this.permissions = permissions;
  }

  public String getDisplayName() {
    return displayName;
  }

  public int getPriority() {
    return priority;
  }

  public boolean hasPermission(IslandPermission perm) {
    return permissions.contains(perm);
  }
}
