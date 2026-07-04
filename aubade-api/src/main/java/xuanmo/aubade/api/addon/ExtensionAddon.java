package xuanmo.aubade.api.addon;

/**
 * 扩展组件接口。
 * 比功能组件更轻量，通常不独立提供 UI，而是增强或修改现有机制。
 */
public interface ExtensionAddon extends SkyAddon {

  /** 扩展唯一标识，如 "island_fly" */
  String getExtensionId();

  /** 显示名称，如 "岛屿飞行" */
  String getFriendlyName();
}
