package org.mule.galaxy.web.client.ui;

import org.mule.galaxy.web.client.ui.panel.AbstractShowable;
import org.mule.galaxy.web.client.ui.panel.WidgetHelper;

import com.extjs.gxt.ui.client.data.BaseModel;

// wrapper class for left side nav items
public class NavMenuItem extends BaseModel {

    public static final String NEW = "/new";


    public NavMenuItem(String title, String tokenBase, AbstractShowable listPanel,
                       AbstractShowable formPanel) {
        set("title", title);
        set("tokenBase", tokenBase);
        set("listPanel", listPanel);
        set("formPanel", formPanel);
    }

    public NavMenuItem(String title, String tokenBase) {
        this(title, tokenBase, null, null);
    }

    public NavMenuItem(String title, String tokenBase, AbstractShowable listPanel) {
        this(title, tokenBase, listPanel, null);
    }

    public String getTitle() {
        return (String) get("title");
    }

    public void setTitle(String title) {
        set("title", title);
    }

    public String getTokenBase() {
        return (String) get("tokenBase");
    }

    public void setTokenBase(String tokenBase) {
        set("tokenBase", tokenBase);
    }

    public WidgetHelper getListPanel() {
        return (WidgetHelper) get("listPanel");
    }

    public void setListPanel(AbstractShowable listPanel) {
        set("listPanel", listPanel);
    }

    public WidgetHelper getFormPanel() {
        return (WidgetHelper) get("formPanel");
    }

    public void setFormPanel(AbstractShowable formPanel) {
        set("formPanel", formPanel);
    }
}
