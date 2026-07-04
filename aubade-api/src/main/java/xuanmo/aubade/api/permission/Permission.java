package xuanmo.aubade.api.permission;

/**
 * 权限常量。
 */
public final class Permission {

  public static final String PREFIX = "aubade.";

  // 玩家权限
  public static final String PLAYER = PREFIX + "player";
  public static final String PLAYER_CREATE = PREFIX + "player.create";
  public static final String PLAYER_DELETE = PREFIX + "player.delete";
  public static final String PLAYER_HOME = PREFIX + "player.home";
  public static final String PLAYER_SETHOME = PREFIX + "player.sethome";
  public static final String PLAYER_INVITE = PREFIX + "player.invite";
  public static final String PLAYER_ACCEPT = PREFIX + "player.accept";
  public static final String PLAYER_KICK = PREFIX + "player.kick";
  public static final String PLAYER_LEAVE = PREFIX + "player.leave";
  public static final String PLAYER_INFO = PREFIX + "player.info";
  public static final String PLAYER_TOP = PREFIX + "player.top";
  public static final String PLAYER_TRUST = PREFIX + "player.trust";
  public static final String PLAYER_UNTRUST = PREFIX + "player.untrust";
  public static final String PLAYER_BAN = PREFIX + "player.ban";
  public static final String PLAYER_UNBAN = PREFIX + "player.unban";
  public static final String PLAYER_SETTINGS = PREFIX + "player.settings";
  public static final String PLAYER_WARP = PREFIX + "player.warp";

  // 管理员权限
  public static final String ADMIN = PREFIX + "admin";
  public static final String ADMIN_RELOAD = PREFIX + "admin.reload";
  public static final String ADMIN_PURGE = PREFIX + "admin.purge";
  public static final String ADMIN_DELETE = PREFIX + "admin.delete";
  public static final String ADMIN_INFO = PREFIX + "admin.info";
  public static final String ADMIN_SETTINGS = PREFIX + "admin.settings";

  private Permission() {
  }
}
