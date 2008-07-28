package org.mule.galaxy.web.client.property;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class SimplePropertyPanel extends PropertyPanel {

    private TextBox valueTB;
    private Label valueLabel;

    protected Widget createEditForm() {
        valueTB = new TextBox();
        valueTB.setVisibleLength(50);
        return valueTB;
    }

    protected Object getRemoteValue() {
        return valueTB.getText();
    }

    protected String getRenderedText() {
        String txt = (String) getProperty().getValue();
        
        if ("".equals(txt) || txt == null) {
            txt = "-----";
        }
        
        return txt;
    }
    
    protected Widget createViewWidget() {
        valueLabel = new Label();
        return valueLabel;
    }

    public void showEdit() {
        valueTB.setText(getRenderedText());
        
        super.showEdit();
    }

    public void showView() {
        valueLabel.setText(getRenderedText());
        
        super.showView();
    }
}
