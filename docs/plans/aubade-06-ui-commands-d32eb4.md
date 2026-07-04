# ArcartX-Aubade 开发蓝图 — 06 UI 与命令系统

**一句话总结**：SkyDream 全部 ArcartX UI 面板清单、设计规范、Packet 通信约定，以及 `/island` 命令树和 PAPI 占位符体系。

---

## 1. UI 设计规范

### 1.1 视觉风格

| 元素 | 规范 |
|---|---|
| 面板底色 | `rgba(45, 45, 55, 220)` 深蓝灰 |
| 描边 | `rgba(80, 180, 255, 180)` 青色 |
| 标题文字 | 49px，白色，`stroke: 2, color: 0,0,0` |
| 正文 | 36px，白色 |
| 输入框 | 28px，浅灰底色 |
| 辅助文字 | 22px，灰色 |
| 控件 ID | 全中文（如`岛屿列表`、`成员按钮`） |

### 1.2 字符串类型

- 纯文本：`'...'`
- 变量文本：`~...`
- 颜色表达式：不使用 `~` 前缀，直接写 Shimmer 颜色值

### 1.3 按钮子文本

```yaml
子文本:
  x: self.parent.width / 2
  y: 12
  through: true
  center: true
  fontSize: 22
```

---

## 2. UI 文件清单

### 2.1 核心 UI（aubade-core）

| UI 文件 | 用途 | 控件结构 | 数据包字段 |
|---|---|---|---|
| `aubade_main.yml` | 玩家主菜单 | 标题 + 功能按钮网格（我的岛屿/创建/加入/排行榜/设置） | `island`, `canCreate`, `top3[]` |
| `aubade_admin.yml` | 管理面板 | Tab 切换（全局设置/世界管理/玩家数据/岛屿列表/组件管理） | `worlds[]`, `players[]`, `islands[]` |
| `aubade_top.yml` | 排行榜 | VStack 列表 + 排名图标 | `category`, `top[]`（name, score, owner） |
| `aubade_create.yml` | 创建岛屿 | 蓝图选择网格（HGrid 3 列） | `blueprints[]`（id, name, icon, desc） |
| `aubade_invite.yml` | 邀请确认 | 玩家信息 + 接受/拒绝按钮 | `inviterName`, `islandName`, `expires` |

### 2.2 岛屿面板 UI

| UI 文件 | 组件 | 控件结构 |
|---|---|---|
| `island_panel.yml` | 核心 | 岛名标题 + 信息栏 + 功能按钮（成员/设置/升级/传送/银行/挑战） |
| `island_settings.yml` | 核心 | 开关列表（锁定/PvP/访客/生物生成）+ 范围显示 + 描述编辑 |
| `member_manage.yml` | Teams | 成员列表(VStack) + 权限编辑弹窗 + 邀请输入框 |
| `team_settings.yml` | Teams | 角色权限矩阵 + 副岛主设置 + 转让岛主 |
| `island_bank.yml` | Bank | 余额显示 + 存取输入 + 交易历史(VStack) |
| `level_display.yml` | Level | HUD 进度条 + 等级数字（`isHud: true`） |
| `level_top.yml` | Level | 排名列表 + 自己的排名高亮 |

### 2.3 功能组件 UI

| UI 文件 | 组件 | 控件结构 |
|---|---|---|
| `challenges_list.yml` | Challenges | 分类 Tab + 挑战卡片网格(VStack) |
| `challenge_detail.yml` | Challenges | 需求列表 + 奖励预览 + 进度条 + 领取按钮 |
| `warp_board.yml` | Warps | 传送点列表 + 搜索框 + 分类筛选 |
| `biome_selector.yml` | Biomes | 群系图标网格 + 价格 + 条件 + 预览 |
| `upgrades_menu.yml` | Upgrades | 升级项列表 + 当前等级/下一级/价格 |
| `visit_browser.yml` | Visit | 岛屿卡片网格 + 排序(等级/点赞/最近) + 搜索 |
| `limits_panel.yml` | Limits | 各类方块/生物计数 + 上限显示 + 警告 |

---

## 3. Packet 通信规范

