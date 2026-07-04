# Aubade 架构重设方案（调研 + 建议，待确认后再编码）

> 目的：解决 Aubade 当前"独立 Bukkit 插件 vs AXS 模块"的根本架构错配（UI 反射
> `com.arcartx.packet.PacketBridgeAPI` 找不到类、被当致命错误导致自禁用）。
> 本文只做方案，**未改任何业务代码**，等你拍板后再动手。

---

## 0. 一句话结论

推荐把 Aubade **改造成 ArcartXSuite(AXS) 模块**（继承 `AbstractAXSModule`，打成
放进 `plugins/ArcartXSuite/modules/` 的模块 jar）。这是你服务器 UI 能力的正主，
也和你让我参考的 ArcartXSuite 内置模块（announcer 等）完全一致。

**Blink 不需要用**：AXS 模块是纯 `id("java")` 工程，只 `compileOnly` axs-api，
不经过 Blink；而且 Blink 的 `blink-libraries` 是"运行时联网下载"依赖，在你这台
离线服上会重演之前 HikariCP 的 SSL 下载失败问题，得不偿失。

---

## 1. 现状问题回顾

- Aubade 现在是**独立 Bukkit 插件**：自带 `plugin.yml`（`main: AubadePlugin`、
  `depend: ArcartX`），自己 `onEnable`。
- UI 层用反射找 `com.arcartx.packet.PacketBridgeAPI` —— **这个类不存在**。
  真正的发包/UI 桥接在 **AXS** 里：`xuanmo.arcartxsuite.api.bridge.PacketBridgeAPI`，
  由 AXS 的 `ModuleContext` 提供给"模块"。独立插件拿不到 `ModuleContext`。
- 结论：不是改个类名就能修好，是"运行形态"不对。要么变成 AXS 模块拿 ModuleContext，
  要么改用 ArcartX 核心自己的公共 UI API（见方案 B）。

---

## 2. 调研发现（关键事实，全部有据）

### 2.1 AXS 模块系统 = 宿主 + 模块 Jar

来源：`ArcartXSuite/MODULAR-README.md`、反编译 `axs-core` 的 `ModuleRegistry`、
`ModuleSignatureVerifier`、`axs-core/.../config.yml`。

- 部署结构：
  ```
  plugins/
    ArcartXSuite.jar            ← 宿主（axs-core）
    ArcartXSuite/
      config.yml                ← 模块启用开关
      modules/                  ← 按需放入模块 jar（AXS-Xxx-*.jar）
  ```
- 宿主启动时 `ModuleRegistry.loadAll()`：扫描 `modules/*.jar` → 读每个 jar 里的
  `module.yml` → 按 `depends` 拓扑排序 → 实例化 `main` 类 → 调 `onEnable(ModuleContext)`。
- **启用是显式的**：`isModuleEnabled` = `config.yml` 的
  `modules.<id>.enabled`，**默认 false**。所以要在 config.yml 加：
  ```yaml
  modules:
    aubade:
      enabled: true
  ```
- **签名验证默认关闭**（重要，扫清最大顾虑）：`ModuleSignatureVerifier` 只有当
  config.yml 里填了 `module-signature-public-keys`（Ed25519 公钥）才生效；留空时
  `verify()` 直接放行。config.yml 原注释明确写着"如果你使用**第三方/私人定制模块**…"
  —— 即第三方模块是官方支持的用法。
- **云端授权也默认关闭**：`cloud.qq/apiKey` 留空即不连云端。`.axb` 加密、逐类加密、
  云端下发是官方模块的发行保护流程，**自建/本地模块用普通 jar 即可**，不用碰。

### 2.2 AXS 模块契约（Aubade 要照着写的模板）

来源：`axs-api` 源码（`AXSModule`/`AbstractAXSModule`/`ModuleContext`/`UiBinding`/
`ModuleCommandHandler`）+ 内置模块 `modules/announcer/.../AnnouncerModule.java`。

- 主类两种写法：
  - 实现 `AXSModule`：`descriptor()` / `onEnable(ModuleContext)` / `onDisable()`
    / `onReload()` / `isReady()`。
  - **继承 `AbstractAXSModule`（推荐）**：只需实现
    `descriptor()` / `loadConfiguration(File)` / `startService()` / `stopService()`，
    框架把 `plugin/logger/dataFolder/packetBridge/clientBridge/...` 作为 protected
    字段注入；配置体检、messages、UI 注册、命令/监听/能力注册都有现成 helper。
