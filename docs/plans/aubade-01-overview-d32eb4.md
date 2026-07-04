# ArcartX-Aubade 开发蓝图 — 01 项目定位与架构总览

**一句话总结**：以 BentoBox 的「核心+组件」架构为骨架，以 AXS 的模块化工程、配置诊断、UI 桥接为工程范本，深度融合 ArcartX 客户端全部能力，构建一套支持多游戏模式、20+功能组件、全中文、宽版本兼容（1.20.x–1.21.x）、开源免费的空岛生态插件。

---

## 1. 项目定位与差异化

| 维度 | BentoBox | ASkyBlock | SuperiorSkyBlock | ArcartX-Aubade |
|---|---|---|---|---|
| 版本兼容 | 硬绑定最新版 | 停更 | 付费闭源 | **宽版本 1.20.x–1.21.x** |
| 管理面板 | 命令/书本 | 命令 | GUI（闭源） | **ArcartX UI 全客户端面板** |
| 组件更新 | Addon 需同步更新 | 无 | 无 | **核心稳定 API，组件独立生命周期** |
| 多游戏模式 | 支持 | 仅 SkyBlock | 仅 SkyBlock | **核心框架支持任意 GameMode** |
| 中文生态 | 英文为主 | 社区汉化 | 社区汉化 | **源码/文档/配置/消息全中文** |
| 开源 | GPL | 停更 | 付费 | **GPL-3.0 完全开源** |
| 外部联动 | 基础 | 无 | 有限 | **深度联动 AXS 全模块 + ArcartX 客户端** |

**核心哲学**：
- **核心稳定，组件自由**：核心 API 长期保持二进制兼容，组件可独立更新
- **UI 即一切**：所有管理、配置、交互优先走 ArcartX 客户端 UI；命令作为降级和自动化接口保留
- **中文优先**：源码注释、配置注释、游戏内消息、Wiki、Issue 模板全部中文
- **宽版本适配**：拒绝版本绑架，一个 jar 覆盖 1.20.1 到 1.21.x
- **生态开放**：不仅做 SkyBlock，更要成为「岛屿类游戏」的通用平台

---

## 2. 整体架构

### 2.1 架构分层

```
┌─────────────────────────────────────────────┐
│  ArcartX 客户端层                           │
│  UI面板 | 路标 | 飘字 | 贴图 | 按键 | 聊天卡片│
└──────────────┬──────────────────────────────┘
               ↑↓
┌─────────────────────────────────────────────┐
│  aubade-core（核心插件，唯一含 plugin.yml）│
│  ├─ 岛屿管理：IslandManager/IslandGrid/Factory│
│  ├─ 玩家数据：PlayerManager/Cache            │
│  ├─ 世界生成：WorldManager/ChunkGenerator     │
│  ├─ 组件加载：AddonLifecycleManager/Loader  │
│  ├─ 命令路由：CommandManager/CompositeCommand │
│  ├─ 配置诊断：ConfigDiagnosticEngine(移植AXS)│
│  ├─ UI 桥接：UiManager/ArcartXUiBridge(反射) │
│  ├─ 跨服通信：CrossServerManager/Redis       │
│  └─ 事件总线：EventBus                       │
└──────────────┬──────────────────────────────┘
               ↑↓
┌─────────────────────────────────────────────┐
│  aubade-api（公开 API，Maven 发布）         │
│  Addon | Island | Player | World | Storage   │
│  Event | Permission | UI Bridge | Command    │
└──────────────┬──────────────────────────────┘
               ↑↓
  游戏模式组件：SkyBlock | AcidIsland | SkyGrid  
              | CaveBlock | OneBlock | Boxed      
  功能组件：Level | Challenges | Warps | Teams    
          | Bank | Biomes | Border | ...           
  扩展组件：InvSwitcher | IslandFly | Limits      
          | Likes | Upgrades | Visit | ...       
```

### 2.2 模块职责

