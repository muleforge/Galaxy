package org.mule.galaxy.web.client.property;

import java.util.HashMap;
import java.util.Map;

import org.mule.galaxy.web.rpc.WProperty;

public class PropertyPanelFactory {
    
    public PropertyPanel createRenderer(WProperty property) {
        if ("lifecycleExtension".equals(property.getExtension())) {
            return new LifecyclePropertyPanel();
        } else if (property.isMultiValued()) {
            return new SimpleListPropertyPanel();
        } else {
            return new SimplePropertyPanel();
        }
    }
}
