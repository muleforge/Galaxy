package org.mule.galaxy.policy;

public class PolicyInfo {
    private Policy artifactPolicy;
    private Object appliesTo;
    
    
    public PolicyInfo(Policy artifactPolicy, Object appliesTo) {
        super();
        this.artifactPolicy = artifactPolicy;
        this.appliesTo = appliesTo;
    }
    
    public Policy getArtifactPolicy() {
        return artifactPolicy;
    }
    public Object getAppliesTo() {
        return appliesTo;
    }
    
    
}
