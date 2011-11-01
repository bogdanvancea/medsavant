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

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.FunctionCall;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.model.structure.CustomTables;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.BinaryConditionMS;
import org.ut.biolab.medsavant.db.util.ConnectionController;

/**
 *
 * @author Andrew
 */
public class VariantQueryUtil {
    
    public static TableSchema getCustomTableSchema(int projectId, int referenceId) throws SQLException {
        return CustomTables.getCustomTableSchema(ProjectQueryUtil.getVariantTablename(projectId, referenceId));
    }
    
    public static Vector getVariants(int projectId, int referenceId, int limit) throws SQLException {       
        return getVariants(projectId, referenceId, new Condition[1][], limit);
    }
   
    public static Vector getVariants(int projectId, int referenceId, Condition[][] conditions, int limit) throws SQLException {            
        
        TableSchema table = CustomTables.getCustomTableSchema(ProjectQueryUtil.getVariantTablename(projectId, referenceId));
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        addConditionsToQuery(query, conditions);
        
        Connection conn = ConnectionController.connectPooled();
        ResultSet rs = conn.createStatement().executeQuery(query.toString() + " LIMIT " + limit);
        
        ResultSetMetaData rsMetaData = rs.getMetaData();
        int numberColumns = rsMetaData.getColumnCount();
        
        Vector result = new Vector();
        while(rs.next()){
            Vector v = new Vector();
            for(int i = 1; i <= numberColumns; i++){
                v.add(rs.getObject(i));
            }
            result.add(v);
        }
        
        return result;
    }
    
    public static double[] getExtremeValuesForColumn(String tablename, String columnname) throws SQLException { 
        
        TableSchema table = CustomTables.getCustomTableSchema(tablename);
        
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addCustomColumns(FunctionCall.min().addColumnParams(table.getDBColumn(columnname)));
        query.addCustomColumns(FunctionCall.max().addColumnParams(table.getDBColumn(columnname)));
      
        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());
        
        double[] result = new double[2];
        rs.next();
        result[0] = rs.getDouble(1);
        result[1] = rs.getDouble(2);
        
