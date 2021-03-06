package org.mule.galaxy.web.client.ui.panel;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Layout;

/* usually used as a child panel -- no heading */ 
public class PaddedContentPanel extends ContentPanel {

    public PaddedContentPanel() {
        super();
        setBodyBorder(false);
        setBorders(false);
        setMonitorWindowResize(true);
        setAutoWidth(true);
        setAutoHeight(true);
        setHeaderVisible(false);
        addStyleName("padded-panel");
    }

    public PaddedContentPanel(Layout layout) {
        this();
        setLayout(layout);
    }

}
