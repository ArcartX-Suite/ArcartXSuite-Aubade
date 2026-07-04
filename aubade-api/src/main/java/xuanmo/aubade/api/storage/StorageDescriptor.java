package xuanmo.aubade.api.storage;

/**
 * 存储描述符。
 */
public record StorageDescriptor(String dialect, String host, int port, String database, String username, String password) {
}
