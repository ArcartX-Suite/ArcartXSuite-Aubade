package xuanmo.aubade.core.ui;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * UI 注册表。
 * 记录已注册 UI 的元数据：逻辑 ID → 运行时 ID / 文件。
 */
public class UiRegistry {

  private final Map<String, UiEntry> entries = new HashMap<>();

  public void record(String uiId, File sourceFile, String runtimeUiId) {
    entries.put(uiId, new UiEntry(uiId, sourceFile, runtimeUiId));
  }

  public String resolveRuntimeId(String uiId) {
    UiEntry entry = entries.get(uiId);
    return entry != null ? entry.runtimeUiId : uiId;
  }

  public boolean isRegistered(String uiId) {
    return entries.containsKey(uiId);
  }

  public File getSourceFile(String uiId) {
    UiEntry entry = entries.get(uiId);
    return entry != null ? entry.sourceFile : null;
  }

  private record UiEntry(String uiId, File sourceFile, String runtimeUiId) {
  }
}