| 模块 | 职责 | 构建产物 |
|---|---|---|
| `aubade-api` | 公开 API，供组件编译依赖，禁止包含 Bukkit 实现 | 发布到 Maven |
| `aubade-core` | 唯一含 `plugin.yml`，负责生命周期/世界/组件加载/UI 桥接 | `Aubade.jar` |
| `aubade-game-*` | 继承 `GameModeAddon`，注册世界、命令、规则 | `Aubade-Game-*.jar` |
| `aubade-feat-*` | 继承 `FeatureAddon`，提供具体功能 | `Aubade-Feat-*.jar` |
| `aubade-ext-*` | 可选增强扩展 | `Aubade-Ext-*.jar` |

---

## 3. 工程结构

### 3.1 根项目树

```
ArcartX-Aubade/
├── gradle.properties
├── settings.gradle.kts
├── buildSrc/                          # 自定义 Gradle 任务
│
├── aubade-api/                      # 公开 API 模块
│   └── src/main/java/xuanmo/aubade/api/
│       ├── addon/SkyAddon.java, GameModeAddon.java, FeatureAddon.java
│       ├── addon/AddonDescriptor.java, AddonClassLoader.java
│       ├── island/Island.java, IslandManager.java, IslandPermission.java, Role.java
│       ├── player/SkyPlayer.java, PlayerManager.java
│       ├── world/WorldSettings.java, GameWorld.java, ChunkGeneratorFactory.java
│       ├── storage/Repository.java, StorageDescriptor.java, DataObject.java
│       ├── ui/UiBridge.java, PacketPayloadBuilder.java
│       ├── event/IslandEvent, IslandCreateEvent, IslandDeleteEvent, IslandJoinEvent, ...
│       ├── command/CompositeCommand.java
│       ├── permission/Permission.java
│       └── config/ConfigComment.java, ConfigEntry.java
│
├── aubade-core/                     # 核心插件（唯一 plugin.yml）
│   ├── src/main/java/xuanmo/aubade/core/
│   │   ├── AubadePlugin.java
│   │   ├── lifecycle/CoreLifecycleManager.java, AddonLifecycleManager.java
│   │   ├── island/IslandManagerImpl.java, IslandGrid.java, IslandFactory.java
│   │   ├── island/IslandProtectionManager.java, IslandBorderManager.java, IslandCache.java
│   │   ├── player/PlayerManagerImpl.java, PlayerCache.java, PlayerJoinListener.java
│   │   ├── world/WorldManagerImpl.java, WorldFactory.java
│   │   ├── world/SkyBlockChunkGenerator.java, AcidIslandChunkGenerator.java
│   │   ├── world/SkyGridChunkGenerator.java, CaveBlockChunkGenerator.java
│   │   ├── command/CommandManager.java, CompositeCommandImpl.java
│   │   ├── command/DefaultPlayerCommand.java, DefaultAdminCommand.java
│   │   ├── command/IslandCreateCommand.java, IslandDeleteCommand.java, IslandHomeCommand.java
│   │   ├── command/IslandSetHomeCommand.java, IslandInviteCommand.java, IslandKickCommand.java
│   │   ├── command/IslandLeaveCommand.java, IslandTrustCommand.java, IslandUntrustCommand.java
│   │   ├── command/IslandPromoteCommand.java, IslandDemoteCommand.java, IslandTransferCommand.java
│   │   ├── command/IslandCoopCommand.java, IslandBanCommand.java, IslandUnbanCommand.java
│   │   ├── command/IslandLockCommand.java, IslandUnlockCommand.java, IslandSettingsCommand.java
│   │   ├── command/IslandTopCommand.java, IslandInfoCommand.java, IslandNameCommand.java
│   │   ├── command/IslandDescriptionCommand.java, IslandResetCommand.java
│   │   ├── blueprint/Blueprint.java, BlueprintManager.java, BlueprintParser.java
│   │   ├── blueprint/BlueprintPaster.java, BlueprintSerializer.java
│   │   ├── storage/StorageManager.java, JdbcIslandRepository.java
│   │   ├── storage/JdbcPlayerRepository.java, JdbcWorldRepository.java, MigrationManager.java
│   │   ├── config/CoreConfig.java, ConfigDiagnosticEngine.java, SyncPolicy.java
│   │   ├── config/ValidationRule.java, ValueType.java, ConfigMigrator.java, ConfigVersion.java
│   │   ├── ui/UiManager.java, UiRegistry.java
│   │   ├── ui/bridge/ArcartXUiBridge.java
│   │   ├── ui/packet/IslandUiPacketHandler.java, AdminUiPacketHandler.java
│   │   ├── addon/AddonRegistry.java, AddonLoader.java, AddonDescriptorParser.java
│   │   ├── addon/AddonFolderWatcher.java
│   │   ├── crossserver/CrossServerManager.java, RedisConnector.java, IslandSyncPacket.java
│   │   ├── placeholder/SkyDreamPlaceholderExpansion.java
│   │   ├── util/VersionAdapter.java, ReflectionCache.java
│   │   ├── util/LocationUtil.java, MathUtil.java, StringUtil.java
│   │   └── listener/PlayerJoinListener.java, PlayerQuitListener.java
│   │       listener/BlockBreakListener.java, BlockPlaceListener.java
│   │       listener/EntityDamageListener.java, PlayerMoveListener.java
│   │       listener/PlayerInteractListener.java, EntitySpawnListener.java
│   │       listener/InventoryMoveListener.java, TeleportListener.java
│   └── src/main/resources/
│       ├── plugin.yml
│       ├── config.yml
│       ├── messages.yml
│       └── arcartx/ui/
│           ├── aubade_main.yml
│           ├── aubade_admin.yml
│           └── aubade_top.yml
│
├── aubade-game-skyblock/            # 经典空岛
├── aubade-game-acidisland/          # 酸海岛
├── aubade-game-skygrid/             # 网格世界
├── aubade-game-caveblock/           # 洞穴方块
├── aubade-game-oneblock/            # 单方块
├── aubade-game-boxed/               # 盒子世界
│
├── aubade-feat-level/               # 岛屿等级
├── aubade-feat-challenges/          # 挑战系统
├── aubade-feat-warps/               # 岛屿传送
├── aubade-feat-teams/               # 团队管理
├── aubade-feat-bank/                # 岛屿银行
├── aubade-feat-biomes/              # 生物群系
├── aubade-feat-border/              # 岛屿边界
│
├── aubade-ext-invswitcher/          # 背包分离
├── aubade-ext-islandfly/            # 岛屿飞行
├── aubade-ext-likes/                # 岛屿点赞
├── aubade-ext-limits/               # 岛屿限制
├── aubade-ext-upgrades/             # 岛屿升级
├── aubade-ext-visit/                # 岛屿参观
├── aubade-ext-voidportals/          # 虚空传送门
├── aubade-ext-greenhouses/          # 温室
├── aubade-ext-magicgen/             # 魔法刷石机
├── aubade-ext-dimensionaltrees/     # 维度树木
├── aubade-extramobs/                # 额外刷怪
├── aubade-ext-farmersdance/         # 农夫之舞
├── aubade-ext-twerkingfortrees/     # 催生树木
├── aubade-ext-cauldronwitchery/     # 坩埚巫术
├── aubade-ext-controlpanel/         # 控制面板
├── aubade-ext-checkmeout/           # 岛屿审核
├── aubade-ext-topblock/             # 最高方块榜
├── aubade-ext-chat/                 # 岛屿聊天频道
│
└── docs/                              # 中文文档
    ├── guide/quickstart.md, install.md, config.md, faq.md
    ├── dev/setup.md, addon-api.md, gamemode.md, ui-cookbook.md, database.md
    ├── architecture/core.md, addon-system.md, storage.md
    └── appendix/changelog.md
```

