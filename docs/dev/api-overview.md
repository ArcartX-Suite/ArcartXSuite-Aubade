# API 概览

## 模块结构

```
aubade-api/
├── addon/       # 组件接口（GameModeAddon, FeatureAddon, ExtensionAddon）
├── island/      # 岛屿数据模型（Island, IslandManager, IslandMember）
├── player/      # 玩家数据模型
├── world/       # 世界配置接口
├── command/     # 命令基类
├── event/       # 事件体系
├── permission/  # 权限常量
├── storage/     # 持久层接口
└── ui/          # UI 桥接抽象
```

## 获取 IslandManager

```java
// 通过 Bukkit ServicesManager
IslandManager manager = Bukkit.getServicesManager().load(IslandManager.class);
```

## 创建自定义组件

```java
public class MyAddon extends AbstractExtensionAddon {
    @Override
    public String getExtensionId() {
        return "my_addon";
    }

    @Override
    public void onEnable() {
        // 注册监听器、初始化数据等
    }
}
```

## 事件列表

- `IslandCreateEvent` — 岛屿创建
- `IslandDeleteEvent` — 岛屿删除
- `IslandEnterEvent` — 玩家进入岛屿
- `IslandLeaveEvent` — 玩家离开岛屿
- `MemberJoinEvent` / `MemberLeaveEvent` — 成员变动
