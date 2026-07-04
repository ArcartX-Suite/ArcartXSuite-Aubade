# ArcartX-Aubade 开发蓝图 — 03 核心数据模型与数据库

**一句话总结**：Aubade 的持久层设计，包含 Island/SkyPlayer/WorldSettings 核心 POJO、权限角色体系、完整 SQL 建表语句与索引策略。

---

## 1. 核心数据对象

### 1.1 岛屿 Island

```java
package xuanmo.aubade.api.island;

public class Island implements DataObject {
    private final UUID uniqueId;           // 唯一标识
    private UUID owner;                    // 岛主
    private String name;                   // 岛屿名称（可自定义）
    private String description;            // 岛屿描述
    private Location center;               // 中心点
    private int protectionRange;           // 保护范围（半径）
    private int range;                     // 实际范围（含缓冲区）
    private World world;                   // 所属世界
    private GameModeAddon gameMode;        // 所属游戏模式
    
    // 成员管理
    private Map<UUID, IslandMember> members = new ConcurrentHashMap<>();
    private Set<UUID> bannedPlayers = ConcurrentHashMap.newKeySet();
    private Set<UUID> trustedPlayers = ConcurrentHashMap.newKeySet();
    private Map<UUID, CoopPlayer> coopPlayers = new ConcurrentHashMap<>();
    
    // 状态
    private boolean locked;                // 是否锁定（禁止访客）
    private boolean purgeProtected;        // 删除保护
    private long createdTime;
    private long lastLoginTime;
    
    // 缓存数据（由组件维护，不直接持久化到 island 主表）
    private volatile long level;         // 岛屿等级（由 level 组件写入）
    private volatile double bankBalance; // 银行余额（由 bank 组件写入）
    private volatile int likes;          // 点赞数（由 likes 组件写入）
    
    // 标志位与扩展
    private Map<String, Boolean> flags = new ConcurrentHashMap<>();
    private Map<String, String> meta = new ConcurrentHashMap<>();  // 组件扩展数据
    
    // 方法：权限检查
    public boolean hasPermission(UUID player, IslandPermission perm) {
        if (player.equals(owner)) return true;
        IslandMember member = members.get(player);
        if (member == null) return false;
        return member.getRole().hasPermission(perm);
    }
    
    // 方法：是否在保护范围内
    public boolean inProtectionRange(Location loc) {
        return center.getWorld().equals(loc.getWorld())
            && center.distanceSquared(loc) <= protectionRange * protectionRange;
    }
}
```

### 1.2 成员 IslandMember

```java
public class IslandMember {
    private final UUID playerUUID;
    private Role role;                     // OWNER, SUB_OWNER, MEMBER, VISITOR
    private long joinedTime;
    private int trustLevel;                // 信任等级（可配置）
    
    public boolean canManage() {
        return role == Role.OWNER || role == Role.SUB_OWNER;
    }
}

public enum Role {
    OWNER("岛主", 100, Set.of(IslandPermission.values())),
    SUB_OWNER("副岛主", 80, Set.of(BREAK, PLACE, INTERACT, USE_CONTAINER,
        USE_REDSTONE, SPAWN_MOBS, PVP, FLY, ANIMAL_SPAWN, MONSTER_SPAWN)),
    MEMBER("成员", 50, Set.of(BREAK, PLACE, INTERACT, USE_CONTAINER, FLY)),
    VISITOR("访客", 0, Set.of(INTERACT));
    
    private final String displayName;
    private final int priority;
    private final Set<IslandPermission> permissions;
    
    public boolean hasPermission(IslandPermission perm) {
        return permissions.contains(perm);
    }
}
```

### 1.3 权限枚举

```java
public enum IslandPermission {
    BREAK("破坏方块", true),
    PLACE("放置方块", true),
    INTERACT("交互", true),
    USE_CONTAINER("使用容器", true),
    USE_REDSTONE("使用红石", true),
    SPAWN_MOBS("刷怪", true),
    PVP("PvP", false),              // 默认关闭
    FLY("飞行", false),             // 默认关闭
    ANIMAL_SPAWN("动物生成", true),
    MONSTER_SPAWN("怪物生成", true),
    FIRE_SPREAD("火焰蔓延", false),
    TNT_DAMAGE("TNT 伤害", false),
    PISTON_PUSH("活塞推动", true),
    LEAF_DECAY("树叶腐烂", true),
    CROP_GROWTH("作物生长", true),
    WEATHER_CHANGE("天气变化", false);
    
    private final String displayName;
    private final boolean defaultValue;  // 默认是否允许
}
```

