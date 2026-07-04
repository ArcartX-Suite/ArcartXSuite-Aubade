package xuanmo.aubade.core.blueprint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BlueprintParserTest {

  @TempDir
  Path tempDir;

  @Test
  void parsesJsonBlueprintFile() throws Exception {
    Path file = tempDir.resolve("custom.json");
    Files.writeString(file, """
        {
          "id": "custom",
          "name": "自定义蓝图",
          "description": "测试蓝图",
          "icon": "DIAMOND_BLOCK",
          "blocks": [
            {"x": 0, "y": 0, "z": 0, "material": "STONE"},
            {"x": 1, "y": 0, "z": 0, "material": "DIRT"}
          ]
        }
        """, StandardCharsets.UTF_8);

    Blueprint blueprint = BlueprintParser.parse(file.toFile());
    assertEquals("custom", blueprint.getId());
    assertEquals("自定义蓝图", blueprint.getName());
    assertEquals("测试蓝图", blueprint.getDescription());
    assertEquals(Material.DIAMOND_BLOCK, blueprint.getIcon());
    assertEquals(2, blueprint.getBlocks().size());
    assertEquals(Material.STONE, blueprint.getBlocks().get(0).material());
  }

  @Test
  void createsExpectedDefaultBlueprint() {
    Blueprint blueprint = BlueprintParser.createDefaultBlueprint();
    assertEquals("default", blueprint.getId());
    assertEquals("经典空岛", blueprint.getName());
    assertNotNull(blueprint.getIcon());
    assertEquals(18, blueprint.getBlocks().size());
  }
}
