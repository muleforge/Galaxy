package org.mule.galaxy.impl.jcr.onm;

import javax.jcr.Node;
import javax.jcr.Session;

import org.mule.galaxy.impl.jcr.JcrUtil;

public class DefaultPersister implements FieldPersister {

    public Object build(Node n, FieldDescriptor fd, Session session) throws Exception {
        return JcrUtil.getProperty(fd.getName(), n);
    }

    public void persist(Object o, Node n, FieldDescriptor fd, Session session) throws Exception {
        String name = fd.getName();
        JcrUtil.setProperty(name, o, n);
    }

    
}
