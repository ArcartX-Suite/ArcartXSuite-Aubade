# ArcartX-Aubade 进度与计划

## 当前定位
ArcartX-Aubade 已完成 AXS 模块化收口，当前主线是继续把各功能 addon 从“有逻辑”补成“可入口、可验证、可交付”的闭环，同时按执行计划推进阶段 2 / 阶段 3 / 阶段 5。

## 已完成（已 push）
| commit | 里程碑 |
|---|---|
| `685e66a` | 动态发现注册游戏模式（ServiceLoader），并加固 `/island create` |
| `1179812` | 退役 orphan 模块 `aubade-api`，契约收口到 `axs-api` |
| `e2aac46` | 模块化打包收口：模块 jar 不再内嵌 `axs-api`，退役 `AubadePlugin` |
| `154343d` | UI 收口到宿主 `packetBridge`，去除反射适配层 |
| `e27b896` | 清理插件语义残留，补齐岛屿保护 listener 事件链，变量命名统一到 `core` |

## 已完成（本地待推）
| 功能 | 入口 / 关键点 | 说明 |
|---|---|---|
| 岛屿空间索引 | `IslandGrid` + `IslandManagerImpl#getIslandAt()` | 用网格/空间索引替代线性遍历，创建/保存/删除时同步索引 |
| 岛屿升级 | `/island upgrade` | `UpgradesAddon` + `UpgradeCommand`；Vault 扣玩家个人余额，升级结果落到岛屿并保存 |
| 岛屿参观 | `/island visit <玩家名>`、`/island leave` | `VisitAddon` + `IslandVisitCommand`；记录返回点，`leave` 可退出参观 |
| 岛屿展示推荐 | `/island checkmeout`、`/island checkmeout list`、`/island checkmeout vote <玩家>` | `CheckMeOutAddon` + `IslandCheckMeOutCommand`；提交自己的岛屿、列出推荐、投票 |
| 岛屿传送点 | `/island warp list`、`/island warp <名称>`、`/island setwarp <名称>`、`/island delwarp <名称>` | `WarpsAddon` 接通既有命名 warp；`warp list` 作为列出入口 |
| 岛屿群系切换 | `/island biome <群系>` | `BiomesAddon` + `IslandBiomeCommand`；按保护范围批量改 biome，并刷新 chunk |
| 管理命令补全 | `/isadmin reload`、`/isadmin purge`、`/isadmin info <玩家>`、`/isadmin delete <玩家> confirm`、`/isadmin setspawn` | 补齐 `DefaultAdminCommand` help 里已宣传但此前未注册的 5 个管理子命令：`reload`→`onReload()`；`purge` 只删孤立岛屿（owner 无效且非 purge-protected）；`info/delete` 按玩家名解析岛屿；`setspawn` 落到 `CoreConfig` 的 `world.spawn.*` 并 `save()` |

## 进行中 / 待办
| 阶段 / 条目 | 状态 | 优先级 | 说明 |
|---|---|---|---|
| 阶段 2.20 集成测试 | 未完成 | P0 | 计划要求做创建→邀请→踢出→删除的链路验收；当前仓内没有可直接复用的测试基建，`gradlew.bat test` 仅为 `NO-SOURCE` |
| 阶段 3.1~3.6 UI 统一管理与 packetBridge / UI YAML 收口 | 已完成主线收口，少量入站动作待后续补齐 | P0~P1 | 已接线 `AdminUiPacketDispatcher`（admin 先于 island）；已把 main/create/invite/top，以及 warp/biome/member(info)/team(settings)/level 的无参入口接到对应 UI；本轮补齐主菜单「银行/边界」按钮及 `SKYDREAM_BANK/BORDER open`，并将 `BANK_DEPOSIT/WITHDRAW`、`BORDER_SHRINK/EXPAND/TOGGLE`、`TEAM_INVITE_OPEN` 接入现有 addon/UI；`TEAM_TRANSFER_OPEN`/`TEAM_RENAME_OPEN`/`WARP_CREATE` 仅保留明确中文提示，不束构后端；`gradlew.bat build` / `gradlew.bat test`（13 用例）均绿 |
| 阶段 5.8 岛屿银行 / AXS warehouse 统一体系 | 计划中 / 未实施 | P1 | 目标是让“岛屿仓库 + 岛屿银行”统一由 AXS warehouse 提供存储与经济能力，Aubade 侧只做接入；当前 `UpgradesAddon` 已明确改为扣玩家个人 Vault 余额，岛屿银行暂不作为升级资金来源 |
| 阶段 5 Redis 跨服同步 v1 | 已完成（AXS CrossServerAPI + invalidation-based） | P1 | 采用 AXS cross-server bus，消息仅携带 eventType/islandId/ownerId/sourceNodeId/timestamp；sync.enabled/channel 配置门控；save/delete 挂钩已接入；fake-bus MockBukkit 测试已覆盖。bank/border/level 若独立持久化则留作后续。 |

- 后续待办：岛主转让。
- 后续待办：岛屿改名。
- 后续待办：warp 板内创建（名称输入 UI）。

## 已知约束 / 部署提醒
- 当前代码以 AXS 模块形态组织，部署时需要重新构建模块 jar 再替换服务端。
- 现阶段 `gradlew.bat test` 没有可执行测试用例，测试基建仍需后续补齐。
- `UpgradesAddon` 的升级资金来源已明确是**玩家个人 Vault 余额**，不是 `Island.getBankBalance()`。
- 新增入口命令都沿用现有 `CompositeCommand` / `registerSubCommand("island", ...)` 范式，便于继续扩展。
- 当前文档反映的是“已 push 的基础地基 + 本地未推功能闭环 + 计划中待办”，不是单纯的代码实现清单。