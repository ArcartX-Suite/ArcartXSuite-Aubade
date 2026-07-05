package xuanmo.aubade.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class AubadeUiRegistrationTest {

  @Test
  void coreUiResourcesStayInSyncWithRegistrationList() throws IOException {
    Path moduleDir = moduleDir();
    Set<String> resourceFiles = listUiFiles(moduleDir.resolve(Path.of("src", "main", "resources", "arcartx", "ui")));
    Set<String> addonFiles = discoverAddonUiFiles(moduleDir.resolve(Path.of("src", "main", "java", "xuanmo", "aubade", "core", "features")));
    Set<String> coreFiles = new HashSet<>(resourceFiles);
    coreFiles.removeAll(addonFiles);

    assertTrue(
        AubadeModule.CORE_UI_FILES.containsAll(coreFiles),
        () -> "Missing core UI registrations: " + missing(coreFiles, new HashSet<>(AubadeModule.CORE_UI_FILES)));
  }

  private static Path moduleDir() {
    Path userDir = Path.of(System.getProperty("user.dir"));
    Path direct = userDir.resolve(Path.of("src", "main", "resources", "arcartx", "ui"));
    if (Files.exists(direct)) {
      return userDir;
    }
    return userDir.resolve("aubade-core");
  }

  private static Set<String> listUiFiles(Path uiDir) throws IOException {
    try (var stream = Files.list(uiDir)) {
      return stream
          .filter(path -> path.getFileName().toString().endsWith(".yml"))
          .map(path -> path.getFileName().toString())
          .collect(Collectors.toCollection(HashSet::new));
    }
  }

  private static Set<String> discoverAddonUiFiles(Path featureRoot) throws IOException {
    Pattern pattern = Pattern.compile("registerUi\\(\"([^\"]+\\.yml)\"");
    Set<String> result = new HashSet<>();
    try (var stream = Files.walk(featureRoot)) {
      for (Path path : stream.filter(file -> file.toString().endsWith(".java")).toList()) {
        String content = Files.readString(path, StandardCharsets.UTF_8);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
          result.add(matcher.group(1));
        }
      }
    }
    return result;
  }

  private static Set<String> missing(Set<String> expected, Set<String> actual) {
    Set<String> missing = new HashSet<>(expected);
    missing.removeAll(actual);
    return missing;
  }
}
