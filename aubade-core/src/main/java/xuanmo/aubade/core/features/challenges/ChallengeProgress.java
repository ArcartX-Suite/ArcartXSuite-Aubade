package xuanmo.aubade.core.features.challenges;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 玩家/岛屿的挑战进度。
 */
public class ChallengeProgress {

  private final UUID islandId;
  private final Set<String> completed = new HashSet<>();
  private final Map<String, Integer> currentProgress = new HashMap<>();
  private final Map<String, Integer> repeatCount = new HashMap<>();

  public ChallengeProgress(UUID islandId) {
    this.islandId = islandId;
  }

  public boolean isCompleted(String challengeId) {
    return completed.contains(challengeId);
  }

  public void complete(String challengeId) {
    completed.add(challengeId);
  }

  public int getProgress(String challengeId) {
    return currentProgress.getOrDefault(challengeId, 0);
  }

  public void setProgress(String challengeId, int value) {
    currentProgress.put(challengeId, value);
  }

  public void addProgress(String challengeId, int delta) {
    currentProgress.put(challengeId, getProgress(challengeId) + delta);
  }

  public int getRepeatCount(String challengeId) {
    return repeatCount.getOrDefault(challengeId, 0);
  }

  public void incrementRepeat(String challengeId) {
    repeatCount.put(challengeId, getRepeatCount(challengeId) + 1);
  }

  public UUID getIslandId() {
    return islandId;
  }

  public Set<String> getCompleted() {
    return new HashSet<>(completed);
  }
}

