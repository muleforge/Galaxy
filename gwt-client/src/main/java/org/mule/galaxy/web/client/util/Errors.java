package org.mule.galaxy.web.client.util;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.HashMap;
import java.util.Iterator;

public final class Errors {
    public static final String STYLE = "as-Error";
    private static Errors singleton = new Errors();
    private ErrorPanel errorPanel = new ErrorPanel();
    private ErrorMessages messages = new ErrorMessages();
    private HashMap errors = new HashMap();

    private Errors() {
    }

    public void add(TextBoxBase source, String error) {
        errors.put(source, error);
        errorPanel.update();
    }

    public static final Errors getInstance() {
        return singleton;
    }

    public ErrorPanel getErrorPanel() {
        return errorPanel;
    }

    public ErrorMessages getErrorMessages() {
        return messages;
    }

    public void remove(TextBoxBase source) {
        errors.remove(source);
        errorPanel.update();
    }

    public final class ErrorPanel extends Composite {
        private Label error = new Label();

        private ErrorPanel() {
            VerticalPanel container = new VerticalPanel();
            container.add(error);
            error.addStyleName(Errors.STYLE);
            initWidget(container);
        }

        private void update() {
            error.setText("");
            error.setVisible(false);
            for (Iterator iter = errors.entrySet().iterator(); iter.hasNext();) {
                String element = (String) iter.next();
                error.setVisible(true);
                error.setText(element);
            }
        }
    }

    public final class ErrorMessages {
    }
}
