package org.mule.galaxy.event;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * An extenstion point bean allowing to add custom Galaxy event listeners. Just configure this bean as a
 * regular one in Spring configuration file and populate via {@link #setCustomListeners(java.util.List)}, e.g.:
 *
 * <pre>
 * &lt;bean class="org.mule.galaxy.event.CustomGalaxyListenersBean">
        &lt;property name="customListeners">
            &lt;bean class="com.example.galaxy.event.listener.MyCustomEventListener"/>
        &lt;/property>
    &lt;/bean>
    </pre>

 * The listener must adhere to the Galaxy listener conventions.
 * <p/>
 * <strong>Note:</strong> this class will automatically discover a standard Galaxy's event manager
 * instance. If however, one needs to be overridden, just inject it via the {@link #setEventManager(EventManager)}.
 * <p/>
 * The custom listeners list is cleared once this bean finished registering those with the central event
 * manager..
 */
public class CustomGalaxyListenersBean implements ApplicationContextAware, InitializingBean {

    private ApplicationContext context;

    private EventManager eventManager;

    private List customListeners = new ArrayList();

    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    public void afterPropertiesSet() throws Exception {
        if (eventManager == null) {
            // only discover the bean if not set OOTB
            this.eventManager = (EventManager) context.getBean("eventManager", EventManager.class);
        }
        for (Object listener : customListeners) {
            eventManager.addListener(listener);
        }

        // clear custom listeners to kill the hard-ref, there's one in eventManager by now already
        this.customListeners.clear();
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public void setEventManager(final EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public void setCustomListeners(final List customListeners) {
        this.customListeners = customListeners;
    }
}
