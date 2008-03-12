package org.mule.galaxy.impl;


import java.util.Collection;
import java.util.Set;

import org.mule.galaxy.ArtifactResult;
import org.mule.galaxy.ArtifactVersion;
import org.mule.galaxy.Dependency;
import org.mule.galaxy.Workspace;
import org.mule.galaxy.test.AbstractGalaxyTest;

public class DependencyTest extends AbstractGalaxyTest {
    
    public void testWsdlDependencies() throws Exception {

        Collection<Workspace> workspaces = registry.getWorkspaces();
        assertEquals(1, workspaces.size());
        Workspace workspace = workspaces.iterator().next();
        
        ArtifactResult schema = registry.createArtifact(workspace, 
                                                        "application/xml", 
                                                        "hello.xsd", 
                                                        "0.1", 
                                                        getResourceAsStream("/wsdl/imports/hello.xsd"), 
                                                        getAdmin());
        
        Set<Dependency> deps = schema.getArtifactVersion().getDependencies();
        assertEquals(0, deps.size());
        
        ArtifactResult portType = registry.createArtifact(workspace, 
                                                          "application/wsdl+xml", 
                                                          "hello-portType.wsdl", 
                                                          "0.1", 
                                                          getResourceAsStream("/wsdl/imports/hello-portType.wsdl"), 
                                                          getAdmin());
        deps = portType.getArtifactVersion().getDependencies();
        assertEquals(1, deps.size());
        
        Dependency dep = deps.iterator().next();
        assertEquals(schema.getArtifact().getId(), dep.getArtifact().getId());
        assertFalse(dep.isUserSpecified());
        
        deps = registry.getDependedOnBy(schema.getArtifact());
        assertEquals(1, deps.size());
        dep = deps.iterator().next();
        assertEquals(portType.getArtifact().getId(), dep.getArtifact().getId());
        assertFalse(dep.isUserSpecified());
        
        ArtifactResult svcWsdl = registry.createArtifact(workspace, 
                                                         "application/wsdl+xml", 
                                                         "hello.wsdl", 
                                                         "0.1", 
                                                         getResourceAsStream("/wsdl/imports/hello.wsdl"), 
                                                         getAdmin());
        deps = svcWsdl.getArtifactVersion().getDependencies();
        assertEquals(1, deps.size());
        
        dep = deps.iterator().next();
        assertEquals(portType.getArtifact().getId(), dep.getArtifact().getId());
        assertFalse(dep.isUserSpecified());
        
        // yeah, this doesn't make sense dependency-wise, but we're just seeing if user specified dependencies work
        ArtifactVersion schemaV = schema.getArtifactVersion();
        registry.addDependencies(schemaV, svcWsdl.getArtifact());
        
        deps = schemaV.getDependencies();
        assertEquals(1, deps.size());
        dep = deps.iterator().next();
        assertEquals(svcWsdl.getArtifact().getId(), dep.getArtifact().getId());
        assertTrue(dep.isUserSpecified());
        
        registry.removeDependencies(schemaV, svcWsdl.getArtifact());
        deps = schemaV.getDependencies();
        assertEquals(0, deps.size());
    }
    
    public void testSchemaDependencies() throws Exception {

        Collection<Workspace> workspaces = registry.getWorkspaces();
        assertEquals(1, workspaces.size());
        Workspace workspace = workspaces.iterator().next();
        
        ArtifactResult schema = registry.createArtifact(workspace, 
                                                        "application/xml", 
                                                        "hello.xsd", 
                                                        "0.1", 
                                                        getResourceAsStream("/schema/hello.xsd"), 
                                                        getAdmin());
        
        Set<Dependency> deps = schema.getArtifactVersion().getDependencies();
        assertEquals(0, deps.size());
        
        ArtifactResult schema2 = registry.createArtifact(workspace, 
                                                         "application/xml", 
                                                         "hello-import.xsd", 
                                                         "0.1", 
                                                         getResourceAsStream("/schema/hello-import.xsd"), 
                                                         getAdmin());
         
        deps = schema2.getArtifactVersion().getDependencies();
        assertEquals(1, deps.size());
        Dependency dep = deps.iterator().next();
        assertEquals(schema.getArtifact().getId(), dep.getArtifact().getId());
        assertFalse(dep.isUserSpecified());
    }
    
    public void testMissingWsdlDependencies() throws Exception {

        Collection<Workspace> workspaces = registry.getWorkspaces();
        assertEquals(1, workspaces.size());
        Workspace workspace = workspaces.iterator().next();

        ArtifactResult svcWsdl = registry.createArtifact(workspace, 
                                                         "application/wsdl+xml", 
                                                         "hello.wsdl", 
                                                         "0.1", 
                                                         getResourceAsStream("/wsdl/imports/hello-missing.wsdl"), 
                                                         getAdmin());
        Set<Dependency> deps = svcWsdl.getArtifactVersion().getDependencies();
        assertEquals(0, deps.size());
    }

}