### 3.2 构建配置要点

```kotlin
// aubade-core/build.gradle.kts
plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.5"
}
dependencies {
    implementation(project(":aubade-api"))
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("priv.seventeen.artist:ArcartX:2.0.0")
    compileOnly("xuanmo.arcartxsuite:axs-api:4.0.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("redis.clients:jedis:5.2.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("com.google.code.gson:gson:2.11.0")
}

// 组件 build.gradle.kts
dependencies {
    compileOnly(project(":aubade-api"))
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
}
```

---

## 4. 组件分类清单

### 4.1 游戏模式组件（GameModeAddon）

| 组件 | 核心玩法 | 世界特点 |
|---|---|---|
| **SkyBlock** | 经典空岛生存 | 虚空世界，基岩出生平台 |
| **AcidIsland** | 酸海生存 | 酸雨/酸海伤害，木船防水 |
| **SkyGrid** | 网格世界 | 随机方块网格散布虚空 |
| **CaveBlock** | 洞穴方块 | 实心石头+洞穴系统 |
| **OneBlock** | 单方块 | 单个再生方块，阶段化产出 |
| **Boxed** | 盒子世界 | 成就解锁边界扩展 |

### 4.2 功能组件（FeatureAddon）

| 组件 | 功能 |
|---|---|
| **Level** | 方块扫描计算等级，排行榜 |
| **Challenges** | 收集/放置/击杀/探索/经济型挑战 |
| **Warps** | 传送牌/公共传送/欢迎设置 |
| **Teams** | 成员邀请/踢出/转让/权限/副岛主 |
| **Bank** | 岛屿共享银行/存取/利率 |
| **Biomes** | 岛屿生物群系更换 |
| **Border** | 世界边界/粒子边界/权限边界 |

