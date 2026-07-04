# 生命周期架构

## 启动流程

```
AubadePlugin.onEnable()
  └─ CoreLifecycleManager.onEnable()
       ├─ 加载配置 (CoreConfig)
       ├─ 初始化数据库 (StorageManager)
       ├─ 初始化 AddonLifecycleManager
       ├─ 注册功能组件 (Level, Teams, Bank...)
       ├─ 注册扩展组件 (IslandFly, Chat, Upgrades...)
       ├─ 启用所有组件
       ├─ 注册 PAPI 占位符
       └─ 注册 AXS 能力
```

## 组件生命周期

每个组件经历以下阶段：

1. **Load** — 类加载、配置读取
2. **Enable** — 注册监听器、初始化数据
3. **Reload** — 重载配置（不重启插件）
4. **Disable** — 注销监听器、保存数据、清理资源

## 依赖解析

组件可通过 `AddonDescriptor` 声明依赖关系：

```java
AddonDescriptor.builder("bank")
    .depends(List.of("teams"))      // 硬依赖
    .softDepends(List.of("level"))  // 软依赖
    .build();
```

`DependencyResolver` 在启用前进行拓扑排序，确保依赖组件先启用。
