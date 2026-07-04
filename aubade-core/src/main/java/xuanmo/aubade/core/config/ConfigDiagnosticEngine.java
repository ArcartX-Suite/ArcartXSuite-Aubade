package xuanmo.aubade.core.config;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 配置诊断引擎。
 * 移植自 AXS 的简化版：对比 jar 默认配置与用户运行时配置，
 * 自动修复缺失字段、清理已删除字段、报告类型错误。
 */
public class ConfigDiagnosticEngine {

  private final JavaPlugin plugin;
  private final Logger logger;
  private final List<ValidationRule> rules = new ArrayList<>();
  private final Map<String, DiagnosisResult> results = new HashMap<>();

  public ConfigDiagnosticEngine(JavaPlugin plugin) {
    this.plugin = plugin;
    this.logger = plugin.getLogger();
  }

  /**
   * 注册验证规则。
   */
  public void addRule(ValidationRule rule) {
    rules.add(rule);
  }

  /**
   * 对指定配置文件执行诊断与修复。
   *
   * @param fileName    用户配置文件名，如 "config.yml"
   * @param syncPolicy  同步策略
   * @return 诊断结果
   */
  public DiagnosisReport diagnose(String fileName, SyncPolicy syncPolicy) {
    File userFile = new File(plugin.getDataFolder(), fileName);
    YamlConfiguration userConfig = YamlConfiguration.loadConfiguration(userFile);

    // 加载 jar 内默认配置
    YamlConfiguration jarDefault = loadJarDefault(fileName);
    if (jarDefault == null) {
      logger.warning("[配置诊断] 未在 jar 中找到默认配置: " + fileName);
      return new DiagnosisReport(fileName, List.of(), false);
    }

    List<DiagnosisResult> localResults = new ArrayList<>();
    boolean modified = false;

    // 1. 自动添加缺失字段
    for (String key : jarDefault.getKeys(true)) {
      if (!userConfig.contains(key)) {
        userConfig.set(key, jarDefault.get(key));
        localResults.add(new DiagnosisResult(key, DiagnosisType.ADDED, "从 jar 默认值自动补充"));
        modified = true;
      }
    }

    // 2. 清理已删除字段（依据 SyncPolicy）
    if (syncPolicy.isPruneRemovedPaths()) {
      for (String key : new ArrayList<>(userConfig.getKeys(true))) {
        if (!jarDefault.contains(key) && !syncPolicy.isDynamicSection(key)) {
          userConfig.set(key, null);
          localResults.add(new DiagnosisResult(key, DiagnosisType.REMOVED, "jar 默认配置中已不存在，已清理"));
          modified = true;
        }
      }
    }

    // 3. 执行自定义验证规则
    for (ValidationRule rule : rules) {
      String path = rule.getPath();
      if (!userConfig.contains(path)) {
        if (rule.isRequired()) {
          localResults.add(new DiagnosisResult(path, DiagnosisType.ERROR, "必填字段缺失"));
        }
        continue;
      }

      Object value = userConfig.get(path);
      if (!checkType(value, rule.getExpectedType())) {
        localResults.add(new DiagnosisResult(path, DiagnosisType.ERROR,
            "类型不匹配，期望: " + rule.getExpectedType()));
      }

      if (rule.getMin() != null && value instanceof Number num) {
        double min = ((Number) rule.getMin()).doubleValue();
        if (num.doubleValue() < min) {
          localResults.add(new DiagnosisResult(path, DiagnosisType.ERROR, "数值低于最小值 " + min));
        }
      }

      if (rule.getMax() != null && value instanceof Number num) {
        double max = ((Number) rule.getMax()).doubleValue();
        if (num.doubleValue() > max) {
          localResults.add(new DiagnosisResult(path, DiagnosisType.ERROR, "数值超过最大值 " + max));
        }
      }

      if (rule.getCustomCheck() != null && !rule.getCustomCheck().test(value)) {
        localResults.add(new DiagnosisResult(path, DiagnosisType.ERROR,
            rule.getErrorMessage() != null ? rule.getErrorMessage() : "自定义验证失败"));
      }
    }

    // 保存修复后的配置
    if (modified) {
      try {
        userConfig.save(userFile);
        logger.info("[配置诊断] 已自动修复配置并保存: " + fileName);
      } catch (Exception e) {
        logger.severe("[配置诊断] 保存配置失败: " + e.getMessage());
      }
    }

    return new DiagnosisReport(fileName, localResults, modified);
  }

  private YamlConfiguration loadJarDefault(String fileName) {
    try (InputStream is = plugin.getResource(fileName)) {
      if (is == null) {
        return null;
      }
      return YamlConfiguration.loadConfiguration(new InputStreamReader(is, StandardCharsets.UTF_8));
    } catch (Exception e) {
      logger.severe("[配置诊断] 加载 jar 默认配置失败: " + e.getMessage());
      return null;
    }
  }

  private boolean checkType(Object value, ValueType expected) {
    if (value == null) {
      return !expected.equals(ValueType.STRING); // null 只允许非 STRING 作为缺失处理
    }
    return switch (expected) {
      case STRING -> value instanceof String;
      case INTEGER -> value instanceof Integer || value instanceof Long || value instanceof Short;
      case DOUBLE -> value instanceof Double || value instanceof Float || value instanceof Number;
      case BOOLEAN -> value instanceof Boolean;
      case LIST -> value instanceof java.util.List;
      case MAP -> value instanceof java.util.Map;
    };
  }

  public enum DiagnosisType {
    ADDED, REMOVED, MODIFIED, ERROR, OK
  }

  public record DiagnosisResult(String path, DiagnosisType type, String message) {
  }

  public record DiagnosisReport(String fileName, List<DiagnosisResult> results, boolean repaired) {
  }
}