### 4.3 扩展组件（ExtensionAddon）

| 组件 | 功能 |
|---|---|
| InvSwitcher | 世界间背包分离 |
| IslandFly | 岛屿内飞行权限控制 |
| Limits | 方块/实体数量限制 |
| Likes | 岛屿点赞/投票 |
| Upgrades | 岛屿属性升级（范围/成员上限等） |
| Visit | 参观其他岛屿 |
| VoidPortals | 虚空传送门 |
| Greenhouses | 温室系统 |
| MagicGen | 魔法刷石机 |
| DimensionalTrees | 维度树木 |
| ExtraMobs | 额外刷怪规则 |
| FarmersDance | 潜行加速作物生长 |
| TwerkingForTrees | 潜行催促树苗立即生长 |
| CauldronWitchery | 坩埚配方巫术 |
| ControlPanel | 管理员控制面板 |
| CheckMeOut | 岛屿审核投票 |
| TopBlock | 最高方块排名 |
| Chat | 岛屿聊天频道 |

---

## 5. 关键设计决策

| 决策项 | 选择 | 理由 |
|---|---|---|
| 是否 AXS 子模块 | **否，独立插件** | 空岛插件需要面向更广泛的 Paper 服务器，不强制依赖 AXS |
| 是否 ArcartX 强制依赖 | **软依赖** | 无 ArcartX 时回退到书本/命令交互 |
| 组件隔离方式 | **自定义类加载器** | 参考 BentoBox AddonClassLoader，但支持运行时热插拔 |
| 数据库 | **SQLite 默认 + MySQL 可选** | 覆盖单机到集群场景 |
| 跨服同步 | **Redis 可选** | 多子服场景通过 Redis 同步岛屿数据 |
| 版本适配 | **1.20.1 Paper API + 反射** | compileOnly 固定 1.20.1，运行时反射适配高版本方块/实体 |
| 蓝图格式 | **JSON（内部）+ 配置化导出** | JSON 便于程序解析，同时支持管理员工具导出 |
| UI 数据模式 | **itemJson 分离 + 字典 key 整数字符串** | 参考 AXS market/warehouse 的最佳实践，避免 packet 体积膨胀 |

---

## 状态与优先级

- **状态**：策划中
- **优先级**：P0（基础文件，后续文件均依赖本节架构决策）

---

## 文件间引用

- [→ 02 ArcartX/AXS 深度联动](aubade-02-arcartx-integration-d32eb4.md)
- [→ 03 核心数据模型与数据库](aubade-03-datamodel-d32eb4.md)
- [→ 04 游戏模式设计](aubade-04-gamemodes-d32eb4.md)
- [→ 05 功能与扩展组件](aubade-05-features-d32eb4.md)
- [→ 06 UI 与命令系统](aubade-06-ui-commands-d32eb4.md)
- [→ 07 配置、诊断与路线图](aubade-07-config-roadmap-d32eb4.md)
