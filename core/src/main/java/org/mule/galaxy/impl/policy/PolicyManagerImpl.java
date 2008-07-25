package org.mule.galaxy.impl.policy;

import static org.mule.galaxy.impl.jcr.JcrUtil.getOrCreate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.mule.galaxy.ArtifactVersion;
import org.mule.galaxy.Entry;
import org.mule.galaxy.EntryVersion;
import org.mule.galaxy.Item;
import org.mule.galaxy.PropertyInfo;
import org.mule.galaxy.Registry;
import org.mule.galaxy.RegistryException;
import org.mule.galaxy.Workspace;
import org.mule.galaxy.impl.jcr.JcrUtil;
import org.mule.galaxy.impl.lifecycle.LifecycleExtension;
import org.mule.galaxy.lifecycle.Lifecycle;
import org.mule.galaxy.lifecycle.Phase;
import org.mule.galaxy.policy.ApprovalMessage;
import org.mule.galaxy.policy.ItemCollectionPolicyException;
import org.mule.galaxy.policy.Policy;
import org.mule.galaxy.policy.PolicyManager;
import org.mule.galaxy.query.OpRestriction;
import org.mule.galaxy.query.QueryException;
import org.mule.galaxy.query.SearchResults;
import org.mule.galaxy.type.PropertyDescriptor;
import org.mule.galaxy.type.TypeManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springmodules.jcr.JcrCallback;
import org.springmodules.jcr.JcrTemplate;

public class PolicyManagerImpl implements PolicyManager, ApplicationContextAware {
    private Map<String, Policy> policies = new HashMap<String, Policy>();
    private Registry registry;
    private JcrTemplate jcrTemplate;
    private String lifecyclesNodeId;
    private String itemsLifecyclesNodeId;
    private String itemsPhasesNodeId;
    private String phasesNodeId;
    private ApplicationContext applicationContext;
    private TypeManager typeManager;
    
    public void initilaize() throws Exception{
        Session session = jcrTemplate.getSessionFactory().getSession();
        Node root = session.getRootNode();
        
        Node activations = getOrCreate(root, "policyActivations");
        lifecyclesNodeId = getOrCreate(activations, "lifecycles").getUUID();
        phasesNodeId = getOrCreate(root, "phases").getUUID();

        Node items = getOrCreate(activations, "items");
        itemsLifecyclesNodeId = getOrCreate(items, "lifecycles").getUUID();
        itemsPhasesNodeId = getOrCreate(items, "phases").getUUID();
        
        session.save();
    }
    
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        String[] names = ctx.getBeanNamesForType(Policy.class);
        for (String s : names) {
            Policy p = (Policy) ctx.getBean(s);
            addPolicy(p);
        }
        
