# 蓝图定制指南

## 什么是蓝图

蓝图定义了玩家创建岛屿时的初始地形。Aubade 支持 JSON 和 YAML 两种格式。

## 蓝图目录

将蓝图文件放入 `plugins/Aubade/blueprints/` 目录，重启或重载后会自动扫描。

## YAML 格式

```yaml
id: tree_island
name: "树岛"
description: "一棵大树和小平台的进阶开局"
icon: OAK_SAPLING
blocks:
  - { x: 0, y: 0, z: 0, material: GRASS_BLOCK }
  - { x: 0, y: 1, z: 0, material: OAK_LOG }
  - { x: 0, y: 2, z: 0, material: OAK_LOG }
```

### 字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | 字符串 | 蓝图唯一标识，用于命令和配置引用 |
| `name` | 字符串 | 显示名称 |
| `description` | 字符串 | 描述 |
| `icon` | 字符串 | 图标 Material 名称 |
| `blocks` | 列表 | 方块列表，每项包含 `x`, `y`, `z`, `material` |

## 使用自定义蓝图

```
/island create tree_island
```

或在 `config.yml` 中设置默认蓝图：

```yaml
general:
  default-blueprint: tree_island
```

## 蓝图设计建议

- 中心点 `(0, 0, 0)` 对应岛屿中心，Y 轴向上
- 保持蓝图小巧（< 1000 方块），大型蓝图使用异步粘贴
- 务必包含初始物资（箱子、树苗等），避免玩家陷入死局