- `module.yml`：
  ```yaml
  id: aubade
  name: Aubade
  version: 1.0.0
  main: xuanmo.aubade.module.AubadeModule
  api-version: 1.0
  depends: []
  softdepends: []
  external-depends: []
  external-softdepends: []
  ```
- **UI（正解，直接修掉原报错）**：`context.packetBridge()` 拿到
  `PacketBridgeAPI`，或用 `AbstractAXSModule#registerModuleUi(resourcePath, exportPath,
  uiId, registerOnEnable)`（内部走 `prepareUiBinding` → `packetBridge.registerOrReloadUi`）。
  UI 的 YAML 放 `src/main/resources/arcartx/ui/*.yml`。
- **命令**：实现 `ModuleCommandHandler` → 自动注册成 **`/axs aubade ...`** 子命令
  （`commandId()` / `actions()` / `onCommand()` / `onTabComplete()`）。
- **`ModuleContext` 能提供**（节选）：`plugin/logger/dataFolder/uiFolder`、
  `packetBridge/clientBridge/itemStackBridge/propBridge`、`currencyManager`、
  `crossServer`、`placeholderResolver`、`accountTypeService`、`registerListener`、
  `registerCommand`、`registerCapability/getCapability`、`getModule`、`hasPlugin`。
- **存储**：axs-api 提供 `storage.StorageDescriptor`（`sqlite(file)` / `mysql(...)`）
  + `AbstractModuleRepository` + `capability.DatabaseMigratable`（`/axs migrate` 统一迁移）。
  即模块可复用宿主的 SQLite/MySQL 基建，无需自己 shade HikariCP。

### 2.3 Blink 是什么、要不要用

来源：`Blink` 仓库 + `ArcartX-Wiki`。

- Blink = **Gradle 插件 + 运行时框架**：构建期用 ASM 生成 `BlinkGeneratedMain`
  与 `plugin.yml`，自动套 Shadow，把自身运行时包重定位到 `<pkg>.blink`；运行期
  `DependencyLoader` 读 `plugin.yml` 的 `blink-libraries` 并**联网下载注入**。
- 用注解 `@Awake(LOAD/ENABLE/ACTIVE/DISABLE)` 管生命周期，主打 Kotlin。
- **对 Aubade 的判断**：
  - 走 AXS 模块路线 → **完全不需要 Blink**（announcer 的 build.gradle.kts 就是
    `plugins { id("java") }` + `compileOnly(project(":axs-api"))`，无 Blink、无 plugin.yml）。
  - `blink-libraries` 运行时下载会在**离线服**上重演 SSL 失败（和之前删掉
    plugin.yml `libraries:` 的原因同源），所以即使保留独立插件也不建议靠它管依赖。

### 2.4 ArcartX 核心自带的公共 UI API（方案 B 用）

来源：`ArcartX-Wiki/.../999_server_api/9_ui_registry.mdx` + `10_ui_handler.mdx`。

- 任意独立插件可用 `ArcartXAPI.getUIRegistry()`：`register(id, file/yaml)` /
  `open(player,id)` / `sendPacket(...)` / `run(...)` / `unregister(id)`；
  或继承 `UIHandler`（构造即自动注册、绑定 OPEN/CLOSE/PACKET 回调）。
- 即：**不变成 AXS 模块也能做 UI**，直接用 ArcartX 核心 API 即可。这是把当前
  错误反射目标换成正确 API 的最小改法。

---

## 3. 方案对比

| 维度 | A. 改成 AXS 模块（荐） | B. 保留独立插件 + ArcartX 公共 UI API | C. 维持现状（UI 降级） |
|---|---|---|---|
| UI 能力 | ✅ ModuleContext.packetBridge，完整 | ✅ ArcartXAPI.getUIRegistry | ❌ 关闭 |
| 与你参考的 AXS 架构一致 | ✅ 完全一致 | ➖ 部分 | ❌ |
| 额外白拿的能力 | 跨服/货币/能力/账号/配置体检/统一存储迁移 | 仅 ArcartX UI | 无 |
| 入口/命令 | `/axs aubade ...`（命名空间变化） | 保留 `/island`、`/is` 等 | 保留 |
| 依赖形态 | 依赖 **ArcartXSuite** 为宿主（硬） | 仅依赖 **ArcartX**（软/硬可选） | 依赖 ArcartX |
| 构建 | `compileOnly axs-api`，纯 jar，无需 Blink/shadow | 现有 shadow 工程基本不变 | 不变 |
| 改造量 | 大（入口/生命周期/UI/命令/存储重构） | 中（只重写 UI 桥接层） | 已完成 |
| Blink | 不需要 | 不需要（可选，不建议） | 不需要 |

