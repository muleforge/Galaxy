package org.mule.galaxy;

import org.mule.galaxy.type.PropertyDescriptor;

public interface PropertyInfo {
    String getName();

    String getDescription();
    
    Object getValue();
    
    PropertyDescriptor getPropertyDescriptor();
    
    boolean isLocked();
    
    boolean isVisible();
}
