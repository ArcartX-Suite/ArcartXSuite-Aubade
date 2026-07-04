# ArcartX-Aubade 开发蓝图 — 07 配置、诊断与路线图

**一句话总结**：移植 AXS 的 ConfigDiagnosticEngine 实现智能配置诊断，定义 6 阶段开发里程碑，以及 CI/CD、开源社区规范。

---

## 1. 配置诊断系统

### 1.1 设计目标

移植 AXS 的 `ConfigDiagnosticEngine`，实现用户配置的自动检测、修复和版本迁移，避免版本升级时配置丢失或冲突。

### 1.2 核心组件

| 组件 | 职责 |
|---|---|
| `ConfigDiagnosticEngine` | 主引擎，协调诊断流程 |
| `SyncPolicy` | 定义用户配置与 JAR 默认值的同步策略 |
| `ValidationRule` | 字段类型/范围/枚举校验规则 |
| `ConfigMigrator` | 跨版本配置迁移执行器 |
| `ConfigVersion` | 版本号管理 |

### 1.3 同步策略

```java
public class SyncPolicy {
    private final Set<String> dynamicSections;  // 用户自定义节，不覆盖
    private final boolean preserveComments;     // 保留用户注释
    private final boolean pruneRemoved;         // 剪除已删除字段
}
```

**动态节示例**：
```java
@Override
protected SyncPolicy defaultSyncPolicy() {
    return SyncPolicy.builder()
        .dynamicSection("worlds")           // 用户自定义世界配置
        .dynamicSection("blueprints")       // 用户自定义蓝图
        .dynamicSection("messages")         // 多语言消息
        .dynamicSection("block-values")     // 等级方块价值表
        .dynamicSection("challenges")       // 自定义挑战
        .dynamicSection("biomes")           // 自定义生物群系
        .dynamicSection("upgrades")         // 自定义升级项
        .preserveComments(true)
        .pruneRemoved(true)
        .build();
}
```

### 1.4 校验规则

```java
@Override
protected List<ValidationRule> mainConfigValidations() {
    return List.of(
        // 存储配置
        ValidationRule.required("storage.mode", ValueType.STRING)
            .withEnum(Set.of("sqlite", "mysql")),
        ValidationRule.required("storage.pool-size", ValueType.INT)
            .withRange(1, 100),
        
        // 岛屿默认配置
        ValidationRule.of("island.default-range", ValueType.INT)
            .withRange(10, 500),
        ValidationRule.of("island.max-range", ValueType.INT)
            .withRange(50, 1000),
        ValidationRule.of("island.spacing", ValueType.INT)
            .withRange(100, 2000),
        
        // 成员限制
        ValidationRule.of("island.max-members", ValueType.INT)
            .withRange(1, 100),
        
        // 经济配置
        ValidationRule.of("economy.starting-balance", ValueType.DOUBLE)
            .withRange(0.0, null),
        ValidationRule.of("economy.tax-rate", ValueType.DOUBLE)
            .withRange(0.0, 1.0),
        
        // 跨服配置
        ValidationRule.of("cross-server.enabled", ValueType.BOOLEAN),
        ValidationRule.of("cross-server.redis.host", ValueType.STRING),
        ValidationRule.of("cross-server.redis.port", ValueType.INT)
            .withRange(1, 65535),
        
        // 版本适配
        ValidationRule.of("version-adapter.mode", ValueType.STRING)
            .withEnum(Set.of("auto", "1_20_1", "1_20_4", "1_21_x"))
    );
}
```

### 1.5 版本迁移

```java
public class ConfigMigrator {
    private static final int CURRENT_VERSION = 2;
    
    public void migrate(File configFile) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        int current = config.getInt("config-version", 1);
        
        while (current < CURRENT_VERSION) {
            current++;
            applyMigration(config, current);
        }
        
        config.set("config-version", CURRENT_VERSION);
        config.save(configFile);
    }
    
    private void applyMigration(YamlConfiguration config, int version) {
        switch (version) {
            case 2 -> {
                // v1 -> v2: 重命名 island.protection-range -> island.default-range
                if (config.contains("island.protection-range")) {
                    config.set("island.default-range", config.get("island.protection-range"));
                    config.set("island.protection-range", null);
                }
            }
            // case 3 -> { ... }
        }
    }
}
```

### 1.6 诊断报告格式

