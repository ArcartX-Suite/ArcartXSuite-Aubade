package xuanmo.aubade.core.features.challenges;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.AbstractFeatureAddon;

/**
 * 挑战系统功能组件。
 */
public class ChallengesAddon extends AbstractFeatureAddon {

  private final Map<String, Challenge> challenges = new HashMap<>();
  private final Map<UUID, ChallengeProgress> progressMap = new HashMap<>();

  public ChallengesAddon(AubadeCore core) {
    super(core, AddonDescriptor.builder("challenges")
        .name("挑战系统")
        .version("1.0.0")
        .mainClass(ChallengesAddon.class.getName())
        .build());
  }

  @Override
  public String getFeatureId() {
    return "challenges";
  }

  @Override
  public String getFriendlyName() {
    return "挑战系统";
  }

  @Override
  public void onLoad() {
    // 加载内置挑战
    loadDefaultChallenges();
  }

  @Override
  public void onEnable() {
    super.onEnable();
    registerUi("challenges_list.yml", "challenges_list");
    registerUi("challenge_detail.yml", "challenge_detail");
    core.getLogger().info("[Challenges] 挑战系统组件已启用。");
  }

  @Override
  public void onDisable() {
    super.onDisable();
    core.getLogger().info("[Challenges] 挑战系统组件已禁用。");
  }

  public Challenge getChallenge(String id) {
    return challenges.get(id);
  }

  public List<Challenge> getAllChallenges() {
    return new ArrayList<>(challenges.values());
  }

  public ChallengeProgress getProgress(UUID islandId) {
    return progressMap.computeIfAbsent(islandId, ChallengeProgress::new);
  }

  private void loadDefaultChallenges() {
    challenges.put("collect_cobble", new Challenge("collect_cobble", "收集圆石", "收集 64 个圆石",
        ChallengeType.COLLECT, Map.of("minecraft:cobblestone", 64),
        List.of(new Reward(Reward.RewardType.EXP, "", 100, "100 经验值")),
        true, 10, List.of(), "{\"id\":\"minecraft:cobblestone\",\"Count\":1}"));

    challenges.put("place_torch", new Challenge("place_torch", "放置火把", "放置 16 个火把",
        ChallengeType.PLACE, Map.of("minecraft:torch", 16),
        List.of(new Reward(Reward.RewardType.ITEM, "minecraft:coal", 8, "8 个煤炭")),
        true, 5, List.of(), "{\"id\":\"minecraft:torch\",\"Count\":1}"));

    challenges.put("kill_zombie", new Challenge("kill_zombie", "击杀僵尸", "击杀 10 只僵尸",
        ChallengeType.KILL, Map.of("minecraft:zombie", 10),
        List.of(new Reward(Reward.RewardType.MONEY, "", 50, "50 金币")),
        true, 20, List.of(), "{\"id\":\"minecraft:rotten_flesh\",\"Count\":1}"));
  }

  private void registerUi(String fileName, String uiId) {
    File uiDir = new File(core.getDataFolder(), "arcartx/ui");
    File uiFile = new File(uiDir, fileName);
    if (!uiFile.exists()) {
      core.saveResource("arcartx/ui/" + fileName, false);
    }
    getUiManager().registerUi(uiId, uiId, uiFile);
  }
}

