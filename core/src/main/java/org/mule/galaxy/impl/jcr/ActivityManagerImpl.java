package org.mule.galaxy.impl.jcr;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.mule.galaxy.DuplicateItemException;
import org.mule.galaxy.NotFoundException;
import org.mule.galaxy.activity.Activity;
import org.mule.galaxy.activity.ActivityManager;
import org.mule.galaxy.impl.jcr.onm.AbstractReflectionDao;
import org.mule.galaxy.security.AccessControlManager;
import org.mule.galaxy.security.AccessException;
import org.mule.galaxy.security.Permission;
import org.mule.galaxy.security.User;
import org.springframework.dao.ConcurrencyFailureException;
import org.springmodules.jcr.JcrCallback;

public class ActivityManagerImpl extends AbstractReflectionDao<Activity> implements ActivityManager {

    private DatatypeFactory dataTypeFactory;
    private AccessControlManager accessControlManager;
    
    public ActivityManagerImpl() throws Exception {
        super(Activity.class, "activities", true);
        
        dataTypeFactory = DatatypeFactory.newInstance();
    }

    @SuppressWarnings("unchecked")
    public Collection<Activity> getActivities(final Date from, 
                                              final Date to, 
                                              final String user, 
                                              final String itemId, 
                                              final String text, 
                                              final EventType eventType, 
                                              final int start, final int results, final boolean ascending) throws AccessException {
        accessControlManager.assertAccess(Permission.VIEW_ACTIVITY);
        
        return (Collection<Activity>) execute(new JcrCallback() {
            public Object doInJcr(Session session) throws IOException, RepositoryException {
                QueryManager qm = getQueryManager(session);
                StringBuilder qstr = new StringBuilder();
                qstr.append("//element(*, galaxy:activity)");
                boolean first = true;
                
                if (from != null) {
                    append(qstr, "date", ">=", getDateString(from), first, false);
                    first = false;
                } 
                if (to != null) {
                    append(qstr, "date", "<=", getDateString(to), first, false);
                    first = false;
                }

                if (user != null) {
                    if (ActivityManager.SYSTEM.equals(user)) {
                        if (first) {
                            qstr.append("[not(@user)");
                            first = false;
                        } else {
                            qstr.append(" and not(@user)");
                        }
                    } else {
                        append(qstr, "user", "=", user, first, true);
                    }
                    first = false;
                }

                if (eventType != null) {
                    append(qstr, "eventType", "=", eventType.toString(), first, true);
                    first = false;
                }

                if (itemId != null) {
                    append(qstr, "itemId", "=", itemId, first, true);
                    first = false;
                }
                
                if (text != null && text.length() > 0) {
                    if (first) {
                        qstr.append("[");
                    } else {
                        qstr.append(" and ");
                    }
                    
                    qstr.append("jcr:contains(@message, '").append(text).append("')");
                    first = false;
                }
                
                if (!first) {
                    qstr.append("]");
                }
                qstr.append(" order by @date ");
                if (ascending) {
                    qstr.append("ascending");
                } else {
                    qstr.append("descending");
                }
                
//                System.out.println(qstr.toString())
                Query query = qm.createQuery(qstr.toString(), Query.XPATH);
                
                QueryResult result = query.execute();
                
                Collection<Activity> activities = new LinkedList<Activity>();
                int i = 0;
                NodeIterator nodes = result.getNodes();
                try {
                    nodes.skip(start);
                    
                } catch (NoSuchElementException e) {
                    return activities;
                }
                
                while(nodes.hasNext()) {
                    Node node = nodes.nextNode();
                    
                    try {
                        activities.add(build(node, session));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    i++;
                    
                    if (i == results) {
                        break;
                    }
                }
                return activities;
            }
        });
    }

    protected void append(StringBuilder qstr, String property, String operator, String value, boolean first, boolean quote) {
        if (first) {
            qstr.append("[@");
        } else {
            qstr.append(" and @");
        }
        qstr.append(property).append(operator);
        if (quote) qstr.append("'");
        
        qstr.append(value);
        
        if (quote) qstr.append("'");
    }

    protected String getDateString(Date from) {
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(from);
        
        XMLGregorianCalendar cal = dataTypeFactory.newXMLGregorianCalendar(gc);
        
        return "xs:dateTime('" + cal.toXMLFormat() + "')";
    }

    @Override
    protected Node getNodeForObject(Node parent, Activity t) throws RepositoryException {
        Calendar date = t.getDate();
        
        parent = JcrUtil.getDateNode(parent, date);
        
        return parent;
    }
    
    @Override
    protected void doCreateInitialNodes(Session session, Node objects) throws RepositoryException {
        super.doCreateInitialNodes(session, objects);
        
        Node objectsNode = session.getRootNode().getNode("activities");
        
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        
        // Create an initial log for activities so that we don't get concurrency
        // problems when we first start.
        JcrUtil.getDateNode(objectsNode, c);
    }

    @Override
    protected String getNodeType() {
        return "galaxy:activity";
    }

    public void logActivity(String activity, EventType eventType) {
        logActivity(activity, eventType, null, null);
    }

    public synchronized void logActivity(String activity, EventType eventType, User user, String itemId) {
        String logDestination = System.getProperty("galaxy.activityLog.destination");
        Calendar c = Calendar.getInstance();
        Date date = new Date();
        c.setTime(date);
        if (logDestination != null) {
            if (logDestination.equals("disabled")) {
                return;
            } else if (logDestination.equals("stdout")) {
                logToStdout(activity, eventType, user);
                return;
            }
        }
        try {
            save(new Activity(user, eventType, c, itemId, activity));
        } catch (ConcurrencyFailureException e) {
            // Sleep briefly, then re-try.
            try {
                Thread.sleep(1000);
                save(new Activity(user, eventType, c, itemId, activity));
            } catch (Exception e2) {
                logToStdout(activity, eventType, user);
            }
        } catch (DuplicateItemException e1) {
            // should never happen
            throw new RuntimeException(e1);
        } catch (NotFoundException e1) {
            // should never happen
            throw new RuntimeException(e1);
        }
    }

    private void logToStdout(String activity, EventType eventType, User user) {
        try {
            Date date = new Date();
            DateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa");
            String loggerTime = format.format(date);
            System.out.println(loggerTime + " " + (user != null ? user.getUsername() : " Service User ") + " " + eventType + " " + activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAccessControlManager(AccessControlManager accessControlManager) {
        this.accessControlManager = accessControlManager;
    }
    
}
