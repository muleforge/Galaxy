/**
 * 
 */
package org.mule.galaxy.web.client.util;

import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

import org.mule.galaxy.web.client.Showable;

public class ShowableTabListener extends SelectionListener<TabPanelEvent> {
    private TabItem previous;
    private List<String> params;
    
    public ShowableTabListener(List<String> params) {
        super();
        this.params = params;
    }

    @Override
    public void componentSelected(TabPanelEvent ce) {
        TabItem item = ce.getItem();
        
        if (item.equals(previous)) {
            return;
        }
        
        if (previous != null) {
            Widget widget = previous.getWidget(0);
            if (widget instanceof Showable) {
                ((Showable)widget).hidePage();
            }
        }
        
        Widget widget = item.getWidget(0);
        if (widget instanceof Showable) {
            ((Showable)widget).showPage(params);
        }
        
        previous = item;
        item.layout();
    }
}