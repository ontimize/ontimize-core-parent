package com.ontimize.gui;

import java.rmi.Remote;
import java.util.Hashtable;

import com.ontimize.db.EntityResult;
import com.ontimize.security.PermissionGroupInfo;
import com.ontimize.security.PermissionInfo;

public interface ServerPermissionManager extends Remote {

    public static final String ENTITY_LIST_KEY = "ENTITY_LIST";

    public static final String USER_PROFILE_PERMISSIONS_KEY = "PROFILE_PERMISSIONS";

    public static final String USER_PERMISSIONS_KEY = "USER_PERMISSIONS";

    /**
     * Sets the server permissions value
     * @param keys Keys to identify the user
     * @param sessionId User session identifier
     * @param permissionXML XML value to describes the server permissions
     * @return
     * @throws Exception
     */
    public EntityResult setServerPermissions(Hashtable keys, int sessionId, StringBuffer permissionXML)
            throws Exception;

    /**
     * Get the permission definition for a specified profile
     * @param profileKeys Keys to identify a profile
     * @param sessionId User session identifier
     * @return
     * @throws Exception
     */
    public EntityResult getUserProfileServerPermissions(Hashtable profileKeys, int sessionId) throws Exception;

    public EntityResult setUserProfileServerPermissions(Hashtable profileKeys, StringBuffer permissions, int sessionId)
            throws Exception;

    /**
     * Get the user permissions
     * @param userKeys Keys to identify the user
     * @param sessionId User session identifier
     * @return
     * @throws Exception
     */
    public EntityResult getServerPermissions(Hashtable userKeys, int sessionId) throws Exception;

    public EntityResult getEntityList(int sessionId) throws Exception;

    /**
     * Checks if the specified user has permissions to execute an action in the specified entity
     * @param entity Entity name
     * @param action Action to check
     * @param sessionId User session identifier
     * @param time
     * @return
     * @throws Exception
     */
    public boolean checkActionPermission(String entity, String action, int sessionId, long time) throws Exception;

    public PermissionInfo getPermissionInfo(String entity, String action, int sessionId) throws Exception;

    public PermissionGroupInfo[] getPermissionGroupsInfo() throws Exception;

}
