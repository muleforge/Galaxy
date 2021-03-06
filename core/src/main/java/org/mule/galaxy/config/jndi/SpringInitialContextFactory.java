package org.mule.galaxy.config.jndi;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;

public class SpringInitialContextFactory implements InitialContextFactory
{
    private static final transient Log log = LogFactory.getLog(SpringInitialContextFactory.class);

    private static final Map<String, BeanFactory> cache = new HashMap<String, BeanFactory>();

    private static Context singleton;

    /**
     * A factory method which can be used to initialise a singleton JNDI context from
     * inside a Spring.xml such that future calls to new InitialContext() will reuse
     * it
     *
     * @return context
     */
    public static Context makeInitialContext()
    {
        singleton = new DefaultSpringJndiContext();
        return singleton;
    }

    public Context getInitialContext(Hashtable environment) throws NamingException
    {
        if (singleton != null)
        {
            return singleton;
        }
        Resource resource;
        Object value = environment.get(Context.PROVIDER_URL);
        String key = "jndi.xml";
        if (value == null)
        {
            resource = new ClassPathResource(key);
        }
        else
        {
            if (value instanceof Resource)
            {
                resource = (Resource) value;
            }
            else
            {
                ResourceEditor editor = new ResourceEditor();
                key = value.toString();
                editor.setAsText(key);
                resource = (Resource) editor.getValue();
            }
        }
        BeanFactory context = loadContext(resource, key);
        Context answer = (Context) context.getBean("jndi");
        if (answer == null)
        {
            log.warn("No JNDI context available in JNDI resource: " + resource);
            answer = new DefaultSpringJndiContext(environment, new ConcurrentHashMap());
        }
        return answer;
    }

    protected BeanFactory loadContext(Resource resource, String key)
    {
        synchronized (cache)
        {
            BeanFactory answer = cache.get(key);
            if (answer == null)
            {
                answer = createContext(resource);
                cache.put(key, answer);
            }
            return answer;
        }
    }

    protected BeanFactory createContext(Resource resource)
    {
        log.info("Loading JNDI context from: " + resource);
        return new SpringInitialContextApplicationContext(new Resource[]{resource});
    }

    /**
     * Simple implementation of AbstractXmlApplicationContext that allows
     * {@link org.springframework.core.io.Resource} to be used in the constructor
     */
    class SpringInitialContextApplicationContext extends AbstractXmlApplicationContext
    {
        private Resource[] configResources;

        public SpringInitialContextApplicationContext(Resource[] resources)
        {
            super();
            configResources = resources;
            refresh();
        }

        protected Resource[] getConfigResources()
        {
            return configResources;
        }
    }

}