### 3.1 服务端→客户端（init / update）

```yaml
# 岛屿主面板数据包
packet:
  islandId: "uuid-string"
  islandName: "~&b我的空岛"
  ownerName: "~&ePlayerName"
  memberCount: 3
  level: 15
  balance: 1250.5
  range: 100
  protectionRange: 50
  locked: false
  pvp: false
  fly: true
  
  # 成员列表分离（itemJson 分离模式）
  members:
    0:
      uuid: "..."
      name: "~&aMember1"
      role: "SUB_OWNER"
    1:
      uuid: "..."
      name: "~&7Member2"
      role: "MEMBER"
  
  # 头像 itemJson 分离
  memberItemJson0: '{"id":"minecraft:player_head",...}'
  memberItemJson1: '{"id":"minecraft:player_head",...}'
```

### 3.2 客户端→服务端

```java
// 格式：Packet.send('SKYDREAM_<ACTION>', 'param1', 'param2', ...)

// 示例操作
SKYDREAM_ISLAND_CREATE       // param: blueprintId
SKYDREAM_INVITE_ACCEPT       // param: islandId
SKYDREAM_INVITE_REJECT       // param: islandId
SKYDREAM_ISLAND_KICK         // param: targetUuid
SKYDREAM_ISLAND_DEPOSIT      // param: amount
SKYDREAM_ISLAND_WITHDRAW     // param: amount
SKYDREAM_ISLAND_UPGRADE      // param: upgradeId
SKYDREAM_CHALLENGE_CLAIM     // param: challengeId
SKYDREAM_BIOME_CHANGE        // param: biomeId
SKYDREAM_WARP_TELEPORT       // param: warpId
SKYDREAM_VISIT_TELEPORT      // param: islandId
SKYDREAM_SETTINGS_TOGGLE     // param: flagName, value
```

### 3.3 安全校验

所有客户端发起的经济/背包操作必须在主线程串行执行：

```java
if (Bukkit.isPrimaryThread()) {
    handlePacket(player, action, data);
} else {
    Bukkit.getScheduler().runTask(plugin, () -> handlePacket(player, action, data));
}
```

---

## 4. 命令系统

### 4.1 命令树

```
/island (aliases: /is, /skyblock, /sb)
  ├─ create [blueprint]         — 创建岛屿
  ├─ delete [confirm]           — 删除岛屿
  ├─ home [number]              — 传送到岛屿家
  ├─ sethome                     — 设置岛屿家
  ├─ invite <player>            — 邀请玩家
  ├─ accept                      — 接受邀请
  ├─ reject                      — 拒绝邀请
  ├─ kick <player>              — 踢出成员
  ├─ leave                       — 离开岛屿
  ├─ promote <player> [role]    — 提升成员
  ├─ demote <player> [role]     — 降级成员
  ├─ transfer <player>          — 转让岛主
  ├─ trust <player>             — 信任玩家
  ├─ untrust <player>           — 取消信任
  ├─ ban <player>               — 封禁玩家
  ├─ unban <player>             — 解封玩家
  ├─ coop <player> [duration]   — 临时协作
  ├─ lock                        — 锁定岛屿
  ├─ unlock                      — 解锁岛屿
  ├─ settings                    — 打开设置 UI
  ├─ name <newName>             — 修改岛名
  ├─ description <text>         — 修改描述
  ├─ top [category]             — 查看排行榜
  ├─ info [player]              — 查看岛屿信息
  ├─ reset [confirm]            — 重置岛屿
  ├─ warp [name]                — 传送到传送点
  ├─ setwarp [name]             — 设置传送点
  ├─ delwarp [name]             — 删除传送点
  ├─ level                       — 查看等级
  ├─ expel <player>             — 驱逐访客
  ├─ team                        — 打开成员管理 UI
  ├─ bank                        — 打开银行 UI
  ├─ challenges                  — 打开挑战 UI
  ├─ biome                       — 打开群系选择 UI
  ├─ upgrades                    — 打开升级 UI
  ├─ visit [player]             — 参观他人岛屿
  ├─ confirm                     — 确认操作
  └─ help [page]                — 帮助

/isadmin (aliases: /islandadmin, /ska)
  ├─ reload                      — 重载配置
  ├─ settings                    — 全局设置
  ├─ create <player> [blueprint] — 为玩家创建岛屿
  ├─ delete <player> [force]    — 删除玩家岛屿
  ├─ info <player>              — 查看岛屿详情
  ├─ setrange <player> <range>  — 设置保护范围
  ├─ setspawn                    — 设置出生点
  ├─ register <addon>           — 注册组件
  ├─ unregister <addon>         — 注销组件
  ├─ purge [days]               — 清理不活跃岛屿
  ├─ bp (blueprint)             — 蓝图管理
  │   ├─ list
  │   ├─ load <file>
  │   ├─ save <name>
  │   └─ paste <name> [player]
  ├─ world                       — 世界管理
  │   ├─ create <name> [gamemode]
  │   ├─ delete <name>
  │   └─ list
  └─ debug                       — 调试模式
```

