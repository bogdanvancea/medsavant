/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.util.query;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.table.PatientTable;
import org.ut.biolab.medsavant.db.table.PatientMapTable;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.table.PatientFormatTable;
import org.ut.biolab.medsavant.db.util.DBSettings;

/**
 *
 * @author Andrew
 */
public class PatientQueryUtil {
    
    public static List<Vector> getBasicPatientInfo(int projectId, int limit) throws SQLException, NonFatalDatabaseException {
        
        String tablename = getPatientTablename(projectId);
        
        Connection c = ConnectionController.connect();
        ResultSet rs = c.createStatement().executeQuery(
                "SELECT " + PatientTable.FIELDNAME_ID + "," + PatientTable.FIELDNAME_FAMILYID + "," + PatientTable.FIELDNAME_PEDIGREEID + "," + PatientTable.FIELDNAME_HOSPITALID + "," + PatientTable.FIELDNAME_DNAIDS + " " +
                "FROM " + tablename);
        
        List<Vector> result = new ArrayList<Vector>();
        while(rs.next()){
            Vector v = new Vector();
            v.add(rs.getInt(1));
            for(int i = 2; i < 7; i++){
                v.add(rs.getString(i));
            }
            result.add(v);
        }
        return result;
    }
    
    public static Vector getPatientRecord(int projectId, int patientId) throws SQLException {
        
        String tablename = getPatientTablename(projectId);
        
        Connection c = ConnectionController.connect();
        ResultSet rs = c.createStatement().executeQuery(
                "SELECT *" + 
                " FROM " + tablename + 
                " WHERE " + PatientTable.FIELDNAME_ID + "=" + patientId);
        
        rs.next();
        Vector v = new Vector();
        for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
            v.add(rs.getObject(i));
        }
        return v;
    }
    
    public static List<String> getPatientFieldAliases(int projectId) throws SQLException {
                
        Connection c = ConnectionController.connect();
        ResultSet rs = c.createStatement().executeQuery(
                "SELECT " + PatientFormatTable.FIELDNAME_ALIAS + 
                " FROM " + PatientFormatTable.TABLENAME + 
                " WHERE " + PatientFormatTable.FIELDNAME_PROJECTID + "=" + projectId + 
                " ORDER BY `" + PatientFormatTable.FIELDNAME_POSITION + "`");
        
        List<String> result = new ArrayList<String>();
        result.add(PatientTable.ALIAS_ID);
        result.add(PatientTable.ALIAS_FAMILYID);
        result.add(PatientTable.ALIAS_PEDIGREEID);
        result.add(PatientTable.ALIAS_HOSPITALID);
        result.add(PatientTable.ALIAS_DNAIDS);
        while(rs.next()){
            result.add(rs.getString(1));
        }
        return result;
    }
    
    public static List<CustomField> getPatientFields(int projectId) throws SQLException {
                
        Connection c = ConnectionController.connect();
        ResultSet rs = c.createStatement().executeQuery(
                "SELECT " + PatientFormatTable.FIELDNAME_COLUMNNAME + "," + 
                PatientFormatTable.FIELDNAME_COLUMNTYPE + "," + 
                PatientFormatTable.FIELDNAME_FILTERABLE + "," + 
                PatientFormatTable.FIELDNAME_ALIAS + "," +
                PatientFormatTable.FIELDNAME_DESCRIPTION + 
                " FROM " + PatientFormatTable.TABLENAME + 
                " WHERE " + PatientFormatTable.FIELDNAME_PROJECTID + "=" + projectId +
                " ORDER BY " + PatientFormatTable.FIELDNAME_POSITION);
        
        List<CustomField> result = new ArrayList<CustomField>();
        result.add(new CustomField(PatientTable.FIELDNAME_ID, "int(11)", false, PatientTable.ALIAS_ID, ""));
        result.add(new CustomField(PatientTable.FIELDNAME_FAMILYID, "varchar(100)", false, PatientTable.ALIAS_FAMILYID, ""));
        result.add(new CustomField(PatientTable.FIELDNAME_PEDIGREEID, "varchar(100)", false, PatientTable.ALIAS_PEDIGREEID, ""));
        result.add(new CustomField(PatientTable.FIELDNAME_HOSPITALID, "varchar(100)", false, PatientTable.ALIAS_HOSPITALID, ""));
        result.add(new CustomField(PatientTable.FIELDNAME_DNAIDS, "varchar(1000)", false, PatientTable.ALIAS_DNAIDS, ""));
        
        while(rs.next()){
            result.add(new CustomField(
                    rs.getString(PatientFormatTable.FIELDNAME_COLUMNNAME), 
                    rs.getString(PatientFormatTable.FIELDNAME_COLUMNTYPE), 
                    rs.getBoolean(PatientFormatTable.FIELDNAME_FILTERABLE), 
                    rs.getString(PatientFormatTable.FIELDNAME_ALIAS), 
                    rs.getString(PatientFormatTable.FIELDNAME_DESCRIPTION)));
        }
        return result;
    }
    
    public static String getPatientTablename(int projectId) throws SQLException {
        Connection c = ConnectionController.connect();
        ResultSet rs = c.createStatement().executeQuery(
                "SELECT " + PatientMapTable.FIELDNAME_PATIENTTABLENAME 
                + " FROM `" + PatientMapTable.TABLENAME + "`"
                + " WHERE " + PatientMapTable.FIELDNAME_PROJECTID + "=" + projectId);
        rs.next();
        return rs.getString(1);
    }
    
     
    public static void createPatientTable(int projectid, File patientFormatFile) throws SQLException, ParserConfigurationException, SAXException, IOException {

        String patientTableName = DBSettings.createPatientTableName(projectid);        
        Connection c = ConnectionController.connect();

        //create basic fields
        String query = 
                "CREATE TABLE `" + patientTableName + "` ("
                + "`" + PatientTable.FIELDNAME_ID + "` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                + "`" + PatientTable.FIELDNAME_FAMILYID + "` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + PatientTable.FIELDNAME_PEDIGREEID + "` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + PatientTable.FIELDNAME_HOSPITALID + "` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + PatientTable.FIELDNAME_DNAIDS + "` varchar(1000) COLLATE latin1_bin DEFAULT NULL,";
        
        //add any extra fields
        List<CustomField> customFields = new ArrayList<CustomField>();
        if(patientFormatFile != null){
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(patientFormatFile);
            doc.getDocumentElement().normalize();

            NodeList fields = doc.getElementsByTagName("field");
            for(int i = 0; i < fields.getLength(); i++){
                Element field = (Element)(fields.item(i));       
                CustomField current = new CustomField(
                        field.getAttribute("name"),
                        field.getAttribute("type"),
                        field.getAttribute("filterable").equals("true"),
                        field.getAttribute("alias"),
                        field.getAttribute("description")
                        );
                customFields.add(current);
                query += current.generateSchema();
            }        
        }
        
        query += "PRIMARY KEY (`" + PatientTable.FIELDNAME_ID + "`)"
                + ") ENGINE=MyISAM;";
        
        //create table
        c.createStatement().execute(query);

        //add to tablemap
        c.createStatement().executeUpdate(
                "INSERT INTO " + PatientMapTable.TABLENAME
                + " (" + PatientMapTable.FIELDNAME_PROJECTID + "," + PatientMapTable.FIELDNAME_PATIENTTABLENAME
                + ") VALUES (" + projectid + ",'" + patientTableName + "')");
        
        //populate format table
        c.setAutoCommit(false);
        for(int i = 0; i < customFields.size(); i++){
            CustomField a = customFields.get(i);
            c.createStatement().executeUpdate(
                    "INSERT INTO " + PatientFormatTable.TABLENAME + " (" + 
                    PatientFormatTable.FIELDNAME_PROJECTID + ", " +
                    PatientFormatTable.FIELDNAME_POSITION + ", " + 
                    PatientFormatTable.FIELDNAME_COLUMNNAME + ", " + 
                    PatientFormatTable.FIELDNAME_COLUMNTYPE + ", " + 
                    PatientFormatTable.FIELDNAME_FILTERABLE + ", " + 
                    PatientFormatTable.FIELDNAME_ALIAS + ", " + 
                    PatientFormatTable.FIELDNAME_DESCRIPTION +
                    ") VALUES (" + 
                    projectid + 
                    "," + i + 
                    ",\"" + a.getColumnName() + 
                    "\",\"" + a.getColumnType() + 
                    "\"," + (a.isFilterable() ? "1" : "0") + 
                    ",\"" + a.getAlias() + 
                    "\",\"" + a.getDescription() + "\");");
        }
        c.commit();
        c.setAutoCommit(true);   
        
    }
    
    public static void removePatient(int projectId, int[] patientIds) throws SQLException {
        
        String tablename = getPatientTablename(projectId);
        
        Connection c = ConnectionController.connect();
        c.setAutoCommit(false);       
        for(int id : patientIds){
            //remove all references
            CohortQueryUtil.removePatientReferences(projectId, id); 
            
            //remove from patient table
            c.createStatement().executeUpdate(
                    "DELETE FROM " + tablename + 
                    " WHERE " + PatientTable.FIELDNAME_ID + "=" + id);
        }
        c.commit();
        c.setAutoCommit(true);
    }
    
    public static void addPatient(int projectId, List<CustomField> cols, List<String> values) throws SQLException {
        
        String tablename = getPatientTablename(projectId);
        
        String query = "INSERT INTO " + tablename + " (";
        for(int i = 0; i < cols.size(); i++){
            query += "`" + cols.get(i).getColumnName() + "`";
            if(i != cols.size()-1){
                query += ",";
            }
        }
        query += ") VALUES (";
        for(int i = 0; i < cols.size(); i++){
            switch(cols.get(i).getFieldType()){
                case VARCHAR:
                case DATE:
                case TIMESTAMP:
                    query += "'" + values.get(i) + "'";
                    break;
                case BOOLEAN:
                    query += (Boolean.parseBoolean(values.get(i)) ? "1" : "0");
                default:
                    query += values.get(i);
            }
            if(i != cols.size()-1){
                query += ",";
            }
        }
        query += ");";
        
        Connection c = ConnectionController.connect();
        c.createStatement().executeUpdate(query);
    }

}