---

## 4. 推荐

**首选 A（AXS 模块）**，条件是你认可"Aubade 作为该服 AXS 生态内的模块运行、
命令走 `/axs aubade`、以 ArcartXSuite 为宿主"。理由：

1. 直接、彻底修掉 UI 根因（拿到正牌 `PacketBridgeAPI`）。
2. 与你指定的参考实现（ArcartXSuite 内置模块）一模一样，后续可长期对齐官方演进。
3. 白拿跨服、货币、能力互通、统一账号、配置体检、统一存储迁移等基建，skyblock
   这类玩法插件能省很多轮子。

**若你更看重"Aubade 保持独立、命令仍是 `/island`、不想被 AXS 宿主绑定"**，
则选 **B**：改造量更小，只把 UI 桥接层从错误反射换成 `ArcartXAPI.getUIRegistry()`。

（C 只是当前的降级兜底，UI 用不了，不作为最终形态。）

---

## 5. 需要你拍板的决策点

1. **A 还是 B？**（是否接受 Aubade 变成 AXS 模块 / 命令变 `/axs aubade`）
2. 若选 A：**命令命名空间**能否接受 `/axs aubade ...`？（想保留顶层 `/island`
   需要额外 hack，非 AXS 常规做法，建议接受 `/axs aubade` 或做别名）
3. **存储**：走 AXS 统一存储（`StorageDescriptor`+`AbstractModuleRepository`，
   支持 SQLite/MySQL 一键迁移）还是沿用 Aubade 现有自带 Hikari+JDBC？
4. **skyblock 子模块**：`aubade-game-skyblock` 作为 Aubade 模块内的子系统，
   还是拆成独立的第二个 AXS 模块？
5. 目标运行环境确认：该服已装 **ArcartXSuite-1.3.2**（有），A 方案以它为硬宿主 OK？

---

## 6. 迁移阶段计划（选定 A 后执行，暂不编码）

- **P0 依赖与骨架**：新增 `aubade-module`（或改造 aubade-core）为 `id("java")`
  工程，`compileOnly` axs-api（+ 需要时 axs-core）；新建 `AubadeModule extends
  AbstractAXSModule` + `module.yml`；产出普通 jar。
- **P1 生命周期迁移**：把 `AubadePlugin.onEnable/onDisable` 的初始化搬进
  `startService/stopService`；`JavaPlugin`/`Logger`/`dataFolder` 改用注入字段。
- **P2 UI 重写**：删掉 `ArcartXUiBridge` 反射，改用 `registerModuleUi` /
  `packetBridge`；UI YAML 迁到 `arcartx/ui/`。
- **P3 命令迁移**：现有 20+ 命令收敛到 `ModuleCommandHandler`（`/axs aubade`）。
- **P4 存储**（按决策 3）：保留自带 Hikari，或迁到 AXS 统一存储 + 实现
  `DatabaseMigratable`。
- **P5 配置/监听/能力**：`registerListener`、配置体检 `configSpecs()`、必要时
  `registerCapability`。
- **P6 实机验证**：放 `plugins/ArcartXSuite/modules/`，config.yml 开启，
  启动看 `/axs` 列表、命令、建库；UI 面板需真人客户端另测。

---

## 7. 风险与备注

- A 是较大重构，`plugin.yml` 形态、入口、命令 UX 都会变，需你接受。
- 若将来要走官方云端/加密分发（`.axb`），得进一步接 AXS 的混淆/签名流程；
  自建本地部署**不需要**。
- 无 MC 客户端，UI 面板仍只能做到"注册成功/发包不报错"级验证，真人交互需你测。
- 按隧道规则：以上仅本地改动，不推 GitHub。
```
