package org.mule.galaxy.web.client.util;

import com.google.gwt.user.client.ui.Label;

public class PasswordTextBox extends com.google.gwt.user.client.ui.PasswordTextBox
        implements org.mule.galaxy.web.client.util.TextBoxBase {

    public boolean hasError() {
        return delegate.hasError();
    }

    private TextBoxDelegate delegate;

    private PasswordTextBox() {
    }

    public PasswordTextBox(Label label) {
        super();
        delegate = new TextBoxDelegate(label, this);
    }

    public void addValidationListener(ValidationListener listener) {
        delegate.addValidationListener(listener);
    }

    public void removeValidationListener(ValidationListener listener) {
        delegate.removeValidationListener(listener);
    }

    public void setError(String error) {
        delegate.setError(error);
    }

    public void setRequired(boolean required) {
        delegate.setRequired(required);
    }

    public void validate() {
        delegate.validate();
    }

    public void addValidator(Validator validator) {
        delegate.addValidator(validator);
    }
}
