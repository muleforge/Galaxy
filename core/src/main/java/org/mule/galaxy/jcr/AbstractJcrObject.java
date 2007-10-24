package org.mule.galaxy.jcr;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.mule.galaxy.util.JcrUtil;

public class AbstractJcrObject {

    public static final String VALUE = "value";
    protected Node node;

    public AbstractJcrObject(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    protected String getStringOrNull(String propName) {
        return JcrUtil.getStringOrNull(node, propName);
    }
    
    protected Calendar getDateOrNull(String propName) {
        return JcrUtil.getDateOrNull(node, propName);
    }
    
    protected Value getValueOrNull(String propName) throws PathNotFoundException, RepositoryException {
        return JcrUtil.getValueOrNull(node, propName);
    }

    public void setProperty(String name, Object value) {
        try {
            setProperty(name, value, node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setProperty(String name, Object value, Node n) throws ItemExistsException,
        PathNotFoundException, VersionException, ConstraintViolationException, LockException,
        RepositoryException {
        if (value instanceof Collection) {
            Node child = n.addNode(name);
            
            Collection c = (Collection) value;
            
            for (Object o : c) {
                Node valueNode = child.addNode(VALUE);
                valueNode.setProperty(VALUE, o.toString());
            }
        } else if (value instanceof String) {
            n.setProperty(name, value.toString());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public Object getProperty(String name) {
        try {
            Node child = node.getNode(name);
            
            List<String> values = new ArrayList<String>();
            for (NodeIterator itr = child.getNodes(); itr.hasNext();) {
                Node next = itr.nextNode();

                String value = JcrUtil.getStringOrNull(next, VALUE);
                
                values.add(value);
            }
            return values;
        } catch (PathNotFoundException e) {
            return null;
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

}