```yaml
# 插件启动时输出到控制台
[SkyDream] 配置诊断报告 (v2):
  ✓ storage.mode: sqlite (用户配置，有效)
  ✓ island.default-range: 50 (用户配置，有效)
  ⚠ island.max-members: 200 (超出范围 1-100，已自动修正为 100)
  ✓ economy.tax-rate: 0.05 (用户配置，有效)
  ℹ 新增字段 island.spawn-protection: true (JAR 默认值)
  ℹ 删除字段 island.old-flag (已废弃，已剪除)
```

---

## 2. 版本适配策略

### 2.1 适配原则

| 策略 | 说明 |
|---|---|
| compileOnly | Gradle 中 `paper-api` 固定为 `1.20.1-R0.1-SNAPSHOT` |
| 运行时反射 | 高版本新增方块/实体/方法通过反射获取 |
| 版本检测 | 启动时检测服务端版本，加载对应适配器 |
|  gracefully 降级 | 某特性在新版本不存在时静默跳过 |

### 2.2 版本适配器

```java
public class VersionAdapter {
    private final String serverVersion;
    private final Map<String, MethodHandle> methodCache = new ConcurrentHashMap<>();
    
    public VersionAdapter(Plugin plugin) {
        this.serverVersion = Bukkit.getServer().getClass()
            .getPackage().getName().split("\\.")[3]; // v1_20_R1
    }
    
    public boolean isVersionAtLeast(int major, int minor) {
        // 解析并比较版本
    }
    
    // 示例：获取高版本 BlockData
    public Object getBlockData(Material material, String data) {
        if (isVersionAtLeast(1, 20)) {
            return material.createBlockData(data);
        }
        return null; // 旧版本回退
    }
}
```

### 2.3 支持版本矩阵

| 服务端版本 | Paper API | 状态 |
|---|---|---|
| 1.20.1 | 1.20.1-R0.1 | ✅ 完全支持 |
| 1.20.4 | 1.20.4-R0.1 | ✅ 反射适配 |
| 1.20.6 | 1.20.6-R0.1 | ✅ 反射适配 |
| 1.21.x | 1.21.x-R0.1 | ✅ 反射适配 |

---

## 3. 开发路线图

### 3.1 六阶段里程碑

#### 阶段 1：基础框架（4-6 周）

| 任务 | 说明 | 产出 |
|---|---|---|
| Gradle 多模块工程搭建 | 参考 AXS `settings.gradle.kts` 和模块 build 脚本 | 可编译的工程骨架 |
| aubade-api 设计 | Island/Player/World 接口 + Addon 生命周期 | API jar |
| aubade-core 基础 | 插件主类、生命周期、配置加载 | 可运行的空插件 |
| 数据库层 | HikariCP + JdbcRepository + Migration | 完整表结构 |
| 基础命令框架 | CommandManager + CompositeCommand | `/island help` |
| **里程碑** | 插件可加载，命令可用，配置可读写 | — |

#### 阶段 2：岛屿核心（4-6 周）

| 任务 | 说明 |
|---|---|
| IslandManager 实现 | 创建/删除/查询/缓存 |
| 岛屿网格算法 | IslandGrid 坐标计算，间距保护 |
| 保护系统 | BlockBreak/Place/Interact 事件拦截 |
| 成员管理 | 邀请/踢出/角色/权限 |
| 世界管理 | WorldFactory 动态创建世界 |
| SkyBlock GameMode | 第一个游戏模式完整实现 |
| 蓝图系统 | JSON 解析 + 异步粘贴 |
| **里程碑** | 玩家可创建岛屿，有保护，可邀请成员 | — |

#### 阶段 3：ArcartX UI 集成（3-4 周）

| 任务 | 说明 |
|---|---|
| ArcartXUiBridge 实现 | 反射封装 PacketBridgeAPI |
| 核心 UI 开发 | main/admin/create/top/invite/panel/settings |
| UI PacketHandler | init/update/action 三阶段处理 |
| 降级机制 | 无 ArcartX 时回退到 Bukkit Inventory |
| **里程碑** | 所有管理操作可通过 ArcartX UI 完成 | — |

#### 阶段 4：功能组件（6-8 周）

| 任务 | 说明 |
|---|---|
| Level 组件 | 方块扫描 + 价值表 + HUD |
| Challenges 组件 | 多类型挑战 + 进度 + 奖励 |
| Teams 组件 | 成员管理 UI + 权限矩阵 |
| Warps 组件 | 传送牌 + 公共传送面板 |
| Bank 组件 | 岛屿银行 + 存取 UI |
| Biomes 组件 | 群系更换 + 选择 UI |
| Border 组件 | 边界显示 + 权限控制 |
| **里程碑** | 8 个核心功能组件全部可用 | — |

