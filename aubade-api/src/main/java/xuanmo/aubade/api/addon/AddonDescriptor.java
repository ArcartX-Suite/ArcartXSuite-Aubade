package xuanmo.aubade.api.addon;

import java.util.List;

/**
 * 组件描述符，定义组件的元数据。
 */
public record AddonDescriptor(
    String id,
    String name,
    String version,
    String mainClass,
    List<String> depends,
    List<String> softDepends,
    List<String> pluginDepends
) {

  public static Builder builder(String id) {
    return new Builder(id);
  }

  public static class Builder {
    private final String id;
    private String name = "";
    private String version = "1.0.0";
    private String mainClass = "";
    private List<String> depends = List.of();
    private List<String> softDepends = List.of();
    private List<String> pluginDepends = List.of();

    private Builder(String id) {
      this.id = id;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder version(String version) {
      this.version = version;
      return this;
    }

    public Builder mainClass(String mainClass) {
      this.mainClass = mainClass;
      return this;
    }

    public Builder depends(List<String> depends) {
      this.depends = depends;
      return this;
    }

    public Builder softDepends(List<String> softDepends) {
      this.softDepends = softDepends;
      return this;
    }

    public Builder pluginDepends(List<String> pluginDepends) {
      this.pluginDepends = pluginDepends;
      return this;
    }

    public AddonDescriptor build() {
      return new AddonDescriptor(id, name, version, mainClass, depends, softDepends, pluginDepends);
    }
  }
}