        return result;
    }
    
    public static List<String> getDistinctValuesForColumn(String tablename, String columnname) throws SQLException {
        
        TableSchema table = CustomTables.getCustomTableSchema(tablename);
        
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.setIsDistinct(true);
        query.addColumns(table.getDBColumn(columnname)); 
        query.addOrdering(table.getDBColumn(columnname), Dir.ASCENDING);
        
        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());
        
        List<String> result = new ArrayList<String>();
        while(rs.next()){
            String val = rs.getString(1);
            if(val == null){
                result.add("");
            } else {
                result.add(val);
            }
        }
        
        return result;
    }
    
    public static int getNumFilteredVariants(int projectId, int referenceId) throws SQLException {
        return getNumFilteredVariants(projectId, referenceId, new Condition[0][]);
    }
    
    public static int getNumFilteredVariants(int projectId, int referenceId, Condition[][] conditions) throws SQLException {
        
        String name = ProjectQueryUtil.getVariantTablename(projectId, referenceId);
        
        if (name == null) { return -1; }
        
        TableSchema table = CustomTables.getCustomTableSchema(name);
               
        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        addConditionsToQuery(q, conditions);

        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(q.toString());
        
        rs.next();
        return rs.getInt(1);
    }
    
    public static int getFilteredFrequencyValuesForColumnInRange(int projectId, int referenceId, Condition[][] conditions, String columnname, double min, double max) throws SQLException {
        
        TableSchema table = CustomTables.getCustomTableSchema(ProjectQueryUtil.getVariantTablename(projectId, referenceId));
               
        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        q.addCondition(BinaryCondition.greaterThan(table.getDBColumn(columnname), min, true)); 
        q.addCondition(BinaryCondition.lessThan(table.getDBColumn(columnname), max, false)); 
        addConditionsToQuery(q, conditions);

        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(q.toString());
        rs.next();
        
        return rs.getInt(1);        
    }
    
    public static Map<String, Integer> getFilteredFrequencyValuesForColumn(int projectId, int referenceId, Condition[][] conditions, String columnAlias) throws SQLException {
        
        TableSchema tableSchema = CustomTables.getCustomTableSchema(ProjectQueryUtil.getVariantTablename(projectId, referenceId));
        DbTable table = tableSchema.getTable();
        DbColumn col = tableSchema.getDBColumnByAlias(columnAlias);
          
        return getFilteredFrequencyValuesForColumn(table, conditions, col);
    }
    
    public static Map<String, Integer> getFilteredFrequencyValuesForColumn(DbTable table, Condition[][] conditions, DbColumn column) throws SQLException {
                       
        SelectQuery q = new SelectQuery();
        q.addFromTable(table);
        q.addColumns(column);
        q.addCustomColumns(FunctionCall.countAll());
        addConditionsToQuery(q, conditions);
        q.addGroupings(column);
        
        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(q.toString());
        
        Map<String, Integer> map = new HashMap<String, Integer>();
        
        System.out.println("Got frequency values for " + column.getAbsoluteName());
        
        while (rs.next()) {
            String key = rs.getString(1);
            if (key == null) { key = ""; }
            map.put(key, rs.getInt(2));
        }

        return map;     
    }
    
    public static int getNumVariantsInRange(int projectId, int referenceId, Condition[][] conditions, String chrom, long start, long end) throws SQLException, NonFatalDatabaseException {
        
        TableSchema table = CustomTables.getCustomTableSchema(ProjectQueryUtil.getVariantTablename(projectId, referenceId));
               
        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        q.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM), chrom));
        q.addCondition(BinaryCondition.greaterThan(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION), start, true));
        q.addCondition(BinaryCondition.lessThan(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION), end, false));
        addConditionsToQuery(q, conditions);
        
        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(q.toString());
        
        rs.next();
        return rs.getInt(1);
    }
    
    public static int[] getNumVariantsForBins(int projectId, int referenceId, Condition[][] conditions, String chrom, int binsize, int numbins) throws SQLException, NonFatalDatabaseException {
        
        TableSchema table = CustomTables.getCustomTableSchema(ProjectQueryUtil.getVariantTablename(projectId, referenceId));
        
        SelectQuery queryBase = new SelectQuery();
        queryBase.addFromTable(table.getTable());
        queryBase.addColumns(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION));
        queryBase.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM), chrom));
        addConditionsToQuery(queryBase, conditions);
        
        /*String queryBase = 
                "SELECT `" + VariantTable.FIELDNAME_POSITION + "`" +
                " FROM " + ProjectQueryUtil.getVariantTablename(projectId, referenceId) + " t0" + 
                " WHERE `" + VariantTable.FIELDNAME_CHROM + "`=\"" + chrom + "\"";
        if(!conditions.isEmpty()){
            queryBase += " AND ";
        }
        queryBase += conditionsToStringOr(conditions);*/
        
        
        //TODO
        String query = "select y.range as `range`, count(*) as `number of occurences` "
                + "from ("
                + "select case ";
        int pos = 0;
        for(int i = 0; i < numbins; i++){
            query += "when `" + DefaultVariantTableSchema.COLUMNNAME_OF_POSITION + "` between " + pos + " and " + (pos+binsize) + " then " + i + " ";
            pos += binsize;
        }
        
        query += "end as `range` "
                + "from (";
        query += queryBase.toString();
        query += ") x ) y "
                + "group by y.`range`";
        
        Connection conn = ConnectionController.connectPooled();
        ResultSet rs = conn.createStatement().executeQuery(query.toString());
        
        int[] numRows = new int[numbins];
        for(int i = 0; i < numbins; i++) numRows[i] = 0;
        while(rs.next()){
            int index = rs.getInt(1);
            numRows[index] = rs.getInt(2);
        }
        return numRows;     
    }
    
    public static void uploadFileToVariantTable(File file, String tableName) throws SQLException{
        
        // TODO: for some reason the connection is closed going into this function
        Connection c = ConnectionController.connectPooled();
        
        System.out.println("Uploading file to variant table: " + 
                "LOAD DATA LOCAL INFILE '" + file.getAbsolutePath().replaceAll("\\\\", "/") + "' "
                + "INTO TABLE " + tableName + " "
                + "FIELDS TERMINATED BY ',' ENCLOSED BY '\"' "
                + "LINES TERMINATED BY '\\r\\n';");

        Statement s = c.createStatement();
        s.setQueryTimeout(60 * 60); // 1 hour
        s.execute(
                "LOAD DATA LOCAL INFILE '" + file.getAbsolutePath().replaceAll("\\\\", "/") + "' "
                + "INTO TABLE " + tableName + " "
                + "FIELDS TERMINATED BY ',' ENCLOSED BY '\"' "
                + "LINES TERMINATED BY '\\r\\n';");    
    }
    
    public static int getNumPatientsWithVariantsInRange(int projectId, int referenceId, Condition[][] conditions, String chrom, int start, int end) throws SQLException {
        
        TableSchema table = getCustomTableSchema(projectId, referenceId);
        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns("COUNT(DISTINCT " + DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID + ")");
        addConditionsToQuery(q, conditions);
        
        Condition[] cond = new Condition[3];
        cond[0] = new BinaryCondition(BinaryCondition.Op.EQUAL_TO, table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM), chrom);
        cond[1] = new BinaryCondition(BinaryCondition.Op.GREATER_THAN_OR_EQUAL_TO, table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION), start);
        cond[2] = new BinaryCondition(BinaryCondition.Op.LESS_THAN, table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION), end);       
        q.addCondition(ComboCondition.and(cond));        
        
        String query = q.toString();
        query = query.replaceFirst("'", "").replaceFirst("'", "");
        
        Statement s = ConnectionController.connectPooled().createStatement();
        ResultSet rs = s.executeQuery(query);
        rs.next();

        int numrows = rs.getInt(1);
        
        return numrows;
    }

    public static void addConditionsToQuery(SelectQuery query, Condition[][] conditions){
        Condition[] c = new Condition[conditions.length];
        for(int i = 0; i < conditions.length; i++){
            c[i] = ComboCondition.and(conditions[i]);
        }
        query.addCondition(ComboCondition.or(c));
    }

}
