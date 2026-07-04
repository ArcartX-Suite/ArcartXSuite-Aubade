package xuanmo.aubade.core.features.blueprint;

import java.io.File;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.aubade.core.AubadeCore;
import xuanmo.aubade.core.blueprint.BlueprintRegistry;
import xuanmo.aubade.core.features.AbstractExtensionAddon;

/**
 * 自定义蓝图生成器扩展组件。
 * 管理蓝图注册表，支持从 JSON/YAML 加载自定义岛屿蓝图。
 */
public class BlueprintGeneratorAddon extends AbstractExtensionAddon {

  private BlueprintRegistry registry;

  public BlueprintGeneratorAddon(AubadeCore core) {
    super(core, AddonDescriptor.builder("blueprint_generator")
        .name("蓝图生成器")
        .version("1.0.0")
        .mainClass(BlueprintGeneratorAddon.class.getName())
        .build());
  }

  @Override
  public String getExtensionId() {
    return "blueprint_generator";
  }

  @Override
  public String getFriendlyName() {
    return "蓝图生成器";
  }

  @Override
  public void onEnable() {
    super.onEnable();
    this.registry = new BlueprintRegistry(core.getLogger(), core.getDataFolder());
    this.registry.loadAll();

    // 释放默认示例蓝图
    releaseDefaultBlueprints();

    core.getLogger().info("[BlueprintGenerator] 蓝图生成器扩展已启用，共 " + registry.getAllBlueprints().size() + " 个蓝图。");
  }

  @Override
  public void onDisable() {
    super.onDisable();
    core.getLogger().info("[BlueprintGenerator] 蓝图生成器扩展已禁用。");
  }

  public BlueprintRegistry getRegistry() {
    return registry;
  }

  private void releaseDefaultBlueprints() {
    File dir = registry.getBlueprintsDir();
    File example = new File(dir, "example_tree.yml");
    if (example.exists()) {
      return;
    }
    try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(new java.io.FileOutputStream(example), java.nio.charset.StandardCharsets.UTF_8)) {
      writer.write("""
          # Aubade 蓝图示例 — 树岛
          id: tree_island
          name: "树岛"
          description: "一棵大树和小平台的进阶开局"
          icon: OAK_SAPLING
          blocks:
            # 5x5 草地方平台
            - { x: -2, y: 0, z: -2, material: GRASS_BLOCK }
            - { x: -2, y: 0, z: -1, material: GRASS_BLOCK }
            - { x: -2, y: 0, z:  0, material: GRASS_BLOCK }
            - { x: -2, y: 0, z:  1, material: GRASS_BLOCK }
            - { x: -2, y: 0, z:  2, material: GRASS_BLOCK }
            - { x: -1, y: 0, z: -2, material: GRASS_BLOCK }
            - { x: -1, y: 0, z: -1, material: GRASS_BLOCK }
            - { x: -1, y: 0, z:  0, material: GRASS_BLOCK }
            - { x: -1, y: 0, z:  1, material: GRASS_BLOCK }
            - { x: -1, y: 0, z:  2, material: GRASS_BLOCK }
            - { x:  0, y: 0, z: -2, material: GRASS_BLOCK }
            - { x:  0, y: 0, z: -1, material: GRASS_BLOCK }
            - { x:  0, y: 0, z:  0, material: GRASS_BLOCK }
            - { x:  0, y: 0, z:  1, material: GRASS_BLOCK }
            - { x:  0, y: 0, z:  2, material: GRASS_BLOCK }
            - { x:  1, y: 0, z: -2, material: GRASS_BLOCK }
            - { x:  1, y: 0, z: -1, material: GRASS_BLOCK }
            - { x:  1, y: 0, z:  0, material: GRASS_BLOCK }
            - { x:  1, y: 0, z:  1, material: GRASS_BLOCK }
            - { x:  1, y: 0, z:  2, material: GRASS_BLOCK }
            - { x:  2, y: 0, z: -2, material: GRASS_BLOCK }
            - { x:  2, y: 0, z: -1, material: GRASS_BLOCK }
            - { x:  2, y: 0, z:  0, material: GRASS_BLOCK }
            - { x:  2, y: 0, z:  1, material: GRASS_BLOCK }
            - { x:  2, y: 0, z:  2, material: GRASS_BLOCK }
            # 树干
            - { x: 0, y: 1, z: 0, material: OAK_LOG }
            - { x: 0, y: 2, z: 0, material: OAK_LOG }
            - { x: 0, y: 3, z: 0, material: OAK_LOG }
            - { x: 0, y: 4, z: 0, material: OAK_LOG }
            - { x: 0, y: 5, z: 0, material: OAK_LOG }
            # 树叶冠
            - { x:  0, y: 6, z:  0, material: OAK_LEAVES }
            - { x:  1, y: 5, z:  0, material: OAK_LEAVES }
            - { x: -1, y: 5, z:  0, material: OAK_LEAVES }
            - { x:  0, y: 5, z:  1, material: OAK_LEAVES }
            - { x:  0, y: 5, z: -1, material: OAK_LEAVES }
            - { x:  1, y: 4, z:  1, material: OAK_LEAVES }
            - { x: -1, y: 4, z: -1, material: OAK_LEAVES }
            - { x:  1, y: 4, z: -1, material: OAK_LEAVES }
            - { x: -1, y: 4, z:  1, material: OAK_LEAVES }
            # 箱子
            - { x: 1, y: 1, z: 1, material: CHEST }
            # 工作台
            - { x: -1, y: 1, z: 1, material: CRAFTING_TABLE }
          """);
    } catch (Exception e) {
      core.getLogger().warning("[BlueprintGenerator] 释放示例蓝图失败: " + e.getMessage());
    }
  }
}

