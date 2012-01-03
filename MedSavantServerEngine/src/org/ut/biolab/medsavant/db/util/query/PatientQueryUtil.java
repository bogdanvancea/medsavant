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

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import java.util.HashMap;
import java.util.Map;
import org.xml.sax.SAXException;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.UpdateQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.rmi.RemoteException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.util.ConnectionController;

import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultpatientTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.PatientFormatTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.PatientTablemapTableSchema;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.format.CustomField.Category;
import org.ut.biolab.medsavant.db.format.PatientFormat;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.BinaryConditionMS;
import org.ut.biolab.medsavant.db.util.CustomTables;
import org.ut.biolab.medsavant.db.util.DBSettings;
import org.ut.biolab.medsavant.db.util.query.api.PatientQueryUtilAdapter;

/**
 *
 * @author Andrew
 */
public class PatientQueryUtil extends java.rmi.server.UnicastRemoteObject implements PatientQueryUtilAdapter {

    private static PatientQueryUtil instance;

    public static PatientQueryUtil getInstance() throws RemoteException {
        if (instance == null) {
            instance = new PatientQueryUtil();
        }
        return instance;
    }

    public PatientQueryUtil() throws RemoteException {}


    public List<Object[]> getBasicPatientInfo(String sid,int projectId, int limit) throws SQLException, NonFatalDatabaseException {

        String tablename = getPatientTablename(sid,projectId);

        TableSchema table = CustomTables.getCustomTableSchema(sid,tablename);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(
                table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_PATIENT_ID),
                table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_FAMILY_ID),
                table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID),
                table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_IDBIOMOM),
                table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_IDBIODAD),
                table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_GENDER),
                table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_DNA_IDS));

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(query.toString());

        List<Object[]> result = new ArrayList<Object[]>();
        while (rs.next()){
            result.add(new Object[] {
                rs.getInt(DefaultpatientTableSchema.COLUMNNAME_OF_PATIENT_ID),
                rs.getString(DefaultpatientTableSchema.COLUMNNAME_OF_FAMILY_ID),
                rs.getString(DefaultpatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID),
                rs.getString(DefaultpatientTableSchema.COLUMNNAME_OF_IDBIOMOM),
                rs.getString(DefaultpatientTableSchema.COLUMNNAME_OF_IDBIODAD),
                rs.getInt(DefaultpatientTableSchema.COLUMNNAME_OF_GENDER),
                rs.getString(DefaultpatientTableSchema.COLUMNNAME_OF_DNA_IDS)
            });
        }
        return result;
    }

    public List<Object[]> getPatients(String sid,int projectId) throws SQLException {
        String tablename = getPatientTablename(sid,projectId);

        TableSchema table = CustomTables.getCustomTableSchema(sid,tablename);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(query.toString());

        List<Object[]> result = new ArrayList<Object[]>();
        while (rs.next()){
            Object[] o = new Object[rs.getMetaData().getColumnCount()];
            for(int i = 0; i < rs.getMetaData().getColumnCount(); i++){
                try {
                    o[i] = rs.getObject(i+1);
                } catch (SQLException e){
                    //ignore...probably invalid input (ie. date 0000-00-00)
                }
            }
            result.add(o);
        }
        return result;
    }

    public Object[] getPatientRecord(String sid,int projectId, int patientId) throws SQLException {

        String tablename = getPatientTablename(sid,projectId);

        TableSchema table = CustomTables.getCustomTableSchema(sid,tablename);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_PATIENT_ID), patientId));

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(query.toString());

        rs.next();
        Object[] v = new Object[rs.getMetaData().getColumnCount()];
        for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
            try {
                v[i - 1] = rs.getObject(i);
            } catch (SQLException e){
                //ignore...probably invalid input (ie. date 0000-00-00)
            }
        }
        return v;
    }

    public List<String> getPatientFieldAliases(String sid,int projectId) throws SQLException {

        TableSchema table = MedSavantDatabase.PatientformatTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        query.addOrdering(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_POSITION), Dir.ASCENDING);

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(query.toString());

        List<String> result = new ArrayList<String>();

        for (CustomField af : PatientFormat.getDefaultAnnotationFormat()) {
            result.add(af.getAlias());
        }

        while(rs.next()){
            result.add(rs.getString(1));
        }
        return result;
    }

    public List<CustomField> getPatientFields(String sid,int projectId) throws SQLException {
        List<CustomField> result = new ArrayList<CustomField>();
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_PATIENT_ID, "int(11)", true, PatientFormat.ALIAS_OF_PATIENT_ID, "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_FAMILY_ID, "varchar(100)", true, PatientFormat.ALIAS_OF_FAMILY_ID, "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID, "varchar(100)", true, PatientFormat.ALIAS_OF_HOSPITAL_ID, "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_IDBIOMOM, "varchar(100)", true, PatientFormat.ALIAS_OF_IDBIOMOM, "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_IDBIODAD, "varchar(100)", true, PatientFormat.ALIAS_OF_IDBIODAD, "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_GENDER, "int(11)", true, PatientFormat.ALIAS_OF_GENDER, "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_DNA_IDS, "varchar(1000)", false, PatientFormat.ALIAS_OF_DNA_IDS, "", Category.PATIENT));
        result.add(new CustomField(DefaultpatientTableSchema.COLUMNNAME_OF_BAM_URL, "varchar(5000)", false, PatientFormat.ALIAS_OF_BAM_URL, "", Category.PATIENT));
        result.addAll(getCustomPatientFields(sid,projectId));
        return result;
    }

    public List<CustomField> getCustomPatientFields(String sid,int projectId) throws SQLException {

        TableSchema table = MedSavantDatabase.PatientformatTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(
                table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME),
                table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE),
                table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_FILTERABLE),
                table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS),
                table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_DESCRIPTION));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        query.addOrdering(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_POSITION), Dir.ASCENDING);

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(query.toString());

        List<CustomField> result = new ArrayList<CustomField>();
        while(rs.next()){
            result.add(new CustomField(
                    rs.getString(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME),
                    rs.getString(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE),
                    rs.getBoolean(PatientFormatTableSchema.COLUMNNAME_OF_FILTERABLE),
                    rs.getString(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS),
                    rs.getString(PatientFormatTableSchema.COLUMNNAME_OF_DESCRIPTION),
                    Category.PATIENT));
        }
        return result;
    }

    public String getPatientTablename(String sid,int projectId) throws SQLException {

        TableSchema table = MedSavantDatabase.PatienttablemapTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PATIENT_TABLENAME));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(query.toString());

        rs.next();
        return rs.getString(1);
    }


    public void createPatientTable(String sid,int projectid, List<CustomField> fields) throws SQLException, ParserConfigurationException, SAXException, IOException {

        String patientTableName = DBSettings.createPatientTableName(projectid);
        Connection c = ConnectionController.connectPooled(sid);

        //create basic fields
        String query =
                "CREATE TABLE `" + patientTableName + "` ("
                + "`" + DefaultpatientTableSchema.COLUMNNAME_OF_PATIENT_ID + "` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                + "`" + DefaultpatientTableSchema.COLUMNNAME_OF_FAMILY_ID + "` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultpatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID + "` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultpatientTableSchema.COLUMNNAME_OF_IDBIOMOM + "` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultpatientTableSchema.COLUMNNAME_OF_IDBIODAD + "` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultpatientTableSchema.COLUMNNAME_OF_GENDER + "` int(11) unsigned DEFAULT NULL,"
                + "`" + DefaultpatientTableSchema.COLUMNNAME_OF_AFFECTED + "` int(11) unsigned DEFAULT NULL,"
                + "`" + DefaultpatientTableSchema.COLUMNNAME_OF_DNA_IDS + "` varchar(1000) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultpatientTableSchema.COLUMNNAME_OF_BAM_URL + "` varchar(5000) COLLATE latin1_bin DEFAULT NULL,";

        for(CustomField field : fields){
            query += field.generateSchema();
        }

        query += "PRIMARY KEY (`" + DefaultpatientTableSchema.COLUMNNAME_OF_PATIENT_ID + "`)"
                + ") ENGINE=MyISAM;";

        //create patientFormatTable
        c.createStatement().execute(query);

        //add to tablemap
        TableSchema patientMapTable = MedSavantDatabase.PatienttablemapTableSchema;
        InsertQuery query1 = new InsertQuery(patientMapTable.getTable());
        query1.addColumn(patientMapTable.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid);
        query1.addColumn(patientMapTable.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PATIENT_TABLENAME), patientTableName);
        c.createStatement().executeUpdate(query1.toString());

        //populate format patientFormatTable
        TableSchema patientFormatTable = MedSavantDatabase.PatientformatTableSchema;
        c.setAutoCommit(false);
        for(int i = 0; i < fields.size(); i++){
            CustomField a = fields.get(i);
            InsertQuery query2 = new InsertQuery(patientFormatTable.getTable());
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid);
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_POSITION), i);
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME), a.getColumnName());
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE), a.getColumnTypeString());
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_FILTERABLE), (a.isFilterable() ? "1" : "0"));
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS), a.getAlias());
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_DESCRIPTION), a.getDescription());
            c.createStatement().executeUpdate(query2.toString());
        }
        c.commit();
        c.setAutoCommit(true);

    }

    public void removePatient(String sid,int projectId, int[] patientIds) throws SQLException, RemoteException {

        String tablename = getPatientTablename(sid,projectId);
        TableSchema table = CustomTables.getCustomTableSchema(sid,tablename);

        Connection c = ConnectionController.connectPooled(sid);
        c.setAutoCommit(false);
        for(int id : patientIds){
            //remove all references
            CohortQueryUtil.getInstance().removePatientReferences(sid,projectId, id);

            //remove from patient patientFormatTable
            DeleteQuery query = new DeleteQuery(table.getTable());
            query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_PATIENT_ID), id));
            c.createStatement().executeUpdate(query.toString());
        }
        c.commit();
        c.setAutoCommit(true);
    }

    public void addPatient(String sid,int projectId, List<CustomField> cols, List<String> values) throws SQLException {

        String tablename = getPatientTablename(sid,projectId);
        TableSchema table = CustomTables.getCustomTableSchema(sid,tablename);

        InsertQuery query = new InsertQuery(table.getTable());
        for(int i = 0; i < Math.min(cols.size(), values.size()); i++){
            query.addColumn(new DbColumn(table.getTable(), cols.get(i).getColumnName(), cols.get(i).getColumnTypeString(), 100), values.get(i));
        }

        ConnectionController.connectPooled(sid).createStatement().executeUpdate(query.toString());
    }

    public Map<Object, List<String>> getDNAIdsForValues(String sid,int projectId, String columnName) throws NonFatalDatabaseException, SQLException {

        String tablename = getPatientTablename(sid,projectId);

        TableSchema table = CustomTables.getCustomTableSchema(sid,tablename);

        DbColumn currentDNAId = table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_DNA_IDS);
        DbColumn testColumn = table.getDBColumn(columnName);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.setIsDistinct(true);
        q.addColumns(currentDNAId, testColumn);

        Statement s = ConnectionController.connectPooled(sid).createStatement();
        ResultSet rs = s.executeQuery(q.toString());

        Map<Object, List<String>> map = new HashMap<Object, List<String>>();
        while(rs.next()){
            Object o = rs.getObject(columnName);
            if(o == null) o = "";
            if(map.get(o) == null) map.put(o, new ArrayList<String>());
            String dnaIdsString = rs.getString(DefaultpatientTableSchema.COLUMNNAME_OF_DNA_IDS);
            if(dnaIdsString == null) continue;
            String[] dnaIds = dnaIdsString.split(",");
            for(String id : dnaIds){
                if(!map.get(o).contains(id)){
                    map.get(o).add(id);
                }
            }
        }
        return map;
    }

    public List<String> getDNAIdsWithValuesInRange(String sid,int projectId, String columnName, Range r) throws NonFatalDatabaseException, SQLException {

        String tablename = getPatientTablename(sid,projectId);

        TableSchema table = CustomTables.getCustomTableSchema(sid,tablename);

        DbColumn currentDNAId = table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_DNA_IDS);
        DbColumn testColumn = table.getDBColumn(columnName);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.setIsDistinct(true);
        q.addColumns(currentDNAId);
        q.addCondition(BinaryCondition.greaterThan(testColumn, r.getMin(), true));
        q.addCondition(BinaryCondition.lessThan(testColumn, r.getMax(), true));

        Statement s = ConnectionController.connectPooled(sid).createStatement();
        ResultSet rs = s.executeQuery(q.toString());

        List<String> result = new ArrayList<String>();
        while(rs.next()){
            String[] dnaIds = rs.getString(1).split(",");
            for(String id : dnaIds){
                if(!result.contains(id)){
                    result.add(id);
                }
            }
        }
        return result;
    }

    public List<String> getDNAIdsForStringList(String sid,TableSchema table, List<String> list, String columnname) throws NonFatalDatabaseException, SQLException {

        DbColumn currentDNAId = table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_DNA_IDS);
        DbColumn testColumn = table.getDBColumn(columnname);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.setIsDistinct(true);
        q.addColumns(currentDNAId);

        Condition[] conditions = new Condition[list.size()];
        for(int i = 0; i < list.size(); i++){
            conditions[i] = BinaryConditionMS.equalTo(testColumn, list.get(i));
        }
        q.addCondition(ComboCondition.or(conditions));

        Statement s = ConnectionController.connectPooled(sid).createStatement();
        ResultSet rs = s.executeQuery(q.toString());

        List<String> result = new ArrayList<String>();
        while(rs.next()){
            String[] dnaIds = rs.getString(1).split(",");
            for(String id : dnaIds){
                if(!result.contains(id)){
                    result.add(id);
                }
            }
        }
        return result;
    }

    /*public List<String> getDNAIdsForIntList(TableSchema patientFormatTable, List<Integer> list, String columnname) throws NonFatalDatabaseException, SQLException {

        DbColumn currentDNAId = patientFormatTable.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_DNA_IDS);
        DbColumn testColumn = patientFormatTable.getDBColumn(columnname);

        SelectQuery q = new SelectQuery();
        q.addFromTable(patientFormatTable.getTable());
        q.setIsDistinct(true);
        q.addColumns(currentDNAId);

        Condition[] conditions = new Condition[list.size()];
        for(int i = 0; i < list.size(); i++){
            conditions[i] = BinaryConditionMS.equalTo(testColumn, list.get(i));
        }
        q.addCondition(ComboCondition.or(conditions));

        Statement s = ConnectionController.connectPooled().createStatement();
        ResultSet rs = s.executeQuery(q.toString());

        List<String> result = new ArrayList<String>();
        while(rs.next()){
            String[] dnaIds = rs.getString(1).split(",");
            for(String id : dnaIds){
                if(!result.contains(id)){
                    result.add(id);
                }
            }
        }
        return result;
    }*/

    public void updateFields(String sid,int projectId, List<CustomField> fields) throws SQLException {

        List<CustomField> currentFields = getCustomPatientFields(sid,projectId);

        String tablename = getPatientTablename(sid,projectId);
        //TableSchema patientTable = CustomTables.getCustomTableSchema(tablename);
        TableSchema patientFormatTable = MedSavantDatabase.PatientformatTableSchema;

        Connection c = ConnectionController.connectPooled(sid);
        c.setAutoCommit(false);

        //remove unused fields
        for(CustomField f : currentFields){
            if(!fields.contains(f)){
                DeleteQuery q = new DeleteQuery(patientFormatTable.getTable());
                q.addCondition(BinaryConditionMS.equalTo(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
                q.addCondition(BinaryConditionMS.equalTo(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME), f.getColumnName()));
                c.createStatement().execute(q.toString());

                String q1 = "ALTER TABLE `" + tablename + "` DROP COLUMN `" + f.getColumnName() + "`";
                c.createStatement().execute(q1);
            }
        }

        //modify old fields, add new fields
        int tempPos = 5002;
        for(CustomField f : fields){
            if(currentFields.contains(f)){
                UpdateQuery q = new UpdateQuery(patientFormatTable.getTable());
                q.addSetClause(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS), f.getAlias());
                q.addSetClause(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_DESCRIPTION), f.getDescription());
                q.addSetClause(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_FILTERABLE), (f.isFilterable() ? "1" : "0"));
                q.addCondition(BinaryConditionMS.equalTo(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
                q.addCondition(BinaryConditionMS.equalTo(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME), f.getColumnName()));
                c.createStatement().executeUpdate(q.toString());
            } else {
                InsertQuery q = new InsertQuery(patientFormatTable.getTable());
                q.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId);
                q.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_POSITION), tempPos++);
                q.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME), f.getColumnName());
                q.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE), f.getColumnTypeString());
                q.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_FILTERABLE), (f.isFilterable() ? "1" : "0"));
                q.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS), f.getAlias());
                q.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_DESCRIPTION), f.getDescription());
                c.createStatement().executeUpdate(q.toString());

                String q1 = "ALTER TABLE `" + tablename + "` ADD " + f.generateSchema().replaceAll(",", "");
                c.createStatement().execute(q1);
            }
        }

        c.commit();
        c.setAutoCommit(true);

        TableSchema patientTable = CustomTables.getCustomTableSchema(sid,tablename, true);
        List<DbColumn> columns = patientTable.getColumns();
        List<DbColumn> defaultColumns = MedSavantDatabase.DefaultpatientTableSchema.getColumns();
        c.setAutoCommit(false);
        int i = 0;
        for(DbColumn col : columns){
            boolean isDefault = false;
            for(DbColumn a : defaultColumns){
                if(col.getColumnNameSQL().equals(a.getColumnNameSQL())){
                    isDefault = true;
                }
            }
            if(isDefault) continue;

            UpdateQuery q = new UpdateQuery(patientFormatTable.getTable());
            q.addSetClause(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_POSITION), i++);
            q.addCondition(BinaryConditionMS.equalTo(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
            q.addCondition(BinaryConditionMS.equalTo(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME), col.getColumnNameSQL()));
            c.createStatement().executeUpdate(q.toString());

        }

        c.commit();
        c.setAutoCommit(true);
    }

    /*
     * Given a list of values for field A, get the corresponding values from field B
     */
    public List<Object> getValuesFromField(String sid,int projectId, String columnNameA, String columnNameB, List<Object> values) throws SQLException {

        String tablename = getPatientTablename(sid,projectId);
        TableSchema table = CustomTables.getCustomTableSchema(sid,tablename);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(columnNameB));
        Condition[] conditions = new Condition[values.size()];
        for(int i = 0; i < values.size(); i++){
            conditions[i] = BinaryConditionMS.equalTo(table.getDBColumn(columnNameA), values.get(i));
        }
        query.addCondition(ComboCondition.or(conditions));

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(query.toString());

        List<Object> result = new ArrayList<Object>();
        while(rs.next()){
            result.add(rs.getObject(1));
        }

        return result;
    }

    public List<String> getDNAIdsFromField(String sid,int projectId, String columnNameA, List<Object> values) throws SQLException {

        List<Object> l1 = getValuesFromField(sid,projectId, columnNameA, DefaultpatientTableSchema.COLUMNNAME_OF_DNA_IDS, values);
        List<String> result = new ArrayList<String>();
        for(Object o : l1){
            String[] dnaIds = ((String) o).split(",");
            for(String id : dnaIds){
                if(!result.contains(id)){
                    result.add(id);
                }
            }
        }
        return result;
    }

    public List<String> getValuesFromDNAIds(String sid,int projectId, String columnNameB, List<String> ids) throws SQLException {

        String tablename = getPatientTablename(sid,projectId);
        TableSchema table = CustomTables.getCustomTableSchema(sid,tablename);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(columnNameB));
        Condition[] conditions = new Condition[ids.size()];
        for(int i = 0; i < ids.size(); i++){
            conditions[i] = BinaryCondition.like(table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_DNA_IDS), "%" + ids.get(i) + "%");
        }
        query.addCondition(ComboCondition.or(conditions));


        String s = query.toString();
        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(query.toString());

        List<String> result = new ArrayList<String>();
        while(rs.next()){
            result.add(rs.getString(1));
        }

        return result;
    }

    public List<Object[]> getFamily(String sid,int projectId, String family_id) throws SQLException {

        String tablename = getPatientTablename(sid,projectId);
        TableSchema table = CustomTables.getCustomTableSchema(sid,tablename);

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());

        query.addColumns(table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID));

        // TODO: replace with OO names
        query.addColumns(table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_IDBIOMOM));
        query.addColumns(table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_IDBIODAD));
        query.addColumns(table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_PATIENT_ID));
        query.addColumns(table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_GENDER));
        query.addColumns(table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_AFFECTED));

        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_FAMILY_ID), family_id));

        String s = query.toString();
        System.out.println(s);
        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(query.toString());

        List<Object[]> result = new ArrayList<Object[]>();
        while(rs.next()){
            Object[] r = new Object[6];
            r[0] = rs.getString(1);
            r[1] = rs.getString(2);
            r[2] = rs.getString(3);
            r[3] = rs.getInt(4);
            r[4] = rs.getInt(5);
            r[5] = rs.getInt(6);
            result.add(r);
        }

        return result;
    }


    public List<Object[]> getFamilyOfPatient(String sid,int projectId, int pid) throws SQLException {

        String family_id = getFamilyIdOfPatient(sid,projectId, pid);
        if(family_id == null){
            return new ArrayList<Object[]>();
        }

        return getFamily(sid,projectId, family_id);
    }

    public String getFamilyIdOfPatient(String sid,int projectId, int pid) throws SQLException {
        String tablename = getPatientTablename(sid,projectId);
        TableSchema table = CustomTables.getCustomTableSchema(sid,tablename);

        SelectQuery q1 = new SelectQuery();
        q1.addFromTable(table.getTable());
        q1.addColumns(table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_FAMILY_ID));
        q1.addCondition(BinaryCondition.equalTo(table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_PATIENT_ID), pid));

        ResultSet rs1 = ConnectionController.connectPooled(sid).createStatement().executeQuery(q1.toString());

        if (!rs1.next()) {
            return null;
        }

        String family_id = rs1.getString(1);
        return family_id;
    }

    public List<String> getFamilyIds(String sid,int projectId) throws SQLException {

        String tablename = getPatientTablename(sid,projectId);
        TableSchema table = CustomTables.getCustomTableSchema(sid,tablename);

        SelectQuery q1 = new SelectQuery();
        q1.addFromTable(table.getTable());
        q1.addColumns(table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_FAMILY_ID));

        q1.setIsDistinct(true);

        ResultSet rs1 = ConnectionController.connectPooled(sid).createStatement().executeQuery(q1.toString());

        List<String> ids = new ArrayList<String>();
        while (rs1.next()) {
            ids.add(rs1.getString(1));
        }

        ids.remove(null);

        return ids;
    }


    //SELECT `dna_ids` FROM `z_patient_proj1` WHERE `family_id` = 'AB0001' AND `dna_ids` IS NOT null;
    public Map<String,String> getDNAIdsForFamily(String sid,int projectId, String familyId) throws SQLException {

        String tablename = getPatientTablename(sid,projectId);
        TableSchema table = CustomTables.getCustomTableSchema(sid,tablename);

        SelectQuery q1 = new SelectQuery();
        q1.addFromTable(table.getTable());
        q1.addColumns(table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID));
        q1.addColumns(table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_DNA_IDS));
        q1.addCondition(BinaryCondition.equalTo(table.getDBColumn(DefaultpatientTableSchema.COLUMNNAME_OF_FAMILY_ID), familyId));

        //System.out.println("Getting DNA ids for family: " + familyId);
        System.out.println(q1);

        ResultSet rs1 = ConnectionController.connectPooled(sid).createStatement().executeQuery(q1.toString());

        Map<String,String> patientIDToDNAIDMap = new HashMap<String,String>();

        //List<String> ids = new ArrayList<String>();
        while (rs1.next()) {
            String patientID = rs1.getString(1);
            String DNAIDString = rs1.getString(2);
            if (DNAIDString != null && !DNAIDString.isEmpty()) {
                patientIDToDNAIDMap.put(patientID, DNAIDString);
            }
        }

        return patientIDToDNAIDMap;
    }

    public void clearPatients(String sid,int projectId) throws SQLException{

        String tablename = getPatientTablename(sid,projectId);
        TableSchema table = CustomTables.getCustomTableSchema(sid,tablename);

        DeleteQuery query = new DeleteQuery(table.getTable());

        ConnectionController.connectPooled(sid).createStatement().execute(query.toString());
    }

    public List<String> parseDnaIds(String s){
        List<String> result = new ArrayList<String>();
        if(s == null) return result;
        String[] dnaIds = s.split(",");
        for(String id : dnaIds){
            if(!result.contains(id)){
                result.add(id);
            }
        }
        return result;
    }

}