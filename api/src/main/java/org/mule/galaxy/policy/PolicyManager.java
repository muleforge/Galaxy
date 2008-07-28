package org.mule.galaxy.policy;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.mule.galaxy.Item;
import org.mule.galaxy.RegistryException;
import org.mule.galaxy.lifecycle.Lifecycle;
import org.mule.galaxy.lifecycle.Phase;

public interface PolicyManager {
    
    Map<Item, List<ApprovalMessage>> approve(Item item) throws PolicyException;
    
    Collection<Policy> getPolicies();
    
    Collection<Policy> getActivePolicies(Item item);

    Collection<Policy> getActivePolicies(Item item, boolean includeInherited);

    Collection<Policy> getActivePolicies(Phase p);

    Collection<Policy> getActivePolicies(Lifecycle p);

    Collection<Policy> getActivePolicies(Item i, Phase p, boolean includeInherited);

    Collection<Policy> getActivePolicies(Item i, Lifecycle p, boolean includeInherited);
    
    void setActivePolicies(Item i, Collection<Phase> phases, Policy... policies)
        throws PolicyException, RegistryException;
    
    void setActivePolicies(Collection<Phase> phases, Policy... policies)
        throws PolicyException, RegistryException;
    
    void setActivePolicies(Item i, Lifecycle lifecycle, Policy... policies) 
        throws PolicyException, RegistryException;

    void setActivePolicies(Lifecycle lifecycle, Policy... policies) 
        throws PolicyException, RegistryException;
    
    
    Policy getPolicy(String id);

    void addPolicy(Policy policy);
}
