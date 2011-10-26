/*
 *    Copyright 2011 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.db.util.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.ut.biolab.medsavant.db.model.UserLevel;
import org.ut.biolab.medsavant.db.util.ConnectionController;

/**
 *
 * @author mfiume
 */
public class UserQueryUtil {
    
    public static List<String> getUserNames() throws SQLException {
        
        ResultSet rs = ConnectionController.executeQuery("SELECT DISTINCT user FROM mysql.user");
        
        List<String> results = new ArrayList<String>();       
        while (rs.next()) {
            results.add(rs.getString(1));
        }
        
        return results;
    }

    public static boolean userExists(String userName) throws SQLException {
        ResultSet rs = ConnectionController.executeQuery("SELECT user FROM mysql.user WHERE user='%s';", userName);
        return rs.next();
    }
    
    public static void addUser(String name, char[] pass, UserLevel level) throws SQLException {
        try {
            ConnectionController.executeUpdate("CREATE USER '%s'@'localhost' IDENTIFIED BY '%s';", name, new String(pass));
        } finally {
            for (int i = 0; i < pass.length; i++) {
                pass[i] = 0;
            }
        }
        grantPrivileges(name, level);
    }

    /**
     * Grant the user the privileges appropriate to their level
     * @param name user name from <code>mysql.user</code> table
     * @param level ADMIN, USER, or GUEST
     * @throws SQLException 
     */
    public static void grantPrivileges(String name, UserLevel level) throws SQLException {
        switch (level) {
            case ADMIN:
                ConnectionController.executeUpdate("GRANT ALL ON %s.* TO '%s'@'localhost';", ConnectionController.getDbname(), name);
                ConnectionController.executeUpdate("GRANT CREATE USER ON *.* TO '%s'@'localhost';", name);
                break;
            case USER:
                ConnectionController.executeUpdate("GRANT SELECT, CREATE TEMPORARY TABLES ON %s.* TO '%s'@'localhost';", ConnectionController.getDbname(), name);
                break;
            case GUEST:
                ConnectionController.executeUpdate("GRANT SELECT ON %s.* TO '%s'@'localhost'", ConnectionController.getDbname(), name);
                break;
        }                
    }

    public static boolean isUserAdmin(String name) throws SQLException {
        if (userExists(name)) {
            // If the user can create other users, they're assumed to be admin.
            ResultSet rs = ConnectionController.executeQuery("SELECT Create_user_priv FROM mysql.user WHERE user='%s';", name);
            rs.next();
            return rs.getString(1).equals("Y");
        } else {
            return false;
        }
    }
    
    public static UserLevel getUserLevel(String name) throws SQLException {
        if (userExists(name)) {
            // If the user can create other users, they're assumed to be admin.
            ResultSet rs = ConnectionController.executeQuery("SELECT Create_user_priv FROM mysql.user WHERE user='%s';", name);
            if (rs.next()) {
                if (rs.getString(1).equals("Y")) {
                    return UserLevel.ADMIN;
                }
            }
            rs = ConnectionController.executeQuery("SELECT Create_tmp_table_priv FROM mysql.db WHERE user='%s'", name, ConnectionController.getDbname());
            if (rs.next()) {
                if (rs.getString(1).equals("Y")) {
                    return UserLevel.USER;
                }
            }
            return UserLevel.GUEST;
        }
        return UserLevel.NONE;
    }
        
    public static void removeUser(String name) throws SQLException {
        ConnectionController.executeUpdate("DROP USER '%s'@'localhost';", name);
    }
}
