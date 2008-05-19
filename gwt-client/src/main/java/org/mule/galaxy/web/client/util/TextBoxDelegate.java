package org.mule.galaxy.web.client.util;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import java.util.Iterator;
import java.util.Vector;

class TextBoxDelegate implements
                      ChangeListener, FocusListener, ValidationEventSource, TextBoxBase {
    public boolean hasError() {
        // TODO Auto-generated method stub
        return (this.error != null);
    }

    private boolean required = false;
    private Label label;
    private String error = null;
    private com.google.gwt.user.client.ui.TextBoxBase superFake;

    private TextBoxDelegate() {
    }

    ;
    private Vector validators = new Vector();

    public void addValidator(Validator validator) {
        validators.add(validator);
    }

    private ValidatorImpl validator = new ValidatorImpl();

    TextBoxDelegate(Label label,
                    com.google.gwt.user.client.ui.TextBoxBase superFake) {
        superFake = superFake;
        superFake.addChangeListener(this);
        superFake.addFocusListener(this);
        label = label;
        label.addStyleName("as-TextBoxLabel");
    }

    /* (non-Javadoc)
    * @see
com.bbh.autoservices.client.ui.TextBoxBase#setError(java.lang.String)
    */
    public void setError(String error) {
        this.error = error;
        Errors.getInstance().add(this, error);
        label.addStyleName(Errors.STYLE);
    }

    /* (non-Javadoc)
    * @see
com.bbh.autoservices.client.ui.TextBoxBase#setRequired(boolean)
    */
    public void setRequired(boolean required) {
        if (required != this.required) {
            if (required)
                label.addStyleName("as-RequiredTextBoxLabel");
            else
                label.removeStyleName("as-RequiredTextBoxLabel");
        }
        this.required = required;
    }

    public void onChange(Widget sender) {
        if (error != null) {
            error = null;
            Errors.getInstance().remove(this);
            label.removeStyleName(Errors.STYLE);
        }
    }

    public void onFocus(Widget sender) {
        // TODO Auto-generated method stub
    }

    public void onLostFocus(Widget sender) {
        validate();
    }

    /* (non-Javadoc)
    * @see com.bbh.autoservices.client.ui.TextBoxBase#validate()
    */
    public void validate() {
        if (this.required && superFake.getText() == null)
            this.setError(label.getText() + " is required.");
        else if (superFake.getText() == null) ;
        else if (isValid())
            validator.raiseValidationEvent(this);
    }

    private boolean isValid() {
        for (Iterator iter = validators.iterator(); iter.hasNext();) {
            Validator element = (Validator) iter.next();
            if (!element.isInputValid(superFake.getText())) {
                this.setError(element.getErrorMessage());
                return false;
            }
        }
        return true;
    }

    /* (non-Javadoc)
    * @see
com.bbh.autoservices.client.ui.TextBoxBase#addValidationListener(com.bbh.au toservices.client.ui.ValidationListener)
    */
    public void addValidationListener(ValidationListener listener) {
        validator.addValidationListener(listener);
    }

    /* (non-Javadoc)
    * @see
com.bbh.autoservices.client.ui.TextBoxBase#removeValidationListener(com.bbh .autoservices.client.ui.ValidationListener)
    */
    public void removeValidationListener(ValidationListener listener) {
        validator.removeValidationListener(listener);
    }

    private class ValidatorImpl implements ValidationEventSource {
        private Vector listeners = new Vector();

        /* (non-Javadoc)
        * @see
com.bbh.autoservices.client.ui.Validator#addValidationListener(com.bbh.auto services.client.ValidationListener)
        */
        public void addValidationListener(ValidationListener listener) {
            listeners.add(listener);
        }

        /* (non-Javadoc)
        * @see
com.bbh.autoservices.client.ui.Validator#removeValidationListener(com.bbh.a utoservices.client.ValidationListener)
        */
        public void removeValidationListener(ValidationListener listener) {
            listeners.remove(listener);
        }

        public void raiseError(TextBoxBase source, String error) {
            for (Iterator iter = listeners.iterator(); iter.hasNext();) {
                ValidationListener element = (ValidationListener) iter.next();
                element.onError(source, error);
            }
        }

        public void raiseValidationEvent(TextBoxBase source) {
            for (Iterator iter = listeners.iterator(); iter.hasNext();) {
                ValidationListener element = (ValidationListener) iter.next();
                element.onValidation(source);
            }
        }
    }
}
