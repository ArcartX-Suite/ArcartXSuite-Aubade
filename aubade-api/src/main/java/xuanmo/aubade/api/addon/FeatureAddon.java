package xuanmo.aubade.api.addon;

/**
 * 功能组件接口。
 * 提供具体功能（如等级、挑战、传送等）。
 */
public interface FeatureAddon extends SkyAddon {

  /** 功能唯一标识，如 "level" */
  String getFeatureId();

  /** 显示名称，如 "岛屿等级" */
  String getFriendlyName();
}