### 1.4 玩家 SkyPlayer

```java
package xuanmo.aubade.api.player;

public class SkyPlayer implements DataObject {
    private final UUID uuid;
    private UUID islandId;               // 所属岛屿
    private UUID lastIsland;             // 最后访问的岛屿
    private PlayerStatus status;         // ONLINE, OFFLINE, AFK
    
    // 统计
    private int deaths;
    private int resets;                  // 重置次数
    private long totalOnlineTime;
    private long lastLogin;
    private long lastLogout;
    
    // 邀请/请求
    private UUID pendingInvite;          // 待处理邀请（islandId）
    private Map<UUID, Long> coopRequests = new HashMap<>();  // 临时协作请求
    
    // 设置
    private boolean autoPickup;          // 自动拾取
    private Locale locale = Locale.SIMPLIFIED_CHINESE;
    
    // 组件扩展数据（JSON 存储，key=addonId, value=JSON）
    private Map<String, String> addonData = new HashMap<>();
    
    public <T> Optional<T> getAddonData(String addonId, Class<T> clazz) {
        String json = addonData.get(addonId);
        if (json == null) return Optional.empty();
        return Optional.of(GsonUtil.fromJson(json, clazz));
    }
    
    public void setAddonData(String addonId, Object data) {
        addonData.put(addonId, GsonUtil.toJson(data));
    }
}
```

### 1.5 世界配置 WorldSettings

```java
package xuanmo.aubade.api.world;

public interface WorldSettings {
    String getFriendlyName();            // 显示名称（如"经典空岛"）
    String getWorldName();               // 世界文件夹名
    boolean isNetherEnabled();
    boolean isEndEnabled();
    int getMaxIslandSize();              // 最大岛屿尺寸
    int getDefaultProtectionRange();     // 默认保护范围
    int getIslandSpacing();              // 岛屿间距
    int getSeaLevel();                   // 海平面
    
    // 游戏规则覆盖
    default boolean getDefaultGameRule(String rule) { return true; }
    
    // 生物群系配置
    default Biome getDefaultBiome() { return Biome.PLAINS; }
    
    // 组件默认启用
    default boolean isFeatureEnabled(String featureId) { return true; }
}
```

---

## 2. 数据库设计

### 2.1 完整建表语句

