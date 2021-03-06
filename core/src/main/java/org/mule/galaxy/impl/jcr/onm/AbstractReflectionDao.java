package org.mule.galaxy.impl.jcr.onm;

import javax.jcr.Node;
import javax.jcr.Session;

public class AbstractReflectionDao<T> extends AbstractDao<T> {

    protected ClassPersister persister;
    
    protected AbstractReflectionDao(Class<T> t, String rootNode) throws Exception {
        this(t, rootNode, false);
    }
    
    protected AbstractReflectionDao(Class<T> t, String rootNode,  boolean generateId) throws Exception {
        super(t, rootNode, generateId);
    }
    
    protected void persist(T o, Node node, Session session) throws Exception {
        persister.persist(o, node, session);
    }

    @SuppressWarnings("unchecked")
    public T build(Node node, Session session) throws Exception {
        T t = (T) persister.build(node, session);
        setId(t, getId(t, node, session));
        return t;
    }

    public void setPersister(ClassPersister persister) {
        this.persister = persister;
    }

}
