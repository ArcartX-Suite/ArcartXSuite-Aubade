package xuanmo.aubade.core.ui.packet;

import java.util.List;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xuanmo.arcartxsuite.api.ClientPacketHandler;
import xuanmo.aubade.core.AubadeCore;

public final class AdminUiPacketDispatcher implements ClientPacketHandler {

  private final List<BasePacketHandler> handlers;

  public AdminUiPacketDispatcher(AubadeCore core) {
    this.handlers = List.of(
        new AdminUiPacketHandler(core),
        new IslandUiPacketHandler(core)
    );
  }

  @Override
  public boolean handleClientPacket(@NotNull Player player, @NotNull String packetId, @NotNull List<String> data) {
    for (BasePacketHandler handler : handlers) {
      if (handler.handle(player, packetId, data)) {
        return true;
      }
    }
    return false;
  }
}