```sql
-- 岛屿主表
CREATE TABLE aubade_islands (
    id              VARCHAR(36) PRIMARY KEY,    -- UUID
    owner           VARCHAR(36) NOT NULL,
    name            VARCHAR(64),
    description     VARCHAR(256),
    world           VARCHAR(64) NOT NULL,
    center_x        INT NOT NULL,
    center_y        INT NOT NULL,
    center_z        INT NOT NULL,
    protection_range INT DEFAULT 50,
    range           INT DEFAULT 100,
    game_mode       VARCHAR(32) NOT NULL,
    locked          BOOLEAN DEFAULT FALSE,
    purge_protected BOOLEAN DEFAULT FALSE,
    flags           TEXT,                       -- JSON: {"pvp":false,"fly":true,...}
    meta            TEXT,                       -- JSON: 组件扩展
    created_at      BIGINT,
    last_login      BIGINT
);

-- 成员关系表
CREATE TABLE aubade_members (
    island_id       VARCHAR(36) NOT NULL,
    player_uuid     VARCHAR(36) NOT NULL,
    role            VARCHAR(16) DEFAULT 'MEMBER',
    trust_level     INT DEFAULT 0,
    joined_at       BIGINT,
    PRIMARY KEY (island_id, player_uuid),
    FOREIGN KEY (island_id) REFERENCES aubade_islands(id) ON DELETE CASCADE
);

-- 封禁表
CREATE TABLE aubade_bans (
    island_id       VARCHAR(36) NOT NULL,
    player_uuid     VARCHAR(36) NOT NULL,
    banned_by       VARCHAR(36),
    reason          VARCHAR(256),
    banned_at       BIGINT,
    PRIMARY KEY (island_id, player_uuid),
    FOREIGN KEY (island_id) REFERENCES aubade_islands(id) ON DELETE CASCADE
);

-- 信任表（非成员但可交互）
CREATE TABLE aubade_trusts (
    island_id       VARCHAR(36) NOT NULL,
    player_uuid     VARCHAR(36) NOT NULL,
    trusted_at      BIGINT,
    PRIMARY KEY (island_id, player_uuid),
    FOREIGN KEY (island_id) REFERENCES aubade_islands(id) ON DELETE CASCADE
);

-- 临时协作者表
CREATE TABLE aubade_coops (
    island_id       VARCHAR(36) NOT NULL,
    player_uuid     VARCHAR(36) NOT NULL,
    invited_by      VARCHAR(36) NOT NULL,
    expires_at      BIGINT,
    PRIMARY KEY (island_id, player_uuid),
    FOREIGN KEY (island_id) REFERENCES aubade_islands(id) ON DELETE CASCADE
);

-- 玩家数据表
CREATE TABLE aubade_players (
    uuid            VARCHAR(36) PRIMARY KEY,
    island_id       VARCHAR(36),
    last_island     VARCHAR(36),
    deaths          INT DEFAULT 0,
    resets          INT DEFAULT 0,
    total_online    BIGINT DEFAULT 0,
    last_login      BIGINT,
    last_logout     BIGINT,
    auto_pickup     BOOLEAN DEFAULT FALSE,
    locale          VARCHAR(16) DEFAULT 'zh_cn',
    addon_data      TEXT,                       -- JSON
    FOREIGN KEY (island_id) REFERENCES aubade_islands(id) ON DELETE SET NULL
);

-- 世界配置表
CREATE TABLE aubade_worlds (
    name            VARCHAR(64) PRIMARY KEY,
    friendly_name   VARCHAR(64),
    game_mode       VARCHAR(32),
    nether_enabled  BOOLEAN DEFAULT TRUE,
    end_enabled     BOOLEAN DEFAULT TRUE,
    max_size        INT DEFAULT 200,
    default_range   INT DEFAULT 50,
    spacing         INT DEFAULT 200,
    sea_level       INT DEFAULT 0,
    settings        TEXT                        -- JSON
);

-- 蓝图注册表
CREATE TABLE aubade_blueprints (
    id              VARCHAR(32) PRIMARY KEY,
    game_mode       VARCHAR(32),
    name            VARCHAR(64),
    description     VARCHAR(256),
    icon            VARCHAR(256),
    file_path       VARCHAR(256),
    default_bp      BOOLEAN DEFAULT FALSE,
    requirements    TEXT                        -- JSON
);

-- 组件元数据表（存储各组件自定义数据）
CREATE TABLE aubade_addon_data (
    addon_id        VARCHAR(32) NOT NULL,
    island_id       VARCHAR(36),
    player_uuid     VARCHAR(36),
    key_name        VARCHAR(64) NOT NULL,
    value           TEXT,
    PRIMARY KEY (addon_id, island_id, player_uuid, key_name)
);

-- 等级历史（由 level 组件维护）
CREATE TABLE aubade_level_history (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    island_id       VARCHAR(36) NOT NULL,
    old_level       BIGINT,
    new_level       BIGINT,
    reason          VARCHAR(64),
    recorded_at     BIGINT
);

-- 排行榜缓存
CREATE TABLE aubade_top_cache (
    category        VARCHAR(32) NOT NULL,
    rank_num        INT NOT NULL,
    island_id       VARCHAR(36) NOT NULL,
    score           DOUBLE NOT NULL,
    updated_at      BIGINT,
    PRIMARY KEY (category, rank_num)
);
```

### 2.2 索引设计

