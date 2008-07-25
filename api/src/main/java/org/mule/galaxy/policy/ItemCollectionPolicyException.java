package org.mule.galaxy.policy;

import java.util.List;
import java.util.Map;

import org.mule.galaxy.EntryVersion;
import org.mule.galaxy.Item;

public class ItemCollectionPolicyException extends Exception {
    private Map<Item,List<ApprovalMessage>> policyFailures;

    public ItemCollectionPolicyException(Map<Item, List<ApprovalMessage>> policyFailures) {
        super();
        this.policyFailures = policyFailures;
    }

    public Map<Item, List<ApprovalMessage>> getPolicyFailures() {
        return policyFailures;
    }
}
