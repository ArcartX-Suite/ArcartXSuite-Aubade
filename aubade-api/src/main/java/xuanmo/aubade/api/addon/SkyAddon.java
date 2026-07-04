package xuanmo.aubade.api.addon;

/**
 * 组件生命周期接口。
 * 所有游戏模式组件和功能组件均需实现此接口。
 */
public interface SkyAddon {

  /** 组件描述符 */
  AddonDescriptor descriptor();

  /** 加载阶段：配置初始化 */
  void onLoad();

  /** 启用阶段：注册命令、监听器、UI */
  void onEnable();

  /** 关闭阶段 */
  void onDisable();

  /** 重载阶段 */
  void onReload();

  /** 是否已启用 */
  boolean isEnabled();

  /** 是否为游戏模式 */
  default boolean isGameMode() {
    return this instanceof GameModeAddon;
  }

  /** 是否为功能组件 */
  default boolean isFeature() {
    return this instanceof FeatureAddon;
  }
}
