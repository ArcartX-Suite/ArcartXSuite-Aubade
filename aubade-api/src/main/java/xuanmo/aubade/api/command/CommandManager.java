package xuanmo.aubade.api.command;

/**
 * 命令管理接口。
 * 负责注册和注销命令。
 */
public interface CommandManager {

  /**
   * 注册一个子命令。
   *
   * @param parentLabel 父命令标签，如 "island"
   * @param subCommand  子命令实现
   */
  void registerSubCommand(String parentLabel, CompositeCommand subCommand);
}
