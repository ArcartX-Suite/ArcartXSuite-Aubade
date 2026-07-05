package xuanmo.aubade.core.ui.packet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.aubade.island.IslandMember;
import xuanmo.arcartxsuite.api.aubade.island.Role;
import xuanmo.arcartxsuite.api.aubade.island.Island;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.features.bank.BankAddon;
import xuanmo.aubade.core.features.border.BorderAddon;
import xuanmo.aubade.core.features.level.LevelAddon;
import xuanmo.aubade.core.features.level.LevelTopCache;
import xuanmo.aubade.core.warp.IslandWarpHelper;
import xuanmo.aubade.core.island.IslandManagerImpl;

public class IslandUiPacketHandler extends BasePacketHandler {

  private static final String PREFIX = "SKYDREAM_";

  public IslandUiPacketHandler(AubadeCore core) {
    super(core);
  }

  @Override
  public boolean canHandle(String packetId) {
    return packetId != null && packetId.startsWith(PREFIX);
  }

  @Override
  protected void doHandle(Player player, String packetId, List<String> data) {
    String action = packetId.substring(PREFIX.length());
    Map<String, String> params = parseParams(data);
    IslandManagerImpl islandManager = core.getLifecycleManager().getIslandManager();

    switch (action) {
      case "MAIN" -> {
        if (!isOpenAction(data)) {
          player.sendMessage("§c未知 UI 动作: " + action);
          return;
        }
        Optional<Island> island = islandManager.getIslandByOwner(player.getUniqueId());
        if (!core.getUiManager().sendPacket(player, "aubade_main", "init", buildMainPayload(player, island))) {
          player.sendMessage("§c主菜单当前不可用。");
        }
      }
      case "TOP" -> {
        if (!isOpenAction(data)) {
          player.sendMessage("§c未知 UI 动作: " + action);
          return;
        }
        if (!core.getUiManager().openUi(player, "aubade_top")) {
          player.sendMessage("§c排行榜面板当前不可用。");
          return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("category", "岛屿等级排行榜");
        payload.put("top", buildTop3());
        if (!core.getUiManager().sendPacket(player, "aubade_top", "init", payload)) {
          player.sendMessage("§c排行榜面板当前不可用。");
        }
      }
      case "SETTINGS" -> {
        if (!isOpenAction(data)) {
          player.sendMessage("§c未知 UI 动作: " + action);
          return;
        }
        Optional<Island> island = islandManager.getIslandByOwner(player.getUniqueId());
        if (island.isEmpty()) {
          player.sendMessage("§c你还没有岛屿。");
          return;
        }
        if (!core.getUiManager().openUi(player, "team_settings")) {
          player.sendMessage("§c岛屿设置面板当前不可用。");
          return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("settings", buildSettingsPayload(island.get()));
        if (!core.getUiManager().sendPacket(player, "team_settings", "init", payload)) {
          player.sendMessage("§c岛屿设置面板当前不可用。");
        }
      }
      case "BANK" -> {
        if (!isOpenAction(data)) {
          return;
        }
        Optional<Island> island = islandManager.getIslandByOwner(player.getUniqueId());
        if (island.isEmpty()) {
          player.sendMessage("§c你还没有岛屿。");
          return;
        }
        Optional<BankAddon> bankAddon = resolveBankAddon();
        if (bankAddon.isEmpty()) {
          player.sendMessage("§c岛屿银行组件未初始化。");
          return;
        }
        if (!openUiAndSend(player, "island_bank", "init", buildBankPayload(bankAddon.get(), island.get()))) {
          player.sendMessage("§c岛屿银行面板当前不可用。");
        }
      }
      case "BORDER" -> {
        if (!isOpenAction(data)) {
          return;
        }
        Optional<Island> island = islandManager.getIslandByOwner(player.getUniqueId());
        if (island.isEmpty()) {
          player.sendMessage("§c你还没有岛屿。");
          return;
        }
        Optional<BorderAddon> borderAddon = resolveBorderAddon();
        if (borderAddon.isEmpty()) {
          player.sendMessage("§c岛屿边界组件未初始化。");
          return;
        }
        if (!openUiAndSend(player, "border_settings", "init", buildBorderPayload(borderAddon.get(), island.get()))) {
          player.sendMessage("§c岛屿边界面板当前不可用。");
        }
      }
      case "BANK_DEPOSIT" -> {
        Optional<Island> island = islandManager.getIslandByOwner(player.getUniqueId());
        if (island.isEmpty()) {
          player.sendMessage("§c你还没有岛屿。");
          return;
        }
        Optional<BankAddon> bankAddon = resolveBankAddon();
        if (bankAddon.isEmpty()) {
          player.sendMessage("§c岛屿银行组件未初始化。");
          return;
        }
        double amount = resolveAmount(params);
        if (amount <= 0) {
          player.sendMessage("§c存入金额必须大于 0。");
          return;
        }
        if (!bankAddon.get().deposit(island.get().getUniqueId(), amount)) {
          player.sendMessage("§c存入失败。");
          return;
        }
        player.sendMessage("§a已存入 §e" + amount + " §a到岛屿银行。");
        if (!core.getUiManager().sendPacket(player, "island_bank", "update", buildBankPayload(bankAddon.get(), island.get()))) {
          player.sendMessage("§c岛屿银行面板当前不可用。");
        }
      }
      case "BANK_WITHDRAW" -> {
        Optional<Island> island = islandManager.getIslandByOwner(player.getUniqueId());
        if (island.isEmpty()) {
          player.sendMessage("§c你还没有岛屿。");
          return;
        }
        Optional<BankAddon> bankAddon = resolveBankAddon();
        if (bankAddon.isEmpty()) {
          player.sendMessage("§c岛屿银行组件未初始化。");
          return;
        }
        double amount = resolveAmount(params);
        if (amount <= 0) {
          player.sendMessage("§c取出金额必须大于 0。");
          return;
        }
        if (!bankAddon.get().withdraw(island.get().getUniqueId(), amount)) {
          player.sendMessage("§c岛屿银行余额不足。");
          return;
        }
        player.sendMessage("§a已从岛屿银行取出 §e" + amount + " §a。");
        if (!core.getUiManager().sendPacket(player, "island_bank", "update", buildBankPayload(bankAddon.get(), island.get()))) {
          player.sendMessage("§c岛屿银行面板当前不可用。");
        }
      }
      case "BORDER_SHRINK" -> {
        Optional<Island> island = islandManager.getIslandByOwner(player.getUniqueId());
        if (island.isEmpty()) {
          player.sendMessage("§c你还没有岛屿。");
          return;
        }
        Optional<BorderAddon> borderAddon = resolveBorderAddon();
        if (borderAddon.isEmpty()) {
          player.sendMessage("§c岛屿边界组件未初始化。");
          return;
        }
        int step = resolveStep(params);
        if (step <= 0) {
          player.sendMessage("§c调整幅度必须大于 0。");
          return;
        }
        int currentSize = borderAddon.get().getBorderSize(island.get());
        int targetSize = Math.max(1, currentSize - step);
        if (targetSize == currentSize) {
          player.sendMessage("§c边界已经不能再缩小了。");
          return;
        }
        borderAddon.get().setBorderOverride(island.get().getUniqueId(), targetSize);
        borderAddon.get().applyBorder(player);
        player.sendMessage("§a边界半径已调整为 §e" + targetSize + "§a。");
        if (!core.getUiManager().sendPacket(player, "border_settings", "update", buildBorderPayload(borderAddon.get(), island.get()))) {
          player.sendMessage("§c岛屿边界面板当前不可用。");
        }
      }
      case "BORDER_EXPAND" -> {
        Optional<Island> island = islandManager.getIslandByOwner(player.getUniqueId());
        if (island.isEmpty()) {
          player.sendMessage("§c你还没有岛屿。");
          return;
        }
        Optional<BorderAddon> borderAddon = resolveBorderAddon();
        if (borderAddon.isEmpty()) {
          player.sendMessage("§c岛屿边界组件未初始化。");
          return;
        }
        int step = resolveStep(params);
        if (step <= 0) {
          player.sendMessage("§c调整幅度必须大于 0。");
          return;
        }
        int currentSize = borderAddon.get().getBorderSize(island.get());
        int maxSize = resolveMaxBorderSize(currentSize);
        int targetSize = Math.min(maxSize, currentSize + step);
        if (targetSize == currentSize) {
          player.sendMessage("§c边界已经不能再扩大了。");
          return;
        }
        borderAddon.get().setBorderOverride(island.get().getUniqueId(), targetSize);
        borderAddon.get().applyBorder(player);
        player.sendMessage("§a边界半径已调整为 §e" + targetSize + "§a。");
        if (!core.getUiManager().sendPacket(player, "border_settings", "update", buildBorderPayload(borderAddon.get(), island.get()))) {
          player.sendMessage("§c岛屿边界面板当前不可用。");
        }
      }
      case "BORDER_TOGGLE" -> {
        Optional<Island> island = islandManager.getIslandByOwner(player.getUniqueId());
        if (island.isEmpty()) {
          player.sendMessage("§c你还没有岛屿。");
          return;
        }
        Optional<BorderAddon> borderAddon = resolveBorderAddon();
        if (borderAddon.isEmpty()) {
          player.sendMessage("§c岛屿边界组件未初始化。");
          return;
        }
        borderAddon.get().setShowBorder(!borderAddon.get().isShowBorder());
        borderAddon.get().applyBorder(player);
        player.sendMessage(borderAddon.get().isShowBorder() ? "§a已显示边界。" : "§e已隐藏边界。");
        if (!core.getUiManager().sendPacket(player, "border_settings", "update", buildBorderPayload(borderAddon.get(), island.get()))) {
          player.sendMessage("§c岛屿边界面板当前不可用。");
        }
      }
      case "TEAM_INVITE_OPEN" -> {
        if (core.getUiManager() != null && core.getUiManager().openUi(player, "aubade_invite")) {
          return;
        }
        player.sendMessage("§c邀请面板当前不可用。");
      }
      case "TEAM_TRANSFER_OPEN" -> {
        Optional<Island> island = islandManager.getIslandByOwner(player.getUniqueId());
        if (island.isEmpty()) {
          player.sendMessage("§c你还没有岛屿。");
          return;
        }
        if (!openUiAndSend(player, "team_transfer", "init", buildTransferPayload(island.get()))) {
          player.sendMessage("§c岛主转让面板当前不可用。");
        }
      }
      case "TEAM_TRANSFER_CONFIRM" -> {
        Optional<Island> island = islandManager.getIslandByOwner(player.getUniqueId());
        if (island.isEmpty()) {
          player.sendMessage("§c你还没有岛屿。");
          return;
        }
        String rawTarget = params.get("target");
        if (rawTarget == null || rawTarget.isBlank()) {
          player.sendMessage("§c请选择要转让的成员。");
          return;
        }
        UUID targetUuid;
        try {
          targetUuid = UUID.fromString(rawTarget.trim());
        } catch (IllegalArgumentException ex) {
          player.sendMessage("§c成员信息无效。");
          return;
        }
        Island sourceIsland = island.get();
        IslandMember targetMember = sourceIsland.getMembers().get(targetUuid);
        if (targetMember == null) {
          player.sendMessage("§c该成员不在你的岛屿中。");
          return;
        }
        if (targetUuid.equals(sourceIsland.getOwner())) {
          player.sendMessage("§c不能把岛主转让给自己。");
          return;
        }
        if (!islandManager.transferIsland(sourceIsland, targetUuid)) {
          player.sendMessage("§c岛主转让失败。");
          return;
        }
        String targetName = resolveMemberName(targetUuid);
        player.sendMessage("§a已将岛主转让给 §e" + targetName + "§a。");
        refreshMainPanel(player, islandManager);
        Player targetPlayer = Bukkit.getPlayer(targetUuid);
        if (targetPlayer != null) {
          refreshMainPanel(targetPlayer, islandManager);
        }
      }
      case "TEAM_RENAME_OPEN" -> {
        Optional<Island> island = islandManager.getIslandByOwner(player.getUniqueId());
        if (island.isEmpty()) {
          player.sendMessage("§c你还没有岛屿。");
          return;
        }
        if (!openUiAndSend(player, "island_rename", "init", buildRenamePayload(island.get()))) {
          player.sendMessage("§c岛屿改名面板当前不可用。");
        }
      }
      case "TEAM_RENAME_CONFIRM" -> {
        Optional<Island> island = islandManager.getIslandByOwner(player.getUniqueId());
        if (island.isEmpty()) {
          player.sendMessage("§c你还没有岛屿。");
          return;
        }
        String newName = params.get("name");
        if (newName == null) {
          newName = params.get("value");
        }
        if (newName == null) {
          player.sendMessage("§c请输入新的岛屿名称。");
          return;
        }
        newName = newName.trim();
        if (newName.isEmpty()) {
          player.sendMessage("§c请输入新的岛屿名称。");
          return;
        }
        if (newName.length() > 32) {
          player.sendMessage("§c岛屿名称不能超过 32 个字符。");
          return;
        }
        Island sourceIsland = island.get();
        sourceIsland.setName(newName);
        islandManager.saveIsland(sourceIsland);
        player.sendMessage("§a岛屿名称已修改为 §e" + newName + "§a。");
        refreshMainPanel(player, islandManager);
        if (!openUiAndSend(player, "team_settings", "init", buildSettingsPayload(sourceIsland))) {
          player.sendMessage("§c岛屿设置面板当前不可用。");
        }
      }
      case "WARP_CREATE" -> {
        Optional<Island> island = islandManager.getIslandByOwner(player.getUniqueId());
        if (island.isEmpty()) {
          player.sendMessage("§c你还没有岛屿。");
          return;
        }
        if (!core.getUiManager().openUi(player, "warp_create")) {
          player.sendMessage("§c传送点创建面板当前不可用。");
        }
      }
      case "WARP_CREATE_CONFIRM" -> {
        Optional<Island> island = islandManager.getIslandByOwner(player.getUniqueId());
        if (island.isEmpty()) {
          player.sendMessage("§c你还没有岛屿。");
          return;
        }
        Island sourceIsland = island.get();
        if (!sourceIsland.inProtectionRange(player.getLocation())) {
          player.sendMessage("§c你只能在自己岛屿保护范围内设置传送点。");
          return;
        }
        String warpName = params.get("name");
        if (warpName == null) {
          warpName = params.get("value");
        }
        if (warpName == null) {
          player.sendMessage("§c请输入传送点名称。");
          return;
        }
        warpName = warpName.trim();
        if (warpName.isEmpty()) {
          player.sendMessage("§c请输入传送点名称。");
          return;
        }
        if (warpName.length() > 32) {
          player.sendMessage("§c传送点名称不能超过 32 个字符。");
          return;
        }
        if (IslandWarpHelper.getWarps(sourceIsland).containsKey(warpName)) {
          player.sendMessage("§c传送点名称已存在。");
          return;
        }
        IslandWarpHelper.setWarp(sourceIsland, warpName, player.getLocation());
        islandManager.saveIsland(sourceIsland);
        player.sendMessage("§a传送点 §e" + warpName + " §a已设置在当前位置。");
        if (!openUiAndSend(player, "warp_board", "init", buildWarpPayload(sourceIsland))) {
          player.sendMessage("§c传送点面板当前不可用。");
        }
      }
      case "ISLAND_CREATE" -> {

        String blueprintId = params.getOrDefault("blueprintId", "default");
        islandManager.createIsland(player, core.getCoreConfig().getDefaultGameMode(), blueprintId);
      }
      case "ISLAND_HOME" -> {
        Optional<Island> opt = islandManager.getIslandByOwner(player.getUniqueId());
        opt.ifPresent(island -> {
          player.teleport(island.getCenter().clone().add(0, 1, 0));
          player.sendMessage("§a正在返回你的岛屿...");
        });
      }
      case "ISLAND_DELETE" -> {
        Optional<Island> opt = islandManager.getIslandByOwner(player.getUniqueId());
        opt.ifPresent(island -> {
          islandManager.deleteIsland(island);
          player.sendMessage("§a岛屿已删除。");
        });
      }
      case "ISLAND_INVITE" -> {
        String targetName = params.get("target");
        if (targetName != null) {
          Player target = Bukkit.getPlayer(targetName);
          if (target != null) {
            islandManager.getIslandByOwner(player.getUniqueId()).ifPresent(island -> islandManager.invitePlayer(island, target));
          }
        }
      }
      case "ISLAND_INVITE_ACCEPT" -> islandManager.acceptInvite(player);
      case "ISLAND_INVITE_REJECT" -> player.sendMessage("§c你已拒绝邀请。");
      case "ISLAND_UNLOCK" -> {
        Optional<Island> opt = islandManager.getIslandByOwner(player.getUniqueId());
        opt.ifPresent(island -> {
          island.setLocked(false);
          islandManager.saveIsland(island);
          player.sendMessage("§a岛屿已解锁。");
        });
      }
      case "SETTINGS_TOGGLE" -> {
        String flagName = params.get("flagName");
        boolean value = Boolean.parseBoolean(params.getOrDefault("value", "false"));
        if (flagName != null) {
          islandManager.getIslandByOwner(player.getUniqueId()).ifPresent(island -> {
            island.getFlags().put(flagName, value);
            islandManager.saveIsland(island);
          });
        }
      }
      default -> player.sendMessage("§c未知 UI 动作: " + action);
    }
  }
  private boolean isOpenAction(List<String> data) {
    return !data.isEmpty() && "open".equalsIgnoreCase(data.get(0));
  }

  private boolean openUiAndSend(Player player, String uiId, String packetType, Map<String, Object> payload) {
    if (core.getUiManager() == null || !core.getUiManager().openUi(player, uiId)) {
      return false;
    }
    return core.getUiManager().sendPacket(player, uiId, packetType, payload);
  }

  private Map<String, Object> buildBankPayload(BankAddon addon, Island island) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("balance", addon.getBalance(island.getUniqueId()));
    payload.put("interestRate", addon.getInterestRate());
    return payload;
  }

  private Map<String, Object> buildBorderPayload(BorderAddon addon, Island island) {
    Map<String, Object> payload = new LinkedHashMap<>();
    int currentSize = addon.getBorderSize(island);
    payload.put("currentSize", currentSize);
    payload.put("maxSize", resolveMaxBorderSize(currentSize));
    payload.put("showBorder", addon.isShowBorder());
    return payload;
  }

  private Optional<BankAddon> resolveBankAddon() {
    var addonManager = core.getLifecycleManager().getAddonLifecycleManager();
    if (addonManager == null) {
      return Optional.empty();
    }
    var addon = addonManager.getAddon("bank");
    if (addon instanceof BankAddon bankAddon) {
      return Optional.of(bankAddon);
    }
    return Optional.empty();
  }

  private Optional<BorderAddon> resolveBorderAddon() {
    var addonManager = core.getLifecycleManager().getAddonLifecycleManager();
    if (addonManager == null) {
      return Optional.empty();
    }
    var addon = addonManager.getAddon("border");
    if (addon instanceof BorderAddon borderAddon) {
      return Optional.of(borderAddon);
    }
    return Optional.empty();
  }

  private double resolveAmount(Map<String, String> params) {
    String raw = params.get("amount");
    if (raw == null || raw.isBlank()) {
      raw = params.get("value");
    }
    if (raw == null || raw.isBlank()) {
      return 1.0;
    }
    try {
      return Double.parseDouble(raw);
    } catch (NumberFormatException e) {
      return -1.0;
    }
  }

  private int resolveStep(Map<String, String> params) {
    String raw = params.get("amount");
    if (raw == null || raw.isBlank()) {
      raw = params.get("value");
    }
    if (raw == null || raw.isBlank()) {
      return 1;
    }
    try {
      return Integer.parseInt(raw);
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  private int resolveMaxBorderSize(int currentSize) {
    int maxSize = currentSize;
    if (core.getLifecycleManager() != null && core.getLifecycleManager().getCoreConfig() != null) {
      maxSize = Math.max(maxSize, core.getLifecycleManager().getCoreConfig().getRaw().getInt("general.max-range", 200));
    }
    return maxSize;
  }

  private Map<String, Object> buildMainPayload(Player player, Optional<Island> island) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("island", island.map(this::buildIslandSummary).orElse(null));
    payload.put("canCreate", island.isEmpty());
    payload.put("top3", buildTop3());
    return payload;
  }

  private Map<String, Object> buildRenamePayload(Island island) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("currentName", island.getName() != null ? island.getName() : "岛屿");
    return payload;
  }

  private Map<String, Object> buildTransferPayload(Island island) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("members", buildMemberList(island));
    return payload;
  }