#### 阶段 5：扩展组件 + 联动（4-6 周）

| 任务 | 说明 |
|---|---|
| 扩展组件开发 | InvSwitcher/IslandFly/Limits/Likes/Upgrades/Visit 等 |
| AXS 联动实现 | 注册 IslandQueryable/Economy/Permission 能力 |
| ArcartX 深度集成 | 路标/飘字/贴图/按键/聊天卡片 |
| 跨服同步 | Redis 同步 + 跨服排行榜 |
| **里程碑** | 与 AXS 和 ArcartX 的联动全部可用 | — |

#### 阶段 6：打磨与开源（3-4 周）

| 任务 | 说明 |
|---|---|
| 全模块联调 | 6 种游戏模式 × 8 功能组件 × 18 扩展组件 |
| 跨版本测试 | 1.20.1 / 1.20.4 / 1.21.x |
| 性能优化 | 异步扫描、缓存命中率、数据库连接池 |
| 中文文档 | 快速开始/安装/配置/开发指南 |
| GitHub Actions CI | 构建/测试/发布 Release |
| 开源规范 | LICENSE/CONTRIBUTING/CODE_OF_CONDUCT |
| **里程碑** | GitHub Release v1.0.0 发布 | — |

### 3.2 时间线估算

```
月份:  第1月    第2月    第3月    第4月    第5月    第6月
阶段:  [====1====][====2====][==3==][======4======][====5====][==6==]
       基础框架   岛屿核心   UI集成   功能组件       扩展联动    打磨开源
```

---

## 4. CI/CD 规范

### 4.1 GitHub Actions 工作流

```yaml
# .github/workflows/build.yml
name: Build & Release
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21', distribution: 'temurin' }
      - run: ./gradlew build
      - run: ./gradlew test
      - uses: actions/upload-artifact@v4
        with:
          name: Aubade
          path: aubade-core/build/libs/*.jar
```

### 4.2 发布规范

| 版本类型 | 格式 | 说明 |
|---|---|---|
| 预览版 | `1.0.0-beta.1` | 功能不完整，供测试 |
| 候选版 | `1.0.0-rc.1` | 功能完整，进入测试期 |
| 正式版 | `1.0.0` | 稳定可用 |
| 补丁版 | `1.0.1` | Bug 修复 |

---

## 5. 开源规范

### 5.1 仓库结构

```
ArcartX-Aubade/
├── .github/
│   ├── workflows/build.yml
│   ├── ISSUE_TEMPLATE/bug_report.md
│   ├── ISSUE_TEMPLATE/feature_request.md
│   └── PULL_REQUEST_TEMPLATE.md
├── docs/
│   ├── guide/
│   ├── dev/
│   ├── architecture/
│   └── appendix/
├── src/
│   ├── main/java/
│   └── main/resources/
├── LICENSE (GPL-3.0)
├── CONTRIBUTING.md
├── CODE_OF_CONDUCT.md
├── README.md
└── build.gradle.kts
```

### 5.2 代码规范

- **语言**：中文注释，英文标识符
- **缩进**：2 空格（参考 AXS）
- **包名**：`xuanmo.aubade.*`
- **类名**：PascalCase，接口不加 `I` 前缀
- **方法名**：camelCase，中文注释
- **常量**：UPPER_SNAKE_CASE

### 5.3 Issue 标签

| 标签 | 说明 |
|---|---|
| `bug` | Bug 报告 |
| `feature` | 功能请求 |
| `documentation` | 文档相关 |
| `good first issue` | 新手友好 |
| `help wanted` | 需要帮助 |
| `AXS-integration` | AXS 联动相关 |
| `ArcartX-UI` | ArcartX UI 相关 |

---

## 状态与优先级

- **状态**：策划中
- **优先级**：P0（贯穿全部开发阶段）

---

## 文件间引用

- [← 01 项目定位与架构总览](aubade-01-overview-d32eb4.md)
- [← 02 ArcartX/AXS 深度联动](aubade-02-arcartx-integration-d32eb4.md)
- [← 03 核心数据模型与数据库](aubade-03-datamodel-d32eb4.md)
- [← 04 游戏模式设计](aubade-04-gamemodes-d32eb4.md)
- [← 05 功能与扩展组件](aubade-05-features-d32eb4.md)
- [← 06 UI 与命令系统](aubade-06-ui-commands-d32eb4.md)
