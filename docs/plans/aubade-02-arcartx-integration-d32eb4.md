# ArcartX-Aubade 开发蓝图 — 02 ArcartX/AXS 深度联动

**一句话总结**：Aubade 与 ArcartX 客户端及 AXS 全模块的深度集成方案，覆盖客户端特效、UI 通信、经济、仓库、邮件、通行证等全部可联动能力。

---

## 1. ArcartX 客户端能力调用矩阵

| ArcartX 能力 | Aubade 应用场景 | 调用接口 |
|---|---|---|
| **UI 面板** | 岛屿主菜单、成员管理、设置面板、排行榜、挑战列表、银行界面、商店界面 | `PacketBridgeAPI.registerOrReloadUi()` / `openUi()` / `sendPacket()` |
| **HUD 覆盖** | 岛屿边界提示、等级显示、Boss 血条、目标锁定 | UI YAML `isHud: true` |
| **路标 Waypoint** | 岛屿导航标记、任务目标点、传送点标记 | `WaypointBridgeAPI.addWaypoint()` |
| **伤害飘字** | 岛屿 PvP/怪物战斗伤害数字 | `ClientBridgeAPI.sendDamageDisplay()` |
| **服务端变量** | 实时推送岛屿等级、在线成员数、保护范围到客户端 | `ClientBridgeAPI.sendServerVariable()` |
| **文字贴图** | 岛屿中心上方显示岛名、欢迎语 | `WorldTextureBridgeAPI.spawnAtLocation()` |
| **NPC 导航标记** | 任务 NPC 私有标记、岛屿向导 | `AdyeshachNpcBridgeAPI.spawnPrivateMarker()` |
| **客户端按键** | 快速打开岛屿菜单、一键回家 | `PropBridgeAPI.registerClientKeyBind()` |
| **聊天卡片** | 岛屿信息分享、交易广播 | `PacketBridgeAPI.sendChatCard()` |

---

## 2. AXS 模块联动矩阵

| AXS 模块 | SkyDream 联动场景 | 接口/能力 |
|---|---|---|
| **market** | 岛屿专属商店、玩家间交易、拍卖行 | `IslandShopCapable` 能力注册 |
| **warehouse** | 岛屿共享仓库、银行货币 | `IslandWarehouseCapable` 能力注册 |
| **mail** | 岛屿邀请邮件、系统通知、离线奖励 | `MailDispatchable` 发送岛屿邮件 |
| **onlinerewards** | 岛屿在线奖励、签到 | 按岛屿活跃度计算奖励倍数 |
| **battlepass** | 岛屿赛季任务（升级X级/完成X挑战） | 事件触发 battlepass 条件评估 |
| **title** | 岛屿称号（岛主/副岛主/工匠/探险家） | `TitleGrantable` 按成就授予 |
| **chat** | 岛屿频道/团队频道 | `IslandChatChannel` 能力注册 |
| **regions** | **岛屿保护的核心实现或补充** | regions 提供通用区域，SkyDream 注册岛屿范围为 region |
| **entitytracker** | 岛屿 Boss 战伤害排名、奖励结算 | 按岛屿维度记录 |
| **fishing** | 岛屿钓鱼小游戏/海钓特产 | 岛屿水域触发特殊钓鱼事件 |
| **questgps** | 岛屿任务目标导航 | `QuestGpsNavigable` 注册岛屿内目标 |
| **map** | 岛屿地图标记/传送 | `MapNavigable` 注册岛屿锚点 |
| **eventpacket** | 岛屿事件广播到客户端 | `EventBusCapability` 推送 |
| **rgb** | UI 美化/岛屿名称彩色渲染 | PAPI 占位符通过 rgb 模块渲染 |
| **qqbot** | 岛屿操作通知到 QQ 群 | `QQBotBroadcastable` 推送 |
| **tab** | TAB 列表显示岛屿信息 | `TabRefreshable` 扩展岛屿字段 |

---

## 3. SkyDream 向 AXS 注册的新能力接口

SkyDream 核心启动时通过 `IslandCapabilityRegistry` 向 AXS 宿主的 Capability 系统注册以下接口，供其他 AXS 模块反向调用。

### 3.1 IslandQueryable — 岛屿信息查询

```java
package xuanmo.aubade.api.capability;

public interface IslandQueryable {
    Optional<Island> getPlayerIsland(UUID player);
    Optional<Island> getIslandById(UUID islandId);
    boolean isInIslandWorld(Player player);
    int getIslandMemberCount(UUID islandId);
    double getIslandLevel(UUID islandId);
    String getIslandName(UUID islandId);
    UUID getIslandOwner(UUID islandId);
}
```

**注册方式**：
```java
// SkyDreamPlugin.java onEnable
context.registerCapability(IslandQueryable.class, islandManager);
```

**AXS 模块消费**：
```java
// 例如 market 模块打开岛屿商店时
IslandQueryable iq = context.getCapability(IslandQueryable.class);
Optional<Island> island = iq.getPlayerIsland(player.getUniqueId());
island.ifPresent(i -> openShop(player, i.getUniqueId()));
```

