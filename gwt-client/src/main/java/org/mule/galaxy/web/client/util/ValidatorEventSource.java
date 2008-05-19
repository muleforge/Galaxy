package org.mule.galaxy.web.client.util;

interface ValidationEventSource {
    public abstract void addValidationListener(ValidationListener
            listener);

    public abstract void removeValidationListener(ValidationListener
            listener);
} 