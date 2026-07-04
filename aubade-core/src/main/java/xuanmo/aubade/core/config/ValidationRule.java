package xuanmo.aubade.core.config;

import java.util.function.Predicate;

/**
 * 配置验证规则。
 * 移植自 AXS 配置诊断引擎的简化版。
 */
public class ValidationRule {

  private final String path;
  private final ValueType expectedType;
  private final boolean required;
  private final Object min;
  private final Object max;
  private final Predicate<Object> customCheck;
  private final String errorMessage;

  private ValidationRule(String path, ValueType expectedType, boolean required, Object min, Object max,
      Predicate<Object> customCheck, String errorMessage) {
    this.path = path;
    this.expectedType = expectedType;
    this.required = required;
    this.min = min;
    this.max = max;
    this.customCheck = customCheck;
    this.errorMessage = errorMessage;
  }

  public static ValidationRule of(String path, ValueType type) {
    return new ValidationRule(path, type, false, null, null, null, null);
  }

  public static ValidationRule required(String path, ValueType type) {
    return new ValidationRule(path, type, true, null, null, null, null);
  }

  public ValidationRule min(Object min) {
    return new ValidationRule(path, expectedType, required, min, max, customCheck, errorMessage);
  }

  public ValidationRule max(Object max) {
    return new ValidationRule(path, expectedType, required, min, max, customCheck, errorMessage);
  }

  public ValidationRule check(Predicate<Object> predicate, String message) {
    return new ValidationRule(path, expectedType, required, min, max, predicate, message);
  }

  // Getters
  public String getPath() {
    return path;
  }

  public ValueType getExpectedType() {
    return expectedType;
  }

  public boolean isRequired() {
    return required;
  }

  public Object getMin() {
    return min;
  }

  public Object getMax() {
    return max;
  }

  public Predicate<Object> getCustomCheck() {
    return customCheck;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}