### 3.2 IslandEconomyCapable — 岛屿经济

```java
public interface IslandEconomyCapable {
    double getIslandBalance(UUID islandId);
    boolean deposit(UUID islandId, double amount);
    boolean withdraw(UUID islandId, double amount);
    boolean transfer(UUID fromIslandId, UUID toIslandId, double amount);
}
```

**联动 bank 组件**：bank 组件实现 `IslandEconomyCapable` 并注册；核心优先查询已注册的实现，若无则使用内部默认存储。

### 3.3 IslandPermissionCheckable — 岛屿权限

```java
public interface IslandPermissionCheckable {
    boolean hasPermission(UUID player, UUID islandId, IslandPermission perm);
    boolean isIslandOwner(UUID player, UUID islandId);
    boolean isIslandMember(UUID player, UUID islandId);
    boolean isCoopPlayer(UUID player, UUID islandId);
    boolean isBanned(UUID player, UUID islandId);
}
```

**使用场景**：AXS 的 regions 模块需要判断某玩家是否能在某岛屿区域破坏方块时，通过此接口查询。

### 3.4 IslandShopCapable — 岛屿商店

```java
public interface IslandShopCapable {
    void openIslandShop(Player player, UUID islandId);
    List<ShopListing> getIslandListings(UUID islandId);
    boolean createListing(UUID islandId, ShopListing listing);
    boolean removeListing(UUID islandId, String listingId);
}
```

**联动 market 组件**：market 组件实现此接口，允许玩家在岛屿内打开专属商店界面。

---

## 4. UI 桥接实现

### 4.1 ArcartXUiBridge 核心封装

```java
package xuanmo.aubade.core.ui.bridge;

public final class ArcartXUiBridge implements UiBridge {
    private final PacketBridgeAPI packetBridge;
    private final ClientBridgeAPI clientBridge;
    private final WaypointBridgeAPI waypointBridge;
    private final WorldTextureBridgeAPI textureBridge;
    private final PropBridgeAPI propBridge;
    private final AdyeshachNpcBridgeAPI npcBridge;
    private final UiRegistry registry;
    
    public ArcartXUiBridge(ModuleContext axsContext) {
        this.packetBridge = axsContext.packetBridge();
        this.clientBridge = axsContext.clientBridge();
        this.waypointBridge = axsContext.waypointBridge();
        this.textureBridge = axsContext.worldTextureBridge();
        this.propBridge = axsContext.propBridge();
        this.npcBridge = axsContext.adyeshachNpcBridge();
        this.registry = new UiRegistry();
    }
    
    @Override
    public boolean registerUi(String name, String uiId, File uiFile) {
        if (!packetBridge.isAvailable()) {
            registry.registerFallback(uiId, uiFile); // 无 ArcartX 时标记为降级
            return false;
        }
        UiRegistrationResult result = packetBridge.registerOrReloadUi(uiId, uiFile);
        if (result.success()) {
            registry.record(uiId, uiFile, result.runtimeUiId());
        }
        return result.success();
    }
    
    @Override
    public boolean openUi(Player player, String uiId) {
        if (!packetBridge.isAvailable()) {
            openFallbackInventory(player, uiId); // 回退到 Bukkit Inventory
            return true;
        }
        return packetBridge.openUi(player, registry.resolveRuntimeId(uiId));
    }
    
    @Override
    public boolean sendPacket(Player player, String uiId, String handler, Object payload) {
        if (!packetBridge.isAvailable()) return false;
        return packetBridge.sendPacket(player, registry.resolveRuntimeId(uiId), handler, payload);
    }
    
    @Override
    public void addIslandWaypoint(Player player, UUID islandId, String title, Location loc) {
        if (waypointBridge == null || !waypointBridge.available()) return;
        waypointBridge.addWaypoint(player, "aubade_island_" + islandId,
            title, "default", loc.getX(), loc.getY(), loc.getZ());
    }
    
    @Override
    public void spawnIslandNameTexture(Location center, String islandName) {
        if (textureBridge == null || !textureBridge.isAvailable()) return;
        textureBridge.spawnAtLocation(center.getWorld(), center.clone().add(0, 3, 0),
            "aubade_name_" + center.hashCode(), "&b&l" + islandName, 2.0, 0.5);
    }
    
    @Override
    public void bindQuickMenuKey(Player player) {
        if (propBridge == null || !propBridge.isAvailable()) return;
        propBridge.registerClientKeyBind("aubade_menu", "岛屿", "KEY_K",
            p -> openUi(p, "aubade_main"));
    }
}
```

### 4.2 降级策略

