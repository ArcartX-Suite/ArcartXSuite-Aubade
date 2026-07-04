# ArcartX-Aubade 开发蓝图 — 05 功能与扩展组件

**一句话总结**：8 个核心功能组件 + 18 个扩展组件的设计，包含生命周期接口、addon.yml 规范、以及 Level/Challenges/Teams 三个重点组件的详细实现。

---

## 1. 组件生命周期

```java
public interface SkyAddon {
    AddonDescriptor descriptor();
    void onLoad();
    void onEnable();
    void onDisable();
    void onReload();
    boolean isEnabled();
}

// addon.yml 示例
// id: level
// name: 岛屿等级
// version: 1.0.0
// main: xuanmo.aubade.features.level.LevelAddon
// api-version: 1.0
// depends: []
// softdepends: [teams]
// plugin-depends: []
```

---

## 2. 功能组件（FeatureAddon）

### 2.1 Level（岛屿等级）

**核心机制**：扫描岛屿范围内的方块，按价值表计算总等级。

```java
public class LevelAddon extends AbstractFeatureAddon {
    private BlockValues blockValues;       // 方块价值表
    private LevelCalculator calculator;    // 扫描算法
    private LevelTopCache topCache;        // 排行榜缓存
    
    @Override
    public void onEnable() {
        blockValues = BlockValues.load(config.getConfigurationSection("block-values"));
        calculator = new LevelCalculator(blockValues, getIslandManager());
        topCache = new LevelTopCache(getStorageManager());
        
        // 注册命令 /island level
        getCommandManager().registerSubCommand("level", new LevelCommand(this));
        
        // 注册 UI
        registerUi("level_display.yml", "island_level_display");
        registerUi("level_top.yml", "island_level_top");
    }
    
    public long calculateLevel(Island island) {
        return calculator.scan(island);
    }
    
    public void updateLevel(Island island) {
        long oldLevel = island.getLevel();
        long newLevel = calculateLevel(island);
        if (newLevel != oldLevel) {
            island.setLevel(newLevel);
            getEventBus().post(new IslandLevelChangeEvent(island, oldLevel, newLevel));
            saveToDatabase(island, newLevel);
        }
    }
}
```

**方块价值表示例**：
```yaml
block-values:
  minecraft:stone: 1
  minecraft:dirt: 1
  minecraft:oak_log: 2
  minecraft:diamond_block: 900
  minecraft:beacon: 1000
  # 支持通配：minecraft:*_ore: 5
```

**扫描算法**：
- 范围：岛屿 `protectionRange` 内
- 方式：异步 BFS/DFS，按 Y 层分层扫描
- 优化：缓存已扫描区块，增量更新
- 限制：上限方块数防止恶意堆叠

**UI**：
- `level_display.yml`：HUD 进度条 + 等级数字（`isHud: true`）
- `level_top.yml`：VStack 排行榜，高亮自己的排名

---

### 2.2 Challenges（挑战系统）

**挑战类型**：

| 类型 | 描述 | 示例 |
|---|---|---|
| COLLECT | 收集指定物品 | 收集 64 个圆石 |
| PLACE | 放置指定方块 | 放置 16 个火把 |
| KILL | 击杀指定实体 | 击杀 10 只僵尸 |
| EXPLORE | 探索指定位置 | 到达下界 |
| ECONOMY | 经济操作 | 岛屿银行存款达到 1000 |
| ISLAND | 岛屿属性 | 岛屿等级达到 10 |
| CRAFT | 合成物品 | 合成 1 个工作台 |

**数据结构**：
```java
public class Challenge {
    private String id;
    private String name;
    private ChallengeType type;
    private Map<String, Integer> requirements;  // item/entity/amount
    private List<Reward> rewards;                 // 经验/物品/经济/权限
    private boolean repeatable;                   // 是否可重复
    private int maxRepeats;                       // 最大重复次数
    private List<String> requiredChallenges;      // 前置挑战
    private String icon;                          // UI 图标
}
```

**进度跟踪**：
- 存储在 `aubade_addon_data` 表，`addon_id="challenges"`
- JSON 格式：`{"completed":["c1","c2"],"progress":{"c3":{"current":32,"target":64}}}`

**UI**：
- `challenges_list.yml`：分类 Tab + 挑战卡片网格
- `challenge_detail.yml`：需求列表 + 奖励预览 + 进度条 + 领取按钮

---

### 2.3 Teams（团队管理）

