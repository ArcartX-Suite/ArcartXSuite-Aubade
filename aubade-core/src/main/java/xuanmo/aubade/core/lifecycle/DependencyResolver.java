package xuanmo.aubade.core.lifecycle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.arcartxsuite.api.aubade.addon.SkyAddon;

/**
 * 组件依赖拓扑排序器。
 * 基于 Kahn 算法实现，检测循环依赖。
 */
public final class DependencyResolver {

  private DependencyResolver() {
  }

  /**
   * 按依赖关系对组件进行拓扑排序。
   *
   * @param addons 组件列表
   * @return 排序后的组件列表（依赖在前，被依赖在后）
   */
  public static List<SkyAddon> resolve(List<SkyAddon> addons) {
    Map<String, SkyAddon> idMap = new HashMap<>();
    Map<String, Integer> inDegree = new HashMap<>();
    Map<String, List<String>> adj = new HashMap<>();

    // 初始化
    for (SkyAddon addon : addons) {
      String id = addon.descriptor().id();
      idMap.put(id, addon);
      inDegree.put(id, 0);
      adj.put(id, new ArrayList<>());
    }

    // 建图
    for (SkyAddon addon : addons) {
      AddonDescriptor desc = addon.descriptor();
      String id = desc.id();
      for (String dep : desc.depends()) {
        if (idMap.containsKey(dep)) {
          adj.get(dep).add(id);
          inDegree.put(id, inDegree.get(id) + 1);
        }
      }
    }

    // Kahn 算法
    List<SkyAddon> result = new ArrayList<>();
    Set<String> queue = new HashSet<>();
    for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
      if (entry.getValue() == 0) {
        queue.add(entry.getKey());
      }
    }

    while (!queue.isEmpty()) {
      String id = queue.iterator().next();
      queue.remove(id);
      result.add(idMap.get(id));

      for (String next : adj.get(id)) {
        int deg = inDegree.get(next) - 1;
        inDegree.put(next, deg);
        if (deg == 0) {
          queue.add(next);
        }
      }
    }

    if (result.size() != addons.size()) {
      throw new IllegalStateException("[依赖解析] 检测到组件间存在循环依赖，无法完成拓扑排序。");
    }

    return result;
  }
}

