package xuanmo.aubade.api.ui;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 数据包载荷构建器。
 */
public class PacketPayloadBuilder {

  private final Map<String, Object> payload = new LinkedHashMap<>();

  public PacketPayloadBuilder put(String key, Object value) {
    payload.put(key, value);
    return this;
  }

  public Map<String, Object> build() {
    return payload;
  }
}
