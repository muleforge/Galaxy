package org.mule.galaxy.impl.lifecycle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mule.galaxy.Item;
import org.mule.galaxy.extension.Extension;
import org.mule.galaxy.lifecycle.LifecycleManager;
import org.mule.galaxy.lifecycle.Phase;
import org.mule.galaxy.policy.PolicyException;
import org.mule.galaxy.type.PropertyDescriptor;

public class LifecycleExtension implements Extension {
    private String id;
    private LifecycleManager lifecycleManager;
    
    public Object getExternalValue(Item entry, PropertyDescriptor pd, Object storedValue) {
        if (storedValue == null) {
            return null;
        }
        
        List ids = (List) storedValue;
        return lifecycleManager.getPhaseById((String) ids.get(1));
    }

    public Object getInternalValue(Item entry, PropertyDescriptor pd, Object value)
            throws PolicyException {
        Phase phase = (Phase) value;
        
        return Arrays.asList(phase.getLifecycle().getId(), phase.getId());
    }

    public boolean isMultivalueSupported() {
        return false;
    }

    public LifecycleManager getLifecycleManager() {
        return lifecycleManager;
    }

    public void setLifecycleManager(LifecycleManager lifecycleManager) {
        this.lifecycleManager = lifecycleManager;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
}
