package org.ut.biolab.medsavant.db.admin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.util.DBSettings;
import org.ut.biolab.medsavant.db.util.DBUtil;

/**
 *
 * @author mfiume
 */
public class Setup {

    private static void dropTables() throws SQLException {

        if (DBUtil.tableExists(DBSettings.DBNAME, DBSettings.TABLENAME_PATIENTTABLEINFO)) {
            List<String> patientTables = getValuesFromField(DBSettings.TABLENAME_PATIENTTABLEINFO, "patient_tablename");
            for (String s : patientTables) {
                dropTable(s);
            }
        }
        if (DBUtil.tableExists(DBSettings.DBNAME, DBSettings.TABLENAME_VARIANTTABLEINFO)) {
            List<String> variantTables = getValuesFromField(DBSettings.TABLENAME_VARIANTTABLEINFO, "variant_tablename");
            for (String s : variantTables) {
                dropTable(s);
            }
        }

        dropTable(DBSettings.TABLENAME_ANNOTATION);
        dropTable(DBSettings.TABLENAME_REFERENCE);
        dropTable(DBSettings.TABLENAME_PROJECT);
        dropTable(DBSettings.TABLENAME_PATIENTTABLEINFO);
        dropTable(DBSettings.TABLENAME_VARIANTTABLEINFO);
    }

    private static void dropTable(String tablename) throws SQLException {
        Connection c = (ConnectionController.connect(DBSettings.DBNAME));

        c.createStatement().execute(
                "DROP TABLE IF EXISTS " + tablename + ";");
    }

    private static void createTables() throws SQLException {

        Connection c = (ConnectionController.connect(DBSettings.DBNAME));

        c.createStatement().execute(
                "CREATE TABLE `" + DBSettings.TABLENAME_REFERENCE + "` ("
                + "`reference_id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                + "`name` varchar(50) COLLATE latin1_bin NOT NULL,"
                + "PRIMARY KEY (`reference_id`), "
                + "UNIQUE KEY `name` (`name`)"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute("CREATE TABLE `" + DBSettings.TABLENAME_ANNOTATION + "` ("
                + "`annotation_id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                + "`program` varchar(100) COLLATE latin1_bin NOT NULL DEFAULT '',"
                + "`version` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`reference_id` int(11) unsigned NOT NULL,"
                + "`path` varchar(500) COLLATE latin1_bin NOT NULL DEFAULT '',"
                + "`format` varchar(10000) COLLATE latin1_bin NOT NULL DEFAULT '',"
                + "PRIMARY KEY (`annotation_id`)"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE `" + DBSettings.TABLENAME_PROJECT + "` "
                + "(`project_id` int(11) unsigned NOT NULL AUTO_INCREMENT, "
                + "`name` varchar(50) NOT NULL, "
                + "PRIMARY KEY (`project_id`), "
                + "UNIQUE KEY `name` (`name`)"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE `" + DBSettings.TABLENAME_PATIENTTABLEINFO + "` ("
                + "`project_id` int(11) unsigned NOT NULL,"
                + "`patient_tablename` varchar(100) COLLATE latin1_bin NOT NULL,"
                + "UNIQUE KEY `patient_tablename` (`patient_tablename`,`project_id`)"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE `" + DBSettings.TABLENAME_VARIANTTABLEINFO + "` ("
                + "`project_id` int(11) unsigned NOT NULL,"
                + "`reference_id` int(11) unsigned NOT NULL,"
                + "`variant_tablename` varchar(100) COLLATE latin1_bin NOT NULL,"
                + "`annotation_ids` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "UNIQUE KEY `unique` (`project_id`,`reference_id`,`variant_tablename`)"
                + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");
    }

    public static void main(String[] argv) throws SQLException {
        System.out.println("Dropping tables...");
        dropTables();
        System.out.println("Creating tables...");
        createTables();
    }

    private static List<String> getValuesFromField(String tablename, String fieldname) throws SQLException {
        String q = "SELECT `" + fieldname + "` FROM `" + tablename + "`";
        Statement stmt = (ConnectionController.connect(DBSettings.DBNAME)).createStatement();
        ResultSet rs = stmt.executeQuery(q);
        List<String> results = new ArrayList<String>();
        while (rs.next()) {
            results.add(rs.getString(1));
        }
        return results;
    }
}