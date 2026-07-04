package xuanmo.aubade.core.features.likes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.AbstractExtensionAddon;

/**
 * 岛屿点赞扩展组件。
 * 记录每个岛屿的点赞数，带玩家反刷机制（单个玩家对同一岛屿只能点赞一次）。
 */
public class LikesAddon extends AbstractExtensionAddon {

  // islandId -> like count
  private final Map<UUID, Integer> likesCount = new ConcurrentHashMap<>();
  // islandId -> set of player UUIDs who liked
  private final Map<UUID, java.util.Set<UUID>> likesRecord = new ConcurrentHashMap<>();

  public LikesAddon(AubadeCore core) {
    super(core, AddonDescriptor.builder("likes")
        .name("岛屿点赞")
        .version("1.0.0")
        .mainClass(LikesAddon.class.getName())
        .build());
  }

  @Override
  public String getExtensionId() {
    return "likes";
  }

  @Override
  public String getFriendlyName() {
    return "岛屿点赞";
  }

  @Override
  public void onEnable() {
    super.onEnable();
    core.getLogger().info("[Likes] 岛屿点赞扩展已启用。");
  }

  @Override
  public void onDisable() {
    super.onDisable();
    core.getLogger().info("[Likes] 岛屿点赞扩展已禁用。");
  }

  /**
   * 为岛屿点赞。
   *
   * @return true 为首次点赞，false 为已点过赞
   */
  public boolean like(UUID islandId, UUID playerId) {
    java.util.Set<UUID> set = likesRecord.computeIfAbsent(islandId, k -> ConcurrentHashMap.newKeySet());
    if (!set.add(playerId)) {
      return false; // 已点过赞
    }
    likesCount.merge(islandId, 1, Integer::sum);
    // 同步到 Island 对象
    getIslandManager().getIslandById(islandId).ifPresent(island -> {
      island.setLikes(likesCount.getOrDefault(islandId, 0));
      getIslandManager().saveIsland(island);
    });
    return true;
  }

  /**
   * 取消点赞。
   */
  public boolean unlike(UUID islandId, UUID playerId) {
    java.util.Set<UUID> set = likesRecord.get(islandId);
    if (set == null || !set.remove(playerId)) {
      return false;
    }
    likesCount.merge(islandId, -1, Integer::sum);
    getIslandManager().getIslandById(islandId).ifPresent(island -> {
      island.setLikes(likesCount.getOrDefault(islandId, 0));
      getIslandManager().saveIsland(island);
    });
    return true;
  }

  public int getLikes(UUID islandId) {
    return likesCount.getOrDefault(islandId, 0);
  }

  public boolean hasLiked(UUID islandId, UUID playerId) {
    java.util.Set<UUID> set = likesRecord.get(islandId);
    return set != null && set.contains(playerId);
  }
}

