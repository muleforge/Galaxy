package org.mule.galaxy.web.client.util;

public interface Validator {
    abstract boolean isInputValid(String input);

    abstract String getErrorMessage();
} 