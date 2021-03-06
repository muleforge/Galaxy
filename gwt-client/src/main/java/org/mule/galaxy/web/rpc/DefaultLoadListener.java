/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.mule.galaxy.web.rpc;

import org.mule.galaxy.web.client.ui.help.PanelConstants;
import org.mule.galaxy.web.client.ui.panel.ErrorPanel;

import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.google.gwt.core.client.GWT;

public class DefaultLoadListener extends LoadListener {
    private ErrorPanel errorPanel;
    private static final PanelConstants panelMessages = (PanelConstants) GWT.create(PanelConstants.class);

    public DefaultLoadListener(ErrorPanel panel) {
        super();
        this.errorPanel = panel;
    }

    
    @Override
    public void loaderLoadException(LoadEvent le) {
        Throwable caught = le.exception;
        String msg = caught.getMessage();
        
        GWT.log("Exception loading data.", caught);
        if (msg != null || !"".equals(msg)) {
            errorPanel.setMessage(panelMessages.errorCommunicatingServer() + caught.getMessage() + "");
        } else {
            errorPanel.setMessage(panelMessages.errorCommunicatingExeption() + caught.getClass().getName());
        }
    }
    
}
