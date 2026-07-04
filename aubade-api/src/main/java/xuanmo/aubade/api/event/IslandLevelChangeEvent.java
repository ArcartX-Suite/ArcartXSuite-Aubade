package xuanmo.aubade.api.event;

import org.bukkit.event.HandlerList;
import xuanmo.aubade.api.island.Island;

/**
 * 岛屿等级变更事件。
 */
public class IslandLevelChangeEvent extends IslandEvent {

  private static final HandlerList handlers = new HandlerList();
  private final long oldLevel;
  private final long newLevel;

  public IslandLevelChangeEvent(Island island, long oldLevel, long newLevel) {
    super(island);
    this.oldLevel = oldLevel;
    this.newLevel = newLevel;
  }

  public long getOldLevel() {
    return oldLevel;
  }

  public long getNewLevel() {
    return newLevel;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
