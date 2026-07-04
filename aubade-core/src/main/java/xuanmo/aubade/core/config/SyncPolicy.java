package xuanmo.aubade.core.config;

import java.util.HashSet;
import java.util.Set;

/**
 * 配置同步策略。
 * 决定用户配置与 jar 默认配置之间的合并行为。
 */
public class SyncPolicy {

  private final boolean pruneRemovedPaths;
  private final Set<String> protectedPaths;
  private final Set<String> dynamicSections;

  private SyncPolicy(boolean pruneRemovedPaths, Set<String> protectedPaths, Set<String> dynamicSections) {
    this.pruneRemovedPaths = pruneRemovedPaths;
    this.protectedPaths = protectedPaths;
    this.dynamicSections = dynamicSections;
  }

  public static Builder builder() {
    return new Builder();
  }

  public boolean isPruneRemovedPaths() {
    return pruneRemovedPaths;
  }

  public Set<String> getProtectedPaths() {
    return protectedPaths;
  }

  public Set<String> getDynamicSections() {
    return dynamicSections;
  }

  public boolean isDynamicSection(String path) {
    return dynamicSections.contains(path);
  }

  public static class Builder {
    private boolean pruneRemovedPaths = true;
    private final Set<String> protectedPaths = new HashSet<>();
    private final Set<String> dynamicSections = new HashSet<>();

    public Builder pruneRemovedPaths(boolean prune) {
      this.pruneRemovedPaths = prune;
      return this;
    }

    public Builder protect(String path) {
      this.protectedPaths.add(path);
      return this;
    }

    public Builder dynamicSection(String path) {
      this.dynamicSections.add(path);
      return this;
    }

    public SyncPolicy build() {
      return new SyncPolicy(pruneRemovedPaths, protectedPaths, dynamicSections);
    }
  }
}