| 能力 | ArcartX 可用时 | ArcartX 不可用时（软依赖降级） |
|---|---|---|
| UI 面板 | ArcartX UI 客户端渲染 | Bukkit Inventory GUI（泛普适） |
| HUD | 客户端 HUD 覆盖 | ActionBar 消息 |
| 路标 | 客户端路标 | BossBar 方向指示 |
| 飘字 | 客户端飘字 | 无（不影响功能） |
| 文字贴图 | 客户端贴图 | 全息图 ArmorStand（若 HolographicDisplays 存在） |
| 按键 | 客户端按键绑定 | 命令别名（如 `/is` 已注册） |
| 聊天卡片 | 聊天卡片 | 普通聊天消息 |

---

## 5. 客户端→服务端 Packet 路由

SkyDream 核心需实现 `ClientPacketHandler`，处理客户端 UI 发起的操作：

```java
@Override
protected ClientPacketHandler createPacketHandler() {
    return (player, packetId, data) -> {
        // packetId 格式: "SKYDREAM_<ACTION>"
        if (!packetId.startsWith("SKYDREAM_")) return false;
        String action = packetId.substring(9);
        
        // 经济/背包操作必须切到主线程
        if (org.bukkit.Bukkit.isPrimaryThread()) {
            handlePacket(player, action, data);
        } else {
            org.bukkit.Bukkit.getScheduler().runTask(plugin,
                () -> handlePacket(player, action, data));
        }
        return true;
    };
}

private void handlePacket(Player player, String action, List<String> data) {
    Map<String, String> params = parseParams(data);
    switch (action) {
        case "ISLAND_CREATE" -> islandManager.createIsland(player, params.get("blueprint"));
        case "ISLAND_INVITE_ACCEPT" -> teamManager.acceptInvite(player, UUID.fromString(params.get("island")));
        case "ISLAND_KICK" -> teamManager.kickMember(player, UUID.fromString(params.get("target")));
        case "ISLAND_DEPOSIT" -> bankManager.deposit(player, Double.parseDouble(params.get("amount")));
        case "ISLAND_WITHDRAW" -> bankManager.withdraw(player, Double.parseDouble(params.get("amount")));
        case "ISLAND_UPGRADE" -> upgradeManager.purchaseUpgrade(player, params.get("upgradeId"));
        case "CHALLENGE_CLAIM" -> challengeManager.claimReward(player, params.get("challengeId"));
        case "BIOME_CHANGE" -> biomeManager.changeBiome(player, params.get("biomeId"));
        case "WARP_TELEPORT" -> warpManager.teleport(player, params.get("warpId"));
        case "VISIT_TELEPORT" -> visitManager.teleportToIsland(player, UUID.fromString(params.get("island")));
        default -> player.sendMessage("未知操作: " + action);
    }
}
```

---

## 6. 跨模块事件联动

### 6.1 SkyDream → AXS 事件推送

```java
// 岛屿等级变化时，推送到 battlepass 和 title 模块
public void onIslandLevelChange(IslandLevelChangeEvent event) {
    // battlepass：检查是否有"升级岛屿到X级"的任务
    EventBusCapability eventBus = context.getCapability(EventBusCapability.class);
    if (eventBus != null) {
        eventBus.publish("aubade.level_change", Map.of(
            "player", event.getPlayer(),
            "island", event.getIslandId(),
            "oldLevel", event.getOldLevel(),
            "newLevel", event.getNewLevel()
        ));
    }
    
    // title：检查是否达到称号解锁条件
    TitleGrantable title = context.getCapability(TitleGrantable.class);
    if (title != null) {
        if (event.getNewLevel() >= 100) {
            title.grantTitle(event.getPlayer(), "百级岛屿");
        }
    }
    
    // qqbot：广播重大升级
    QQBotBroadcastable qq = context.getCapability(QQBotBroadcastable.class);
    if (qq != null && event.getNewLevel() % 50 == 0) {
        qq.broadcast("🎉 恭喜 " + event.getPlayer().getName() + " 的岛屿升到 " + event.getNewLevel() + " 级！");
    }
}
```

### 6.2 AXS → SkyDream 事件消费

```java
// onlinerewards 按岛屿活跃度计算奖励倍数
public double calculateRewardMultiplier(Player player) {
    IslandQueryable iq = context.getCapability(IslandQueryable.class);
    if (iq == null) return 1.0;
    
    Optional<Island> island = iq.getPlayerIsland(player.getUniqueId());
    if (island.isEmpty()) return 1.0;
    
    int memberCount = iq.getIslandMemberCount(island.get().getUniqueId());
    double level = iq.getIslandLevel(island.get().getUniqueId());
    
    // 成员越多、等级越高，奖励倍数越高
    return 1.0 + (memberCount * 0.05) + (level * 0.001);
}
```

---

## 状态与优先级

- **状态**：策划中
- **优先级**：P0（决定 Aubade 区别于其他空岛插件的核心竞争力）

---

## 文件间引用

- [← 01 项目定位与架构总览](aubade-01-overview-d32eb4.md)
- [→ 03 核心数据模型与数据库](aubade-03-datamodel-d32eb4.md)