**权限矩阵**：

| 操作 | OWNER | SUB_OWNER | MEMBER | VISITOR |
|---|---|---|---|---|
| 邀请玩家 | ✅ | ✅ | ❌ | ❌ |
| 踢出成员 | ✅ | ✅（仅限 MEMBER） | ❌ | ❌ |
| 设置权限 | ✅ | ❌ | ❌ | ❌ |
| 转让岛主 | ✅ | ❌ | ❌ | ❌ |
| 删除岛屿 | ✅ | ❌ | ❌ | ❌ |
| 修改名称 | ✅ | ✅ | ❌ | ❌ |
| 银行操作 | ✅ | ✅ | ❌ | ❌ |

**邀请流程**：
1. 岛主 `/island invite <玩家>`
2. 被邀请玩家收到 UI 弹窗 `aubade_invite.yml`
3. 被邀请玩家点击接受/拒绝
4. 接受后更新 `aubade_members` 表

**UI**：
- `member_manage.yml`：成员列表(VStack) + 权限编辑弹窗 + 邀请输入框
- `team_settings.yml`：角色权限矩阵 + 副岛主设置 + 转让岛主确认

---

### 2.4 其他功能组件概要

| 组件 | 核心功能 | 数据存储 | UI 文件 |
|---|---|---|---|
| **Warps** | 传送牌创建/删除/公共传送 | `aubade_addon_data` (addon_id="warps") | `warp_board.yml` |
| **Bank** | 岛屿共享银行，存取/利率/交易税 | `aubade_islands.bank_balance` | `island_bank.yml` |
| **Biomes** | 岛屿生物群系更换，消耗/条件 | `aubade_addon_data` | `biome_selector.yml` |
| **Border** | 世界边界/粒子边界显示 | 无独立数据，使用核心岛屿范围 | 无独立 UI，集成到设置面板 |

---

## 3. 扩展组件（ExtensionAddon）

扩展组件比功能组件更轻量，通常不独立提供 UI，而是增强或修改现有机制。

| 组件 | 功能 |  hook 点 |
|---|---|---|
| **InvSwitcher** | 世界间背包分离 | `PlayerChangedWorldEvent` |
| **IslandFly** | 岛屿内飞行权限控制 | `PlayerMoveEvent` + 权限检查 |
| **Limits** | 方块/实体数量限制 | `BlockPlaceEvent` + `EntitySpawnEvent` |
| **Likes** | 岛屿点赞/投票，反刷机制 | 数据库计数 + 冷却时间 |
| **Upgrades** | 岛屿属性升级（范围/成员上限/刷怪率） | 消耗资源升级 + 数据库记录 |
| **Visit** | 参观其他岛屿，无需传送牌 | 安全传送检查 |
| **VoidPortals** | 掉入虚空传送到下界/末地 | `PlayerMoveEvent` (Y<0) |
| **Greenhouses** | 玻璃结构内模拟指定生物群系 | 定时扫描玻璃结构 |
| **MagicGen** | 配置化刷石机产出任意方块 | `BlockFormEvent` 拦截 |
| **DimensionalTrees** | 下界/末地种树产出特殊材料 | `StructureGrowEvent` |
| **ExtraMobs** | 调整岛屿刷怪规则 | `CreatureSpawnEvent` |
| **FarmersDance** | 潜行加速作物生长 | `PlayerToggleSneakEvent` |
| **TwerkingForTrees** | 潜行催促树苗立即生长 | `PlayerToggleSneakEvent` |
| **CauldronWitchery** | 坩埚配方召唤生物/获得效果 | `PlayerInteractEvent` (cauldron) |
| **ControlPanel** | 管理员全局控制面板 | 集成到 `aubade_admin.yml` |
| **CheckMeOut** | 玩家提交岛屿供审核投票 | 审核队列 + 投票计数 |
| **TopBlock** | 按岛屿最高放置方块排名 | 定时扫描 Y 最大值 |
| **Chat** | 岛屿/团队聊天频道 | `AsyncPlayerChatEvent` |

---

## 状态与优先级

- **状态**：策划中
- **优先级**：P1（功能组件在核心稳定后开发）

---

## 文件间引用

- [← 01 项目定位与架构总览](aubade-01-overview-d32eb4.md)
- [← 03 核心数据模型与数据库](aubade-03-datamodel-d32eb4.md)
- [→ 06 UI 与命令系统](aubade-06-ui-commands-d32eb4.md)
