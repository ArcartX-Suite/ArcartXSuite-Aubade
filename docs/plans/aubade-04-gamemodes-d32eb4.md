# ArcartX-Aubade 开发蓝图 — 04 游戏模式设计

**一句话总结**：6 种游戏模式的核心机制、世界生成算法、蓝图系统与专属规则，每种模式只需实现 `GameModeAddon` 接口即可接入核心框架。

---

## 1. 游戏模式基类

```java
public interface GameModeAddon extends SkyAddon {
    String getGameModeId();              // 唯一标识，如 "skyblock"
    String getFriendlyName();            // 显示名称
    WorldSettings getWorldSettings();    // 世界配置
    void registerWorlds(WorldManager wm);
    ChunkGenerator getOverworldGenerator();
    default ChunkGenerator getNetherGenerator() { return null; }
    default ChunkGenerator getEndGenerator() { return null; }
    void registerCommands(CommandManager cm);
    List<Blueprint> getDefaultBlueprints();
    default void onIslandCreate(Island island) {}
}
```

每个 GameMode 独立为一个模块（`aubade-game-*`），有自己的 `addon.yml` 描述符和蓝图资源。

---

## 2. SkyBlock（经典空岛）

**核心机制**：虚空世界，基岩出生平台，开局一棵树+一个箱子。

| 维度 | 配置 |
|---|---|
| Overworld | `seaLevel=0`，虚空，基岩平台 |
| Nether | 可选，顶部基岩层，禁用地狱堡垒 |
| End | 可选，末地岛间距缩小 |

**蓝图系统**：
- 格式：JSON，包含 `version`、`name`、`blocks[]`、`entities[]`、`spawnOffset`
- 粘贴：异步分 tick 批次，完成后设置 spawn 点
- 内置蓝图：经典空岛、硬核空岛（仅基岩）、沙漠绿洲、雪域冰岛、蘑菇岛、花岛

**专属规则**：
- 脚手架保护：防止玩家误删出生平台基岩
- 虚空保护：掉落虚空自动传回家
- 初始物资：通过箱子的 NBT 数据配置

---

## 3. AcidIsland（酸海岛）

**核心机制**：海面以下为酸水，下雨对无庇护玩家造成伤害。

| 机制 | 实现 |
|---|---|
| 酸水 | 自定义 damage，木质船免疫 |
| 酸雨 | 玩家头顶无方块时持续扣血 |
| 淡水获取 | 漏斗+沙子过滤 → 淡水桶 |
| 生物群系 | 强制覆盖为自定义酸海群系 |

**生成特点**：
- 海面 Y=50
- 海底有珊瑚礁、沉船残骸
- 岛屿生成在海面上方

---

## 4. SkyGrid（网格世界）

**核心机制**：固定间距的随机方块网格散布在虚空中。

**生成算法**：
- 间距：每 4 格一个方块
- 权重分布：石头 70%、泥土 10%、木头 5%、矿石 5%、特殊 5%
- 深度分布：煤 Y<64、铁 Y<48、金 Y<32、钻石 Y<16
- 底部：一层基岩防止掉落

**专属规则**：
- 禁止自然生物群系，全图统一为虚空/平原混合
- 禁用自然洞穴生成
- 矿物生成率可通过配置调整

---

## 5. CaveBlock（洞穴方块）

**核心机制**：实心石头填充的世界，仅保留洞穴系统。

**生成特点**：
- 地表：基岩封顶
- 内部：石头填充 + 自然洞穴系统
- 矿物：按深度正常分布
- 光照：玩家周围无光照源则缓慢掉血（光照生存机制）

**专属规则**：
- 禁用天气变化
- 禁用日夜循环（恒定为夜晚或配置值）
- 作物生长依赖人工光源

---

## 6. OneBlock（单方块）

**核心机制**：单个可再生的破坏方块，破坏后立即在原位生成新方块。

**阶段系统**：

| 阶段 | 破坏次数 | 产出类型 | 特殊事件 |
|---|---|---|---|
| 1 | 0-100 | 主世界方块（泥土/石头/木头/矿石） | 无 |
| 2 | 100-300 | 下界方块（下界岩/灵魂沙/石英） | 小概率烈焰人 |
| 3 | 300-600 | 末地方块（末地石/紫颂花） | 末影人 |
| 4 | 600+ | 混合稀有（钻石/远古残骸/龙蛋） | Boss 阶段 |
| Boss | 每 500 次 | 混合 + 怪物潮 | 触发 Boss 战 |

**配置化**：阶段定义通过 JSON 阶段表，服务器管理员可自定义每个阶段的方块权重和事件概率。

---

## 7. Boxed（盒子世界）

**核心机制**：初始边界 5×5 区块，完成成就解锁边界扩展。

**核心循环**：
1. 玩家出生在小边界盒内
2. 完成系统成就（如"收集 64 个木头"）
3. 边界向指定方向扩展 +1 区块
4. 新区域带来新的资源和挑战

**盒内规则覆盖**：
- PvP、刷怪、天气等规则可每盒独立配置
- 盒外：虚空伤害（立即死亡）
- 盒内：可配置禁用某些游戏机制

**成就系统**：
- 内置成就库：资源收集、生物击杀、探索、建造
- 自定义成就：通过 JSON 配置

---

## 8. 蓝图系统详细设计

### 8.1 JSON 格式

```json
{
  "version": 1,
  "name": "经典空岛",
  "description": "一棵树 + 一个箱子",
  "author": "SkyDream Team",
  "size": {"x": 10, "y": 10, "z": 10},
  "spawnOffset": {"x": 0.5, "y": 65.0, "z": 0.5},
  "blocks": [
    {"x": 0, "y": 64, "z": 0, "type": "minecraft:grass_block"},
    {"x": 0, "y": 65, "z": 0, "type": "minecraft:oak_log"},
    {"x": 0, "y": 66, "z": 0, "type": "minecraft:oak_leaves"},
    {"x": 1, "y": 64, "z": 0, "type": "minecraft:chest",
     "nbt": "{Items:[{Slot:0b,id:\"minecraft:lava_bucket\",Count:1b}]}"
