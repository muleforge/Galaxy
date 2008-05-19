package org.mule.galaxy.web.client.util;


public interface TextBoxBase {
    public abstract void setError(String error_);

    public abstract void setRequired(boolean required);

    public abstract void validate();

    public abstract void addValidator(Validator validator_);

    public abstract void addValidationListener(ValidationListener
            listener_);

    public abstract void removeValidationListener(ValidationListener
            listener_);

    public abstract boolean hasError();
}