### 4.2 命令实现基类

```java
public abstract class IslandSubCommand {
    protected final String name;
    protected final String description;
    protected final String permission;
    protected final boolean playerOnly;
    
    public abstract boolean execute(Player player, String[] args);
    public abstract List<String> tabComplete(Player player, String[] args);
    
    protected Island getPlayerIsland(Player player) {
        return getIslandManager().getPlayerIsland(player.getUniqueId());
    }
}
```

---

## 5. PAPI 占位符

### 5.1 占位符列表

| 占位符 | 说明 | 示例输出 |
|---|---|---|
| `%aubade_island_name%` | 玩家所属岛屿名称 | `我的空岛` |
| `%aubade_island_level%` | 岛屿等级 | `15` |
| `%aubade_island_members%` | 成员数 | `3` |
| `%aubade_island_owner%` | 岛主名 | `PlayerName` |
| `%aubade_island_range%` | 保护范围 | `50` |
| `%aubade_island_bank%` | 银行余额 | `1250.5` |
| `%aubade_island_likes%` | 点赞数 | `42` |
| `%aubade_island_rank%` | 岛屿排名 | `#5` |
| `%aubade_top_name_1%` | 排行榜第1名岛名 | `最强空岛` |
| `%aubade_top_owner_1%` | 排行榜第1名岛主 | `ProPlayer` |
| `%aubade_top_level_1%` | 排行榜第1名等级 | `999` |
| `%aubade_in_island%` | 是否在岛屿世界 | `是` |
| `%aubade_island_role%` | 玩家在岛屿的角色 | `岛主` |

### 5.2 占位符扩展实现

```java
public class AubadePlaceholderExpansion extends PlaceholderExpansion {
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) return "";
        Island island = islandManager.getPlayerIsland(player.getUniqueId());
        
        return switch (identifier) {
            case "island_name" -> island != null ? island.getName() : "无";
            case "island_level" -> island != null ? String.valueOf(island.getLevel()) : "0";
            case "island_members" -> island != null ? String.valueOf(island.getMembers().size()) : "0";
            case "island_owner" -> island != null ? Bukkit.getOfflinePlayer(island.getOwner()).getName() : "无";
            case "island_range" -> island != null ? String.valueOf(island.getProtectionRange()) : "0";
            case "island_bank" -> island != null ? String.valueOf(island.getBankBalance()) : "0";
            case "island_likes" -> island != null ? String.valueOf(island.getLikes()) : "0";
            case "island_role" -> getRoleDisplay(player, island);
            case "in_island" -> islandManager.isInIslandWorld(player) ? "是" : "否";
            default -> null;
        };
    }
}
```

---

## 状态与优先级

- **状态**：策划中
- **优先级**：P1（与核心功能同步开发）

---

## 文件间引用

- [← 01 项目定位与架构总览](aubade-01-overview-d32eb4.md)
- [← 03 核心数据模型与数据库](aubade-03-datamodel-d32eb4.md)
- [← 05 功能与扩展组件](aubade-05-features-d32eb4.md)
- [→ 07 配置、诊断与路线图](aubade-07-config-roadmap-d32eb4.md)
