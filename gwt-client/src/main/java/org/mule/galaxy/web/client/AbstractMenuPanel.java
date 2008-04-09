package org.mule.galaxy.web.client;

import org.mule.galaxy.web.rpc.RegistryServiceAsync;
import org.mule.galaxy.web.rpc.SecurityServiceAsync;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractMenuPanel extends AbstractComposite implements ErrorPanel {

    private DockPanel panel;
    private FlowPanel leftMenuContainer;
    private FlowPanel mainPanel;
    private Widget mainWidget;
    private FlowPanel topPanel;
    private Widget topWidget;
    private FlowPanel errorPanel;
    private FlowPanel leftMenu;
    private Galaxy galaxy;
    private FlowPanel centerPanel;
    
    public AbstractMenuPanel(Galaxy galaxy) {
        super();
        this.galaxy = galaxy;
        
        panel = new DockPanel();
        panel.setSpacing(0);

        leftMenu = new FlowPanel() {
            protected void onLoad() {

                Element br = DOM.createElement("br");
                DOM.setElementAttribute(br, "class", "clearit");
                DOM.appendChild(DOM.getParent(this.getElement()), br);
            }
        };
        leftMenu.setStyleName("left-menu");
        
        panel.add(leftMenu, DockPanel.WEST);
        
        leftMenuContainer = new FlowPanel(){

            protected void onLoad() {

                Element br = DOM.createElement("br");
                DOM.setElementAttribute(br, "class", "clearit");
                DOM.appendChild(DOM.getParent(this.getElement()), br);
            }
            
        };
        leftMenuContainer.setStyleName("left-menu-container");
        
        leftMenu.add(leftMenuContainer);

        centerPanel = new FlowPanel();
        panel.add(centerPanel, DockPanel.CENTER);
        panel.setCellWidth(centerPanel, "100%");
        
        mainPanel = new FlowPanel();
        mainPanel.setStyleName("main-panel");
        centerPanel.add(mainPanel);
        
        errorPanel = new FlowPanel();
        errorPanel.setStyleName("error-panel");

        topPanel = new FlowPanel();
        topPanel.setStyleName("top-panel");
        
        initWidget(panel);
    }

    public Galaxy getGalaxy() {
        return galaxy;
    }


    public RegistryServiceAsync getRegistryService() {
        return galaxy.getRegistryService();
    }

    public SecurityServiceAsync getSecurityService() {
        return galaxy.getSecurityService();
    }
    
    public void addMenuItem(Widget widget) {
        leftMenuContainer.add(widget);
    }
    
    public void removeMenuItem(Widget widget) {
        leftMenuContainer.remove(widget);
    }
    
    public void setMain(Widget widget) {
        if (mainWidget != null) {
            mainPanel.remove(mainWidget);
        }
        
        errorPanel.clear();
        mainPanel.remove(errorPanel);
        
        this.mainWidget = widget;
        
        mainPanel.add(widget);
    }    
    
    public void setTop(Widget widget) {
        if (centerPanel.getWidgetIndex(topPanel) == -1) {
            centerPanel.insert(topPanel, 0);
        }
        
        if (topWidget != null)
            topPanel.remove(topWidget);
        
        if (widget instanceof AbstractComposite) {
            ((AbstractComposite)widget).onShow();
        }
        topWidget = widget;
        topPanel.add(widget);
    }
    
    public void setMain(final PageInfo page) {
        final Widget menu = this;
        PageInfo wrapper = new PageInfo(page.getName()) {
            public AbstractComposite createInstance() {
                return page.createInstance();
            }

            public void show() {
                int idx = galaxy.getTabPanel().getWidgetIndex(menu);
                galaxy.getTabPanel().selectTab(idx);
                page.show();
            }
        };
        galaxy.show(wrapper, false);
    }
    
    protected int getErrorPanelPosition() {
        return 0;
    }

    public void setMessage(Label label) {
        errorPanel.clear();
        
        int pos = getErrorPanelPosition();
        if (pos > mainPanel.getWidgetCount()) pos = mainPanel.getWidgetCount();
        errorPanel.add(label);
        
        mainPanel.insert(errorPanel, pos);
    }
    
    public void setMessage(String string) {
        setMessage(new Label(string));
    }

    public void addPage(PageInfo page) {
        galaxy.addPage(page);
    }

    /**
     * If you use this method, please make sure that the composite that you're instantiating
     * doesn't do anything resource intensive in the constructor. Thats what AbstractComposite.onShow
     * is for!
     * 
     * @param token
     * @param composite
     * @return
     */
    public MenuPanelPageInfo createPageInfo(String token, final AbstractComposite composite) {
        MenuPanelPageInfo page = new MenuPanelPageInfo(token, this) {
            public AbstractComposite createInstance() {
                return composite;
            }
        };
        galaxy.addPage(page);
        return page;
    }
    
    protected int getTabIndex() {
        return galaxy.getTabPanel().getWidgetIndex(this);
    }

    public Widget getMain() {
        return mainWidget;
    };
}
