package org.mule.galaxy.web.client.admin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;
import org.mule.galaxy.web.client.AbstractMenuPanel;
import org.mule.galaxy.web.client.Galaxy;
import org.mule.galaxy.web.rpc.UserService;
import org.mule.galaxy.web.rpc.UserServiceAsync;

public class AdministrationPanel extends AbstractMenuPanel {

    private UserServiceAsync userService;
    private Hyperlink add;
    
    public AdministrationPanel(Galaxy galaxy) {
        super(galaxy);
        
        userService = (UserServiceAsync) GWT.create(UserService.class);
        final AdministrationPanel adminPanel = this;
        
        ServiceDefTarget target = (ServiceDefTarget) userService;
        target.setServiceEntryPoint("/handler/userService.rpc");
        
        Hyperlink link = new Hyperlink("Lifecycles", "");
        link.addClickListener(new ClickListener() {
            public void onClick(Widget w) {
                showUsers();
            }
        });
        addMenuItem(link);
        
        link = new Hyperlink("Policies", "");
        link.addClickListener(new ClickListener() {
            public void onClick(Widget w) {
                showUsers();
            }
        });
        addMenuItem(link);
        
        link = new Hyperlink("Queries", "");
        link.addClickListener(new ClickListener() {
            public void onClick(Widget w) {
                showUsers();
            }
        });
        addMenuItem(link);
        
        link = new Hyperlink("Users", "");
        link.addClickListener(new ClickListener() {
            public void onClick(Widget w) {
                showUsers();
            }
        });
        
        add = new Hyperlink("[Add]","");
        add.addClickListener(new ClickListener() {
            public void onClick(Widget w) {
                setMain(new UserForm(adminPanel));
            }
        });
        HorizontalPanel item = new HorizontalPanel();
        item.setSpacing(10);
        item.add(link);
        item.add(add);
        
        addMenuItem(item);
    }


    protected void addUser() {
        // TODO Auto-generated method stub
        
    }


    public void showUsers() {
        setMain(new UserListPanel(this));
    }

    public UserServiceAsync getUserService() {
        return userService;
    }
}
