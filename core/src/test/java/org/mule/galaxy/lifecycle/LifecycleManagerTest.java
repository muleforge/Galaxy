package org.mule.galaxy.lifecycle;

import java.util.Collection;
import java.util.Set;

import org.mule.galaxy.DuplicateItemException;
import org.mule.galaxy.Item;
import org.mule.galaxy.NotFoundException;
import org.mule.galaxy.Registry;
import org.mule.galaxy.event.DefaultEvents;
import org.mule.galaxy.event.LifecycleTransitionEvent;
import org.mule.galaxy.event.annotation.BindToEvent;
import org.mule.galaxy.event.annotation.OnEvent;
import org.mule.galaxy.policy.PolicyException;
import org.mule.galaxy.test.AbstractGalaxyTest;

public class LifecycleManagerTest extends AbstractGalaxyTest {

    protected LifecycleManager lifecycleManager;
    
    public void testLifecycleInitialization() throws Exception {
        Collection<Lifecycle> lifecycles = lifecycleManager.getLifecycles();
        
        assertEquals(1, lifecycles.size());
        
        Lifecycle l = lifecycles.iterator().next();
        
        assertEquals("Default", l.getName());
        
        Phase init = l.getInitialPhase();
        assertNotNull(init);
        
        assertEquals("Created", init.getName());
        
        Set<Phase> phases = init.getNextPhases();
        assertEquals(1, phases.size());
        
        Phase p = phases.iterator().next();
        assertEquals("Developed", p.getName());
    }
    
    public void testLifecyclesAndArtifact() throws Exception {
        Collection<Lifecycle> lifecycles = lifecycleManager.getLifecycles();
        assertEquals(1, lifecycles.size());
        
        Lifecycle l = lifecycles.iterator().next();
        Phase created = l.getInitialPhase();
        Phase dev = created.getNextPhases().iterator().next();
        
        Item version = importHelloWsdl();
        assertEquals(created, getPhase(version));
        
        assertFalse(lifecycleManager.isTransitionAllowed(version, Registry.PRIMARY_LIFECYCLE, created));
        assertTrue(lifecycleManager.isTransitionAllowed(version, Registry.PRIMARY_LIFECYCLE, dev));
        
        Phase current = getPhase(version);
        assertEquals(created.getName(), current.getName());
        
        EventCounter counter = new EventCounter();
        eventManager.addListener(counter);
        
        version.setProperty(Registry.PRIMARY_LIFECYCLE, dev);
        registry.save(version);
        
        current = getPhase(version);
        assertEquals(dev.getName(), current.getName());
        assertEquals(1, counter.getCounter());
        
        // we aren't transitioning, make sure that works though
        version.setProperty(Registry.PRIMARY_LIFECYCLE, dev);
        registry.save(version);
        
        Phase next = l.getPhase("Tested");
        version.setProperty(Registry.PRIMARY_LIFECYCLE, next);
        registry.save(version);
        
        next = l.getPhase("Staged");
        version.setProperty(Registry.PRIMARY_LIFECYCLE, next);
        registry.save(version);
        
        next = l.getPhase("Production");
        version.setProperty(Registry.PRIMARY_LIFECYCLE, next);
        registry.save(version);
        
        next = l.getPhase("Retired");
        version.setProperty(Registry.PRIMARY_LIFECYCLE, next);
        registry.save(version);
        
        // try going back
        next = l.getPhase("Production");
        version.setProperty(Registry.PRIMARY_LIFECYCLE, next);
        registry.save(version);
        
        // Try an invalid phase by setting the internal value
        try {
            version.setProperty(Registry.PRIMARY_LIFECYCLE, dev);
            registry.save(version);
            
            fail("Expected Transition Exception");
        } catch (PolicyException e) {
            // expected
        }
    }

    public void testInvalidSaves() throws Exception {    
        Lifecycle l = new Lifecycle();
        
        try {
            lifecycleManager.save(l);
            fail("Must have lifecycle name");
        } catch (RuntimeException e) {
        }
        
        l.setName("test");
        
        try {
            lifecycleManager.save(l);
            fail("Must have initial phase");
        } catch (RuntimeException e) {
        }
        
        Phase p = new Phase(l);
        p.setName("test");
        
        l.setInitialPhase(p);
        l.addPhase(p);
        
        lifecycleManager.save(l);
    }

