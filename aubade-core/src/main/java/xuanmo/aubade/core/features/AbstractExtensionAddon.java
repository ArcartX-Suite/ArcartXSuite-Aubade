package xuanmo.aubade.core.features;

import xuanmo.arcartxsuite.api.aubade.addon.AddonDescriptor;
import xuanmo.arcartxsuite.api.aubade.addon.ExtensionAddon;
import xuanmo.aubade.core.AubadeCore;

public abstract class AbstractExtensionAddon extends AbstractFeatureAddon implements ExtensionAddon {

  public AbstractExtensionAddon(AubadeCore plugin, AddonDescriptor descriptor) {
    super(plugin, descriptor);
  }

  @Override
  public abstract String getExtensionId();

  @Override
  public String getFeatureId() {
    return getExtensionId();
  }
}

