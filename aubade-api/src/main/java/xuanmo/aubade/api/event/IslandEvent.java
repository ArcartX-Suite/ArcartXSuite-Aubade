package xuanmo.aubade.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xuanmo.aubade.api.island.Island;

/**
 * 岛屿事件基类。
 */
public abstract class IslandEvent extends Event {

  private static final HandlerList handlers = new HandlerList();
  private final Island island;

  protected IslandEvent(Island island) {
    this.island = island;
  }

  public Island getIsland() {
    return island;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
