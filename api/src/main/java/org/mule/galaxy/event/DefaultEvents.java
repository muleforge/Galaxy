package org.mule.galaxy.event;

/**
 * A convenience Constants class for every standard event in Galaxy.
 */
public class DefaultEvents {

    // workspace
    public static final String WORKSPACE_CREATED = "WorkspaceCreated";
    public static final String WORKSPACE_DELETED = "WorkspaceDeleted";

    // property
    public static final String PROPERTY_CHANGED = "PropertyChanged";

    // artifact
    public static final String ARTIFACT_VERSION_CREATED = "ArtifactVersionCreated";
    public static final String ARTIFACT_VERSION_DELETED = "ArtifactVersionDeleted";

    // artifact version
    public static final String ARTIFACT_CREATED = "ArtifactCreated";
    public static final String ARTIFACT_DELETED = "ArtifactDeleted";
    public static final String ARTIFACT_MOVED = "ArtifactMoved";

    // misc
    public static final String LIFECYCLE_TRANSITION = "LifecycleTransition";
}