  private Map<String, Object> buildWarpPayload(Island island) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("warps", buildWarpList(island));
    return payload;
  }

  private List<Map<String, Object>> buildMemberList(Island island) {
    List<Map<String, Object>> members = new ArrayList<>();
    for (Map.Entry<UUID, IslandMember> entry : island.getMembers().entrySet()) {
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("uuid", entry.getKey().toString());
      row.put("name", resolveMemberName(entry.getKey()));
      row.put("role", entry.getValue().getRole().name());
      members.add(row);
    }
    return members;
  }

  private List<Map<String, Object>> buildWarpList(Island island) {
    List<Map<String, Object>> warps = new ArrayList<>();
    for (Map.Entry<String, org.bukkit.Location> entry : IslandWarpHelper.getWarps(island).entrySet()) {
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("name", entry.getKey());
      row.put("displayName", entry.getKey());
      row.put("public", false);
      warps.add(row);
    }
    return warps;
  }

  private void refreshMainPanel(Player player, IslandManagerImpl islandManager) {
    if (core.getUiManager() == null) {
      return;
    }
    Optional<Island> island = islandManager.getIslandByOwner(player.getUniqueId());
    core.getUiManager().sendPacket(player, "aubade_main", "update", buildMainPayload(player, island));
  }

  private String resolveMemberName(UUID memberId) {
    OfflinePlayer owner = Bukkit.getOfflinePlayer(memberId);
    String name = owner.getName();
    return name != null ? name : memberId.toString();
  }

  private Map<String, Object> buildIslandSummary(Island island) {
    Map<String, Object> summary = new LinkedHashMap<>();
    summary.put("id", island.getUniqueId().toString());
    summary.put("name", island.getName() != null ? island.getName() : "未命名");
    summary.put("owner", resolveOwnerName(island));
    summary.put("level", island.getLevel());
    summary.put("range", island.getProtectionRange());
    summary.put("bankBalance", island.getBankBalance());
    summary.put("locked", island.isLocked());
    summary.put("members", island.getMembers().size());
    return summary;
  }

  private List<Map<String, Object>> buildTop3() {
    LevelAddon levelAddon = resolveLevelAddon();
    if (levelAddon == null) {
      return List.of();
    }

    List<Map<String, Object>> top3 = new ArrayList<>();
    List<LevelTopCache.TopEntry> entries = levelAddon.getTop(3);
    for (int i = 0; i < entries.size(); i++) {
      LevelTopCache.TopEntry entry = entries.get(i);
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("rank", i + 1);
      Optional<Island> island = core.getLifecycleManager().getIslandManager().getIslandById(entry.islandId());
      if (island.isPresent()) {
        row.put("name", island.get().getName() != null ? island.get().getName() : "未命名");
      } else {
        row.put("name", "未知岛屿");
      }
      row.put("score", entry.level());
      top3.add(row);
    }
    return top3;
  }

  private Map<String, Object> buildSettingsPayload(Island island) {
    Map<String, Object> settings = new LinkedHashMap<>();
    Map<String, Object> flags = new LinkedHashMap<>();
    settings.put("locked", island.isLocked());
    island.getFlags().forEach(flags::put);
    settings.put("flags", flags);
    return settings;
  }

  private String resolveOwnerName(Island island) {
    if (island.getOwner() == null) {
      return "未知";
    }
    OfflinePlayer owner = Bukkit.getOfflinePlayer(island.getOwner());
    return owner.getName() != null ? owner.getName() : "未知";
  }

  private LevelAddon resolveLevelAddon() {
    if (core.getLifecycleManager() == null || core.getLifecycleManager().getAddonLifecycleManager() == null) {
      return null;
    }
    var addon = core.getLifecycleManager().getAddonLifecycleManager().getAddon("level");
    if (addon instanceof LevelAddon levelAddon) {
      return levelAddon;
    }
    return null;
  }

}