```sql
-- 岛屿查询索引
CREATE INDEX idx_islands_owner ON aubade_islands(owner);
CREATE INDEX idx_islands_world ON aubade_islands(world);
CREATE INDEX idx_islands_gamemode ON aubade_islands(game_mode);

-- 成员查询索引
CREATE INDEX idx_members_player ON aubade_members(player_uuid);
CREATE INDEX idx_members_role ON aubade_members(island_id, role);

-- 玩家查询索引
CREATE INDEX idx_players_island ON aubade_players(island_id);
CREATE INDEX idx_players_lastlogin ON aubade_players(last_login);

-- 等级历史索引
CREATE INDEX idx_level_history_island ON aubade_level_history(island_id);
CREATE INDEX idx_level_history_time ON aubade_level_history(recorded_at);

-- 排行榜索引
CREATE INDEX idx_topcache_category ON aubade_top_cache(category, updated_at);

-- 组件数据索引
CREATE INDEX idx_addon_data_island ON aubade_addon_data(addon_id, island_id);
CREATE INDEX idx_addon_data_player ON aubade_addon_data(addon_id, player_uuid);
```

### 2.3 MySQL 适配差异

| 差异项 | SQLite | MySQL |
|---|---|---|
| 主键自增 | `INTEGER PRIMARY KEY AUTOINCREMENT` | `INT AUTO_INCREMENT PRIMARY KEY` |
| UUID 存储 | `VARCHAR(36)` | `VARCHAR(36)` 或 `BINARY(16)` |
| JSON 字段 | `TEXT` | `JSON`（MySQL 5.7+） |
| 布尔值 | `BOOLEAN`（存 0/1） | `BOOLEAN` / `TINYINT(1)` |
| 外键 | 默认启用 | 默认启用 |
| 大小写敏感 | `LIKE` 不敏感 | 依赖 collation |

---

## 3. 缓存策略

### 3.1 三级缓存

```
L1: 运行时对象缓存（ConcurrentHashMap）
  └─ IslandCache: UUID → Island 对象（热数据常驻内存）
  └─ PlayerCache: UUID → SkyPlayer 对象（在线玩家常驻）
  
L2: 查询结果缓存（Caffeine，TTL 5分钟）
  └─ 岛屿列表按世界缓存
  └─ 玩家岛屿关系缓存
  └─ 排行榜缓存
  
L3: 数据库持久层（HikariCP 连接池）
  └─ 写操作异步批量提交
  └─ 读操作优先走缓存
```

### 3.2 缓存一致性

| 操作 | 缓存更新策略 |
|---|---|
| 岛屿创建 | 写入 DB → 加入 L1 缓存 → 失效 L2 世界列表缓存 |
| 成员加入 | 更新 DB → 更新 L1 Island.members → 失效 L2 玩家关系缓存 |
| 等级变更 | 更新 DB → 更新 L1 island.level → 异步更新 L2 排行榜 |
| 玩家下线 | L1 PlayerCache 保留 10 分钟后移除 |
| 跨服同步 | Redis pub/sub 推送变更事件，各节点失效对应缓存 |

---

## 4. 数据库迁移管理

### 4.1 版本控制

```java
public class MigrationManager {
    private static final int CURRENT_VERSION = 1;
    
    public void migrate(DataSource ds) {
        int current = getCurrentVersion(ds);
        while (current < CURRENT_VERSION) {
            current++;
            runMigration(ds, current);
            setVersion(ds, current);
        }
    }
    
    private void runMigration(DataSource ds, int version) {
        switch (version) {
            case 1 -> runV1InitialSchema(ds);
            // case 2 -> runV2AddNewColumn(ds);
        }
    }
}
```

### 4.2 迁移文件结构

```
src/main/resources/migrations/
├── V1__initial_schema.sql
├── V2__add_level_history.sql
├── V3__add_top_cache.sql
└── ...
```

---

## 状态与优先级

- **状态**：策划中
- **优先级**：P0（所有功能的基础）

---

## 文件间引用

- [← 01 项目定位与架构总览](aubade-01-overview-d32eb4.md)
- [← 02 ArcartX/AXS 深度联动](aubade-02-arcartx-integration-d32eb4.md)
- [→ 04 游戏模式设计](aubade-04-gamemodes-d32eb4.md)
