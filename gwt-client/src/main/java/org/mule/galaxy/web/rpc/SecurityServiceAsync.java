package org.mule.galaxy.web.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;

import java.util.Map;


public interface SecurityServiceAsync extends RemoteService {
    void getUsers(AsyncCallback callback);
    
    void addUser(WUser user, String password, AsyncCallback callback);
    
    void updateUser(WUser user, String password, String confirm, AsyncCallback callback);

    void deleteUser(String userId, AsyncCallback callback);

    void getPermissions(int permissionType, AsyncCallback callback);

    void save(WGroup group, AsyncCallback callback);
    
    void getGroupPermissions(AsyncCallback callback);
    
    void applyPermissions(Map group2Permissions, AsyncCallback callback);

    void getGroupPermissions(String itemId, AsyncCallback callback);
    
    void applyPermissions(String itemId, Map group2Permissions, AsyncCallback callback);

    void getGroups(AsyncCallback abstractCallback);
}