        this.applicationContext = ctx;
    }
    
    public Registry getRegistry() {
        if (registry == null) {
            registry = (Registry) applicationContext.getBean("registry");
        }
        return registry;
    }

    public void addPolicy(Policy p) {
        policies.put(p.getId(), p);
    }

    public List<ApprovalMessage> approve(Item item) {
        Collection<Policy> policies = getActivePolicies(item);
        List<ApprovalMessage> approvals = new ArrayList<ApprovalMessage>();
        for (Policy p : policies) {
            Collection<ApprovalMessage> list = p.isApproved((ArtifactVersion) item);
            if (list != null) {
                approvals.addAll(list);
            }
        }
        return approvals;
    }


    public Policy getPolicy(String id) {
        return policies.get(id);
    }

    public Collection<Policy> getPolicies() {
        return policies.values();
    }
    
    public void setActivePolicies(Item item, Collection<Phase> phases, Policy... policies) 
        throws ItemCollectionPolicyException {
        
        Map<Item, List<ApprovalMessage>> failures = new HashMap<Item, List<ApprovalMessage>>();
        
        approveItem(item, failures, phases, policies);
        
        if (failures.size() > 0) {
            throw new ItemCollectionPolicyException(failures);
        }
        
        activatePolicy(itemsPhasesNodeId, phases, policies, item.getId());
    }

    private void approveItem(Item item, 
                             Map<Item, List<ApprovalMessage>> failures, 
                             Collection<Phase> phases, 
                             Policy... policies) 
        throws ItemCollectionPolicyException {
        
        for (Iterator<PropertyInfo> itr = item.getProperties(); itr.hasNext();) {
            PropertyInfo pi = (PropertyInfo) itr.next();
            if (pi.getPropertyDescriptor().getExtension() instanceof LifecycleExtension) {
                Phase p = (Phase) pi.getValue();

                if (phases.contains(p)) {
                    List<ApprovalMessage> messages = approve(item, policies);
                    
                    if (messages != null && messages.size() > 0) {
                        failures.put(item, messages);
                    }
                }
            }
        }
        
        if (item instanceof Workspace) {
            for (Item i : ((Workspace) item).getItems()) {
                approveItem(i, failures, phases, policies);
            }
        } else if (item instanceof Entry) {
            for (EntryVersion v : ((Entry) item).getVersions()) {
                approveItem((EntryVersion)v, failures, phases, policies);
            }
        }
    }

    private void approveItem(Item item, 
                             Map<Item, List<ApprovalMessage>> failures, 
                             Lifecycle lifecycle, 
                             Policy... policies) 
        throws ItemCollectionPolicyException {
        
        for (Iterator<PropertyInfo> itr = item.getProperties(); itr.hasNext();) {
            PropertyInfo pi = (PropertyInfo) itr.next();
            if (pi.getPropertyDescriptor().getExtension() instanceof LifecycleExtension) {
                Phase p = (Phase) pi.getValue();

                if (lifecycle.equals(p.getLifecycle())) {
                    List<ApprovalMessage> messages = approve(item, policies);
                    
                    if (messages != null && messages.size() > 0) {
                        failures.put(item, messages);
                    }
                }
            }
        }
        
        if (item instanceof Workspace) {
            for (Item i : ((Workspace) item).getItems()) {
                approveItem(i, failures, lifecycle, policies);
            }
        } else if (item instanceof Entry) {
            for (EntryVersion v : ((Entry) item).getVersions()) {
                approveItem((EntryVersion)v, failures, lifecycle, policies);
            }
        }
    }

    private List<ApprovalMessage> approve(Item item, Policy... policies) {
        List<ApprovalMessage> messages = null;
        for (Policy p : policies) {
            if (!p.applies(item)) continue;
            
            Collection<ApprovalMessage> approved = p.isApproved(item);
            boolean failed = false;
            if (approved != null) {
                for (ApprovalMessage m : approved) {
                    if (!m.isWarning()) {
                        failed = true;
                        break;
                    }
                }
            }
            
            if (failed) {
                if (messages == null) {
                    messages = new ArrayList<ApprovalMessage>();
                }
                messages.addAll(approved);
            }
        }
        return messages;
    }

    public void setActivePolicies(Collection<Phase> phases, Policy... policies) 
        throws ItemCollectionPolicyException, RegistryException {
        // TODO don't hard code this
        Collection<PropertyDescriptor> pds = 
            typeManager.getPropertyDescriptorsForExtension("lifecycleExtension");
        if (pds != null) {
            for (PropertyDescriptor pd : pds) {
                org.mule.galaxy.query.Query q = new org.mule.galaxy.query.Query();
                q.add(OpRestriction.in(pd.getProperty() + ".phase", phases));
                
                approveArtifacts(q, null, phases, policies);
            }
        }
        
        activatePolicy(phasesNodeId, phases, policies);
    }

    private void approveArtifacts(org.mule.galaxy.query.Query q, 
                                  Lifecycle lifecycle,
                                  Collection<Phase> phases, 
                                  Policy... policies)
        throws RegistryException, ItemCollectionPolicyException {
        try {
            q.add(OpRestriction.eq("enabled", true));
            SearchResults results = getRegistry().search(q);
            Map<Item, List<ApprovalMessage>> approvals = new HashMap<Item, List<ApprovalMessage>>();
            
            for (Object o : results.getResults()) {
                if (lifecycle != null) {
                    approveItem((Item) o, approvals, lifecycle, policies);
                } else {
                    approveItem((Item) o, approvals, phases, policies);
                }
            }
            
            if (approvals.size() > 0) {
                throw new ItemCollectionPolicyException(approvals);
            }
        } catch (QueryException e) {
            // this should never happen as we know our query is valid
            throw new RuntimeException(e);
        }
    }

    public void setActivePolicies(Lifecycle lifecycle, Policy... policies) 
        throws RegistryException, ItemCollectionPolicyException {
        
        // TODO don't hard code this
        Collection<PropertyDescriptor> pds = 
            typeManager.getPropertyDescriptorsForExtension("lifecycleExtension");
        if (pds != null) {
            for (PropertyDescriptor pd : pds) {
                org.mule.galaxy.query.Query q = new org.mule.galaxy.query.Query();
                q.add(OpRestriction.eq(pd.getProperty() + ".id", lifecycle.getId()));
                
                approveArtifacts(q, lifecycle, null, policies);
            }
        }
        
        activatePolicy(lifecyclesNodeId, policies, lifecycle.getId());
    }

    public void setActivePolicies(Item item, Lifecycle lifecycle, Policy... policies) 
        throws RegistryException, ItemCollectionPolicyException {
        Map<Item, List<ApprovalMessage>> failures = new HashMap<Item, List<ApprovalMessage>>();
        
        approveItem(item, failures, lifecycle, policies);
        
        if (failures.size() > 0) {
            throw new ItemCollectionPolicyException(failures);
        }
        
        activatePolicy(itemsLifecyclesNodeId, policies, item.getId(), lifecycle.getId());
    }
    
    private void activatePolicy(final String nodeId, 
                                final Collection<Phase> phases,
                                final Policy[] policies,
                                final String... nodes) {
        if (phases.size() == 0) {
            return;
        }
        
        jcrTemplate.execute(new JcrCallback() {
            public Object doInJcr(Session session) throws IOException, RepositoryException {
                Node node = session.getNodeByUUID(nodeId);
                
                for (String name : nodes) {
                    node = getOrCreate(node, name);
                }
                for (Phase p : phases) {
                    Node pNode = getOrCreate(node, p.getId());
                    
                    JcrUtil.removeChildren(pNode);
                    
                    for (Policy policy : policies) {
                        getOrCreate(pNode, policy.getId());
                    }
                }
                
                session.save();
                return null;
            }
        });
    }
    private void activatePolicy(final String nodeId, 
                                final Policy[] policies, 
                                final String... nodes) {
        jcrTemplate.execute(new JcrCallback() {
            public Object doInJcr(Session session) throws IOException, RepositoryException {
                Node node = session.getNodeByUUID(nodeId);
                
                for (String name : nodes) {
                    node = getOrCreate(node, name);
                }
                
                JcrUtil.removeChildren(node);
                
                for (Policy policy : policies) {
                    getOrCreate(node, policy.getId());
                }
                
                session.save();
                return null;
            }
        });
    }
    
    public Collection<Policy> getActivePolicies(final Lifecycle l) {
        final Set<Policy> activePolicies = new HashSet<Policy>();
        jcrTemplate.execute(new JcrCallback() {
            public Object doInJcr(Session session) throws IOException, RepositoryException {
                Node node = session.getNodeByUUID(lifecyclesNodeId);
                try {

                    node = node.getNode(l.getId());
                    
                    for (NodeIterator nodes = node.getNodes(); nodes.hasNext();) {
                        activePolicies.add(getPolicy(nodes.nextNode().getName()));
                    }
                } catch (PathNotFoundException e) {
                }
                
                return null;
            }

        });
        return activePolicies;
    }

    public Collection<Policy> getActivePolicies(final Phase p) {
        final Set<Policy> activePolicies = new HashSet<Policy>();
        jcrTemplate.execute(new JcrCallback() {
            public Object doInJcr(Session session) throws IOException, RepositoryException {
                Node node = session.getNodeByUUID(phasesNodeId);
                try {

                    node = node.getNode(p.getId());
                    
                    for (NodeIterator nodes = node.getNodes(); nodes.hasNext();) {
                        activePolicies.add(getPolicy(nodes.nextNode().getName()));
                    }
                } catch (PathNotFoundException e) {
                }
                
                return null;
            }

        });
        return activePolicies;
    }

    public Collection<Policy> getActivePolicies(final Item item, final Lifecycle lifecycle, final boolean includeInherited) {
        final Set<Policy> activePolicies = new HashSet<Policy>();
        jcrTemplate.execute(new JcrCallback() {
            public Object doInJcr(Session session) throws IOException, RepositoryException {
                // Add policies which apply to the lifecycle
                addPolicies(activePolicies, session, 
                            lifecyclesNodeId, lifecycle.getId());
                
                addPolicies(activePolicies, session, itemsLifecyclesNodeId, 
                            item.getId(), lifecycle.getId());
                
                if (includeInherited) {
                    Item policyItem = item.getParent();
                    while (policyItem != null) {
                        // Add policies which apply to this item and lifecycle
                        
                        
                        policyItem = policyItem.getParent();
                    }
                }
                return null;
            }

        });
        return activePolicies;
    }

    public Collection<Policy> getActivePolicies(final Item item, final Phase phase, final boolean includeInherited) {
        final Set<Policy> activePolicies = new HashSet<Policy>();
        jcrTemplate.execute(new JcrCallback() {
            public Object doInJcr(Session session) throws IOException, RepositoryException {
                // Add policies which apply to the phase
                addPolicies(activePolicies, session, 
                            phasesNodeId, phase.getId());
                
                // Add policies which apply to this item and phase
                addPolicies(activePolicies, session, itemsPhasesNodeId, 
                            item.getId(), phase.getId());
                
                if (includeInherited) {
                    Lifecycle lifecycle = phase.getLifecycle();
                    
                    // Add policies which apply to the lifecycle
                    addPolicies(activePolicies, session, 
                                lifecyclesNodeId, lifecycle.getId());

                    // Add policies which apply to this item and lifecycle
                    addPolicies(activePolicies, session, itemsLifecyclesNodeId, 
                                item.getId(), lifecycle.getId());
                    
                    Item policyItem = item.getParent();
                    while (policyItem != null) {
                        addPoliciesForItem(activePolicies, policyItem, session, phase, lifecycle);
                        
                        policyItem = policyItem.getParent();
                    }
                }
                return null;
            }

        });
        return activePolicies;
    }

    public Collection<Policy> getActivePolicies(final Item item) {
        return getActivePolicies(item, true);
    }

    public Collection<Policy> getActivePolicies(final Item item, 
                                                final boolean includeInherited) {
        final Set<Policy> activePolicies = new HashSet<Policy>();
        jcrTemplate.execute(new JcrCallback() {
            public Object doInJcr(Session session) throws IOException, RepositoryException {
                for (Iterator<PropertyInfo> itr = item.getProperties(); itr.hasNext();) {
                    PropertyInfo pi = itr.next();
                    PropertyDescriptor pd = pi.getPropertyDescriptor();
                    if (pd != null && pd.getExtension() instanceof LifecycleExtension) {
                        Phase phase = (Phase) pi.getValue();
                        Lifecycle lifecycle = phase.getLifecycle();
                        
                        // Add policies which apply to the lifecycle
                        addPolicies(activePolicies, session, 
                                    lifecyclesNodeId, lifecycle.getId());
                        
                        // Add policies which apply to the phase
                        addPolicies(activePolicies, session, 
                                    phasesNodeId, phase.getId());
                        
                        // Loop through the item and it's parents and apply the policies.
                        if (includeInherited) {
                            Item policyItem = item;
                            while (policyItem != null) {
                                addPoliciesForItem(activePolicies, policyItem, session, phase, lifecycle);
                                
                                policyItem = policyItem.getParent();
                            }
                        }
                    }
                }

                return null;
            }

        });
        return activePolicies;
    }

    protected void addPoliciesForItem(final Set<Policy> activePolicies, final Item item,
                                    Session session, Phase phase, Lifecycle lifecycle)
        throws ItemNotFoundException, RepositoryException {
        // Add policies which apply to this item and lifecycle
        addPolicies(activePolicies, session, itemsLifecyclesNodeId, 
                    item.getId(), lifecycle.getId());
        
        // Add policies which apply to this item and lifecycle
        addPolicies(activePolicies, session, itemsPhasesNodeId, 
                    item.getId(), phase.getId());
    }
    
    protected void addPolicies(final Set<Policy> activePolicies,
                             final Session session,
                             final String rootNodeId,
                             String... nodeIds) throws ItemNotFoundException, RepositoryException {
        try {
            Node node = session.getNodeByUUID(rootNodeId);
            for (String name : nodeIds) {
                node = node.getNode(name);
            }
            
            for (NodeIterator itr = node.getNodes(); itr.hasNext();) {
                Policy p = policies.get(itr.nextNode().getName());
                if (p != null) {
                    activePolicies.add(p);
                }
            }
        } catch (PathNotFoundException e){
            
        }
    }
    
    public void setJcrTemplate(JcrTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    public void setTypeManager(TypeManager typeManager) {
        this.typeManager = typeManager;
    }
    
}
