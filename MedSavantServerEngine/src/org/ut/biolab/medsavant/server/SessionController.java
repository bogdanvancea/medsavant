package org.ut.biolab.medsavant.server;

import java.io.Serializable;
import java.rmi.RemoteException;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.util.CustomTables;
import org.ut.biolab.medsavant.server.api.SessionAdapter;

/**
 *
 * @author mfiume
 */
public class SessionController extends java.rmi.server.UnicastRemoteObject implements SessionAdapter {

    int lastSessionId = 0;

    private static SessionController instance;

    public static SessionController getInstance() throws RemoteException {
        if (instance == null) {
            instance = new SessionController();
        }
        return instance;
    }

    public SessionController() throws RemoteException {}

    @Override
    public String registerNewSession(String uname, String pw, String dbname) {

        String sessionId = ++lastSessionId + "";

        boolean success = ConnectionController.registerCredentials(sessionId,uname,pw,dbname);

        if (success) {
            return sessionId;
        } else {
            return null;
        }
    }

    @Override
    public void unregisterSession(String sessionId) throws RemoteException {
        ConnectionController.removeSession(sessionId);
        CustomTables.getInstance().clearMap(sessionId);
        
    }

    @Override
    public boolean testConnection(String sessionId) throws RemoteException {
        return ConnectionController.connectPooled(sessionId) != null;
    }

    public String getUserForSession(String sid) {
        return ConnectionController.getUserForSession(sid);
    }

}