    public void testWeirdNames() throws Exception {    
        Lifecycle l = new Lifecycle();
        l.setName("Foo Bar");
        
        Phase p = new Phase(l);
        p.setName("test");
        
        l.setInitialPhase(p);
        l.addPhase(p);
        
        lifecycleManager.save(l);
        
        l = lifecycleManager.getLifecycleById(l.getId());
        assertEquals("Foo Bar", l.getName());

        l.setName("asdf!@#$%^&*()");
        lifecycleManager.save(l);
        
        l = lifecycleManager.getLifecycle(l.getName());
        assertEquals("asdf!@#$%^&*()", l.getName());
        
    }
    
    public void testSave() throws Exception {    
        Lifecycle l = lifecycleManager.getDefaultLifecycle();
        l.setName("test");
        assertNotNull(l.getId());
        
        lifecycleManager.save(l);
        assertEquals("test", l.getName());

        Collection<Lifecycle> lcs = lifecycleManager.getLifecycles();
        assertEquals(1, lcs.size());
        
        Lifecycle l2 = lifecycleManager.getLifecycle("test");
        assertNotNull(l2);
        
        Item a = importHelloWsdl();
        
        l.setName("test2");
        lifecycleManager.save(l);
        
        a = registry.getItemById(a.getId());
        assertEquals(l, getPhase(a).getLifecycle());
        
        try {
            lifecycleManager.delete(l.getId(), null);
            fail("Expected not found exception");
        } catch (NotFoundException e) {
            
        }
        
        Lifecycle newLc = new Lifecycle();
        newLc.setName("another");
        
        Phase phase = new Phase(newLc);
        phase.setName("p1");
        newLc.setInitialPhase(phase);
        
        newLc.addPhase(phase);
        
        lifecycleManager.save(newLc);
        
        lcs = lifecycleManager.getLifecycles();
        assertEquals(2, lcs.size());
        
        lifecycleManager.delete(l.getId(), newLc.getId());
        
        // Workspace wkspc = (Workspace) registry.getItemByPath("Default Workspace");
        // Artifact artifact = registry.getArtifact(wkspc, "hello_world.wsdl");
        // Doesn't work because the session state isn't persisted yet
//        assertEquals(newLc.getId(), getPhase(artifact.getDefaultOrLastVersion()).getLifecycle().getId());
        
        // try saving a lifecycle with a duplicate name
        newLc.setId(null);
        
        try {
            lifecycleManager.save(newLc);
            fail("Duplicate lifecycles should not be allowed.");
        } catch (DuplicateItemException e) {
            // this is expected
        }
        
        
    }
    
    public void testWorkspaceInteraction() throws Exception {
        Lifecycle newLc = new Lifecycle();
        newLc.setName("another");
        
        Phase phase = new Phase(newLc);
        phase.setName("p1");
        newLc.setInitialPhase(phase);
        newLc.addPhase(phase);
        
        lifecycleManager.save(newLc);
        
        assertNotNull(newLc.getId());
        
        Item w = registry.getItems().iterator().next();
        w.setDefaultLifecycle(newLc);
        registry.save(w);
        
        Item w2 = (Item) registry.getItemByPath(w.getPath());
        assertEquals(newLc.getName(), w2.getDefaultLifecycle().getName());
    }
    
    public void testPhaseChanges() throws Exception {
        Lifecycle l = lifecycleManager.getDefaultLifecycle();
        
        Item artifact = importHelloWsdl();
        
        Phase p1 = l.getInitialPhase();
        p1.setName("new name");
        
        lifecycleManager.save(l);
        
        artifact = registry.getItemById(artifact.getId());
        assertEquals(p1, getPhase(artifact));
        
        Phase p2 = p1.getNextPhases().iterator().next();
        
        l.removePhase(p2);
        lifecycleManager.save(l);
        
        Lifecycle l2 = lifecycleManager.getDefaultLifecycle();
        assertEquals(l.getPhases().size(), l2.getPhases().size());
        
        // todo: invalid nextPhases
    }
    
    @BindToEvent(DefaultEvents.LIFECYCLE_TRANSITION)
    public static final class EventCounter {
        private int counter;
        
        @OnEvent
        public void onEvent(LifecycleTransitionEvent e) {
            counter++;
        }

        public int getCounter() {
            return counter;
        }
    }
}
